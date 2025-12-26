package com.aura.clinician.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aura.clinician.api.dto.DashboardResponse;
import com.aura.clinician.api.dto.FeedbackRequest;
import com.aura.clinician.domain.FeedbackEntry;
import com.aura.clinician.repository.FeedbackRepository;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final DashboardService dashboardService;
    private final AuditService auditService;

    public FeedbackService(
        FeedbackRepository feedbackRepository,
        DashboardService dashboardService,
        AuditService auditService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.dashboardService = dashboardService;
        this.auditService = auditService;
    }

    public DashboardResponse submitFeedback(FeedbackRequest request) {
        FeedbackEntry entry = new FeedbackEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setCaseId(request.getCaseId());
        entry.setDiseaseType(request.getDiseaseType());
        entry.setClinicianId(request.getClinicianId());
        entry.setAction(request.getAction());
        entry.setComment(request.getComment());
        entry.setTimestamp(Instant.now());
        feedbackRepository.append(entry);

        auditService.logFeedback(request.getCaseId(), request.getDiseaseType(), request.getClinicianId(), request.getAction());

        DashboardResponse response = dashboardService.getDashboard(
            request.getCaseId(),
            request.getDiseaseType(),
            request.getClinicianId()
        );
        response.setFeedbackStatus("RECEIVED");
        return response;
    }
}
