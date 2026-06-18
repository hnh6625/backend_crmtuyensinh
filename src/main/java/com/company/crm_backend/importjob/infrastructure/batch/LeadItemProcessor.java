package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.application.dto.LeadRowDto;
import com.company.crm_backend.lead.domain.Gender;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.domain.LeadSource;
import com.company.crm_backend.lead.domain.LeadStatus;
import com.company.crm_backend.lead.infrastructure.LeadSourceRepository;
import com.company.crm_backend.lead.infrastructure.LeadStatusRepository;
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
    private final LeadStatusRepository leadStatusRepository;

    // Set phone đã có trong DB — load 1 lần trước job, tránh query mỗi row
    @Value("#{jobExecutionContext['existingPhones']}")
    private Set<String> existingPhones;

    // Cache source tên → entity, tránh query DB mỗi row
    private final Map<String, LeadSource> sourceCache = new HashMap<>();

    @Override
    public Lead process(LeadRowDto row) throws Exception {
        List<String> errors = new ArrayList<>();

        // Validate Họ Tên
        if (!StringUtils.hasText(row.getFullName())) {
            errors.add("Cột [Họ tên]: Không được để trống");
        } else if (row.getFullName().trim().length() > 255) {
            errors.add("Cột [Họ tên]: Vượt quá 255 ký tự");
        }

        // CHUẨN HOÁ & Validate SĐT
        String normalizedPhone = normalizePhone(row.getPhone());
        if (!StringUtils.hasText(normalizedPhone)) {
            errors.add("Cột [Số điện thoại]: Không được để trống");
        } else if (!normalizedPhone.matches("^0[35789][0-9]{8}$")) {
            errors.add("Cột [Số điện thoại]: Sai định dạng (" + row.getPhone() + ")");
        } else if (existingPhones != null && existingPhones.contains(normalizedPhone)) {
            errors.add("Cột [Số điện thoại]: Đã tồn tại trong hệ thống hoặc bị trùng lặp trong file (" + normalizedPhone + ")");
        }

        // Validate Email
        if (StringUtils.hasText(row.getEmail())) {
            String email = row.getEmail().trim();
            if (email.length() > 255) {
                errors.add("Cột [Email]: Vượt quá 255 ký tự");
            } else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                errors.add("Cột [Email]: Sai định dạng (" + email + ")");
            }
        }

        // Validate Giới tính
        Gender parsedGender = null;
        if (StringUtils.hasText(row.getGender())) {
            try {
                parsedGender = Gender.valueOf(row.getGender().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Cột [Giới tính]: Chỉ chấp nhận MALE, FEMALE hoặc OTHER");
            }
        }

        // Validate Ngày sinh (Xử lý đa định dạng - Bất tử với Excel)
        LocalDate parsedDob = null;
        if (StringUtils.hasText(row.getBirthDate())) {
            // Thay thế các dấu phân cách lạ thành dấu /
            String bd = row.getBirthDate().trim().replace("-", "/").replace(".", "/");
            boolean isParsed = false;

            // Danh sách các định dạng phổ biến nhất mà Excel có thể sinh ra
            String[] patterns = {
                    "d/M/yyyy",   // Chuẩn VN (15/08/2005, 7/9/2002)
                    "yyyy/M/d",   // Chuẩn ISO (2005/08/15)
                    "M/d/yyyy",   // Chuẩn Mỹ (08/15/2005)
                    "d/M/yy"      // Năm rút gọn (15/08/05)
            };

            for (String pattern : patterns) {
                try {
                    parsedDob = LocalDate.parse(bd, DateTimeFormatter.ofPattern(pattern));

                    // Xử lý lỗi Excel tự nhảy năm sang thế kỷ sau
                    if (parsedDob.getYear() > LocalDate.now().getYear() + 10) {
                        parsedDob = parsedDob.minusYears(100);
                    }

                    isParsed = true;
                    break;
                } catch (Exception ignored) {
                    // Thử pattern tiếp theo
                }
            }

            if (!isParsed) {
                errors.add("Cột [Ngày sinh]: Sai định dạng (Dữ liệu gốc: '" + row.getBirthDate() + "')");
            } else if (parsedDob.isAfter(LocalDate.now())) {
                errors.add("Cột [Ngày sinh]: Không được lớn hơn ngày hiện tại");
            }
        }

        // Validate Năm tốt nghiệp
        Integer parsedGradYear = null;
        if (StringUtils.hasText(row.getGraduationYear())) {
            try {
                parsedGradYear = Integer.parseInt(row.getGraduationYear().trim());
                int currentYear = LocalDate.now().getYear();
                if (parsedGradYear < 1950 || parsedGradYear > currentYear + 6) {
                    errors.add("Cột [Năm tốt nghiệp]: Không hợp lý (từ 1950 đến " + (currentYear + 6) + ")");
                }
            } catch (NumberFormatException e) {
                errors.add("Cột [Năm tốt nghiệp]: Phải là một số nguyên");
            }
        }

        // Validate chiều dài các cột chuỗi khác
        if (StringUtils.hasText(row.getSchoolName()) && row.getSchoolName().trim().length() > 255) {
            errors.add("Cột [Tên trường]: Vượt quá 255 ký tự");
        }
        if (StringUtils.hasText(row.getProvince()) && row.getProvince().trim().length() > 100) {
            errors.add("Cột [Tỉnh/Thành]: Vượt quá 100 ký tự");
        }
        if (StringUtils.hasText(row.getAddress()) && row.getAddress().trim().length() > 500) {
            errors.add("Cột [Địa chỉ]: Vượt quá 500 ký tự");
        }
        if (StringUtils.hasText(row.getNote()) && row.getNote().trim().length() > 1000) {
            errors.add("Cột [Ghi chú]: Vượt quá 1000 ký tự");
        }

        // Ném lỗi nếu có (ImportValidationException phải nằm cùng thư mục)
        if (!errors.isEmpty()) {
            throw new ImportValidationException(String.join(" | ", errors));
        }


        if (existingPhones != null) {
            existingPhones.add(normalizedPhone);
        }

        Lead lead = new Lead();
        lead.setFullName(row.getFullName().trim());
        lead.setPhone(normalizedPhone);
        lead.setPhoneNormalized(normalizedPhone);
        lead.setEmail(StringUtils.hasText(row.getEmail()) ? row.getEmail().trim() : null);
        lead.setGender(parsedGender);
        lead.setBirthDate(parsedDob);
        lead.setGraduationYear(parsedGradYear);
        lead.setSchoolName(row.getSchoolName());
        lead.setProvince(row.getProvince());
        lead.setAddress(row.getAddress());
        lead.setNote(row.getNote());

        if (StringUtils.hasText(row.getSourceName())) {
            LeadSource src = sourceCache.computeIfAbsent(
                    row.getSourceName().trim(),
                    name -> leadSourceRepository.findBySourceName(name).orElse(null));
            lead.setSource(src);
        }

        LeadStatus defaultStatus = leadStatusRepository.findById(1L).orElse(null);
        if (defaultStatus != null) {
            lead.setStatus(defaultStatus);
        }

        return lead;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        String p = phone.trim().replaceAll("[^0-9+]", "");
        if (p.startsWith("+84")) {
            p = "0" + p.substring(3);
        } else if (p.startsWith("84") && p.length() == 11) {
            p = "0" + p.substring(2);
        } else if (p.length() == 9 && p.matches("^[35789]\\d{8}$")) {
            p = "0" + p;
        }
        return p;
    }
}