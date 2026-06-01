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

// Validate + check duplicate cho từng row CSV
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

        // Validate họ tên
        if (!StringUtils.hasText(row.getFullName()))
            errors.add("Họ tên không được trống");

        // Validate SĐT
        String phone = StringUtils.hasText(row.getPhone())
                ? row.getPhone().trim().replaceAll("\\s+", "") : "";

        if (phone.isEmpty())
            errors.add("Số điện thoại không được trống");
        else if (!phone.matches("^(0|\\+84)[0-9]{8,10}$"))
            errors.add("SĐT sai định dạng: " + phone);

        // Validate email
        if (StringUtils.hasText(row.getEmail())
                && !row.getEmail().matches(
                "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            errors.add("Email sai định dạng: " + row.getEmail());

        // Nếu có lỗi → throw để skip row =
        if (!errors.isEmpty())
            throw new ImportValidationException(String.join("; ", errors));

        // Kiểm tra trùng SĐT bằng Set trong RAM
        String normalized = normalizePhone(phone);
        if (existingPhones.contains(normalized))
            throw new ImportValidationException("SĐT trùng: " + phone);

        existingPhones.add(normalized);   // thêm để check trong cùng file

        // Map sang Entity
        Lead lead = new Lead();
        lead.setFullName(row.getFullName().trim());
        lead.setPhone(phone);
        lead.setPhoneNormalized(normalized);
        lead.setEmail(StringUtils.hasText(row.getEmail())
                ? row.getEmail().trim() : null);
        lead.setSchoolName(row.getSchoolName());
        lead.setProvince(row.getProvince());
        lead.setAddress(row.getAddress());
        lead.setNote(row.getNote());

        // Gender
        if (StringUtils.hasText(row.getGender())) {
            try { lead.setGender(Gender.valueOf(row.getGender().toUpperCase())); }
            catch (Exception ignored) {}
        }

        // Ngày sinh
        if (StringUtils.hasText(row.getBirthDate())) {
            try {
                lead.setBirthDate(LocalDate.parse(
                        row.getBirthDate().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (Exception ignored) {}
        }

        // Năm tốt nghiệp
        if (StringUtils.hasText(row.getGraduationYear())) {
            try { lead.setGraduationYear(
                    Integer.parseInt(row.getGraduationYear().trim()));
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

    private String normalizePhone(String phone) {
        return phone.startsWith("+84") ? "0" + phone.substring(3) : phone;
    }
}