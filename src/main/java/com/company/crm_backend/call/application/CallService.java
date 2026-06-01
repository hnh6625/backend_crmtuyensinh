package com.company.crm_backend.call.application;

import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.infrastructure.UserRepository;
import com.company.crm_backend.call.domain.CallLog;
import com.company.crm_backend.call.domain.CallResult;
import com.company.crm_backend.call.domain.FollowUpSchedule;
import com.company.crm_backend.call.domain.FollowUpStatus;
import com.company.crm_backend.call.dto.CallLogResponse;
import com.company.crm_backend.call.dto.CreateCallLogRequest;
import com.company.crm_backend.call.dto.CreateFollowUpRequest;
import com.company.crm_backend.call.dto.FollowUpResponse;
import com.company.crm_backend.call.infrastructure.CallLogRepository;
import com.company.crm_backend.call.infrastructure.CallResultRepository;
import com.company.crm_backend.call.infrastructure.FollowUpRepository;
import com.company.crm_backend.lead.application.LeadHistoryService;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.infrastructure.LeadRepository;
import com.company.crm_backend.shared.exception.AppException;
import com.company.crm_backend.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CallService {

    private final CallLogRepository callLogRepository;
    private final CallResultRepository callResultRepository;
    private final FollowUpRepository followUpRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadHistoryService historyService;

    // Dropdown kết quả cuộc gọi
    @Transactional(readOnly = true)
    public List<CallResult> getCallResults() {
        return callResultRepository.findAll();
    }

    // Ghi nhận 1 cuộc gọi
    public CallLogResponse createCallLog(CreateCallLogRequest req, Long consultantId) {
        Lead lead = leadRepository.findByLeadIdAndDeletedAtIsNull(req.getLeadId())
                .orElseThrow(() -> new AppException(ErrorCode.LEAD_NOT_FOUND));

        User consultant = userRepository.findById(consultantId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CallResult result = callResultRepository.findById(req.getResultId())
                .orElseThrow(() -> new AppException(ErrorCode.CALL_RESULT_NOT_FOUND));

        // Kiểm tra đã gọi đủ 3 lần chưa
        long callCount = callLogRepository.countByLead_LeadId(req.getLeadId());
        if (callCount >= 3)
            throw new AppException(ErrorCode.CALL_ATTEMPT_EXCEEDED);

        int attemptNo = (int) callCount + 1;

        CallLog callLog = CallLog.builder()
                .lead(lead)
                .consultant(consultant)
                .result(result)
                .callAttemptNo(attemptNo)
                .durationSeconds(req.getDurationSeconds())
                .note(req.getNote())
                .build();

        callLogRepository.save(callLog);

        // Cập nhật thời gian gọi cuối trên lead
        leadRepository.updateLastCalledAt(req.getLeadId(), LocalDateTime.now());

        // Ghi lịch sử lead
        historyService.record(req.getLeadId(), "CALL_LOG",
                null,
                "Lần " + attemptNo + " - " + result.getResultName(),
                consultantId);

        log.info("CallLog: leadId={}, attempt={}, result={}",
                req.getLeadId(), attemptNo, result.getResultName());

        return CallLogResponse.from(callLog);
    }

    // Lấy lịch sử gọi của 1 lead
    @Transactional(readOnly = true)
    public List<CallLogResponse> getCallsByLead(Long leadId) {
        return callLogRepository
                .findAllByLead_LeadIdOrderByCalledAtDesc(leadId)
                .stream().map(CallLogResponse::from).toList();
    }

    // Lấy lịch sử gọi của consultant — có phân trang
    @Transactional(readOnly = true)
    public Page<CallLogResponse> getCallsByConsultant(Long consultantId,
                                                      Pageable pageable) {
        return callLogRepository
                .findAllByConsultant_UserIdOrderByCalledAtDesc(consultantId, pageable)
                .map(CallLogResponse::from);
    }

    // Tạo lịch hẹn follow-up
    public FollowUpResponse createFollowUp(CreateFollowUpRequest req,
                                           Long consultantId) {
        Lead lead = leadRepository.findByLeadIdAndDeletedAtIsNull(req.getLeadId())
                .orElseThrow(() -> new AppException(ErrorCode.LEAD_NOT_FOUND));

        User consultant = userRepository.findById(consultantId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        FollowUpSchedule schedule = FollowUpSchedule.builder()
                .lead(lead)
                .consultant(consultant)
                .scheduledAt(req.getScheduledAt())
                .note(req.getNote())
                .build();

        followUpRepository.save(schedule);

        // Cập nhật next_follow_up_at trên lead
        leadRepository.updateNextFollowUpAt(req.getLeadId(), req.getScheduledAt());

        // Ghi lịch sử lead
        historyService.record(req.getLeadId(), "FOLLOW_UP_SCHEDULED",
                null, "Hẹn lúc " + req.getScheduledAt(), consultantId);

        return FollowUpResponse.from(schedule);
    }

    // Lấy lịch hẹn PENDING của consultant hiện tại
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getMyPendingFollowUps(Long consultantId) {
        return followUpRepository
                .findAllByConsultant_UserIdAndStatusOrderByScheduledAtAsc(
                        consultantId, FollowUpStatus.PENDING)
                .stream().map(FollowUpResponse::from).toList();
    }

    // Lấy tất cả lịch hẹn của 1 lead
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUpsByLead(Long leadId) {
        return followUpRepository
                .findAllByLead_LeadIdOrderByScheduledAtDesc(leadId)
                .stream().map(FollowUpResponse::from).toList();
    }

    // Cập nhật trạng thái lịch hẹn
    public FollowUpResponse updateFollowUpStatus(Long scheduleId,
                                                 FollowUpStatus newStatus) {
        FollowUpSchedule schedule = followUpRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLLOW_UP_NOT_FOUND));

        // Chỉ cho phép cập nhật khi đang PENDING
        if (schedule.getStatus() != FollowUpStatus.PENDING)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);

        schedule.setStatus(newStatus);
        return FollowUpResponse.from(followUpRepository.save(schedule));
    }
}