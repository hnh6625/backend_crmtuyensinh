package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.application.dto.LeadRowDto;
import com.company.crm_backend.lead.domain.Gender;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.domain.LeadSource;
import com.company.crm_backend.lead.infrastructure.LeadSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LeadItemProcessor implements ItemProcessor<LeadRowDto, Lead> {

    private final LeadSourceRepository leadSourceRepository;

    // Set phone đã có trong DB — load 1 lần trước job, tránh query mỗi row
    @Value("#{jobExecutionContext['existingPhones']}")
    private Set<String> existingPhones;

    // Cache source tên → entity, tránh query DB mỗi row
    private final Map<String, LeadSource> sourceCache = new HashMap<>();

    @Override
    public Lead process(LeadRowDto row) throws Exception {
        List<String> errors = new ArrayList<>();

        // 1. Validate họ tên
        if (!StringUtils.hasText(row.getFullName())) {
            errors.add("Họ tên không được trống");
        }

        // 2. CHUẨN HOÁ SĐT (Fix lỗi Excel mất số 0, dấu nháy đơn, khoảng trắng)
        String normalizedPhone = normalizePhone(row.getPhone());

        // Validate SĐT sau khi đã được làm sạch
        if (normalizedPhone == null || normalizedPhone.isEmpty()) {
            errors.add("Số điện thoại không được trống");
        } else if (!normalizedPhone.matches("^0[35789][0-9]{8}$")) { // Bắt buộc 10 số, đầu 03,05,07,08,09
            errors.add("SĐT sai định dạng: " + row.getPhone());
        }

        // 3. Validate email
        if (StringUtils.hasText(row.getEmail())
                && !row.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            errors.add("Email sai định dạng: " + row.getEmail());
        }

        // Nếu có lỗi → throw để bỏ qua dòng này và ghi vào bảng Lỗi
        if (!errors.isEmpty()) {
            throw new ImportValidationException(String.join("; ", errors));
        }

        // 4. Kiểm tra trùng SĐT bằng Set trong RAM (Tốc độ ánh sáng)
        if (existingPhones.contains(normalizedPhone)) {
            throw new ImportValidationException("SĐT trùng: " + normalizedPhone);
        }

        // Ghi SĐT mới vào RAM để kiểm tra các dòng tiếp theo trong cùng 1 file Excel
        existingPhones.add(normalizedPhone);

        // 5. Map sang Entity
        Lead lead = new Lead();
        lead.setFullName(row.getFullName().trim());
        lead.setPhone(normalizedPhone);           // Lưu số đã làm sạch (VD: 0902501039)
        lead.setPhoneNormalized(normalizedPhone);
        lead.setEmail(StringUtils.hasText(row.getEmail()) ? row.getEmail().trim() : null);
        lead.setSchoolName(row.getSchoolName());
        lead.setProvince(row.getProvince());
        lead.setAddress(row.getAddress());
        lead.setNote(row.getNote());

        // Gender
        if (StringUtils.hasText(row.getGender())) {
            try {
                lead.setGender(Gender.valueOf(row.getGender().toUpperCase()));
            } catch (Exception ignored) {}
        }

        // Ngày sinh (Nâng cấp: Xử lý cả 2 định dạng của Excel)
        if (StringUtils.hasText(row.getBirthDate())) {
            String bd = row.getBirthDate().trim();
            try {
                if (bd.contains("-")) {
                    lead.setBirthDate(LocalDate.parse(bd, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else {
                    lead.setBirthDate(LocalDate.parse(bd, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            } catch (Exception ignored) {}
        }

        // Năm tốt nghiệp
        if (StringUtils.hasText(row.getGraduationYear())) {
            try {
                lead.setGraduationYear(Integer.parseInt(row.getGraduationYear().trim()));
            } catch (Exception ignored) {}
        }

        // Nguồn — dùng cache
        if (StringUtils.hasText(row.getSourceName())) {
            LeadSource src = sourceCache.computeIfAbsent(
                    row.getSourceName().trim(),
                    name -> leadSourceRepository.findBySourceName(name).orElse(null));
            lead.setSource(src);
        }

        return lead;
    }

    // HÀM BỌC LÓT DỮ LIỆU SĐT
    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;

        // Xóa toàn bộ khoảng trắng, dấu nháy đơn, ký tự lạ
        String p = phone.trim().replaceAll("[^0-9+]", "");

        // Xử lý các tiền tố quốc tế
        if (p.startsWith("+84")) {
            p = "0" + p.substring(3);
        } else if (p.startsWith("84") && p.length() == 11) {
            p = "0" + p.substring(2);
        }
        // Đắp lại số 0 bị mất trong Excel
        else if (p.length() == 9 && p.matches("^[35789]\\d{8}$")) {
            p = "0" + p;
        }

        return p;
    }
}