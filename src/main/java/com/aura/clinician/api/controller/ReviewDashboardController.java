package com.aura.clinician.api.controller;

import com.aura.clinician.api.dto.FilterDtos.DashboardReviewNeedFilterDto;
import com.aura.clinician.api.dto.RequestDtos.ClinicianFeedbackRequestDto;
import com.aura.clinician.api.dto.RequestDtos.ModelTrainingCallbackRequestDto;
import com.aura.clinician.api.dto.ResponseDtos.*;
import com.aura.clinician.service.ReviewDashboardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard-review")
@Validated
@AllArgsConstructor
@Slf4j
public class ReviewDashboardController {

    @Autowired
    private ReviewDashboardService reviewDashboardService;

    @GetMapping
    public ReviewQueueResponseDto getReviewQueueRecords(@ModelAttribute DashboardReviewNeedFilterDto filterDto,
                                                        @PageableDefault(size = 10,sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("HIT | /api/v1/dashboard-review | GET | getReviewQueueRecords | filterDto: {} | pageable : {} ", filterDto, pageable);
        return reviewDashboardService.getReviewQueueRecords(filterDto,pageable);
    }

    @GetMapping("/{caseId}")
    public ReviewQueueItemResponseDto getReviewRecordDetailsByCaseId(@PathVariable String caseId) {
        log.info("HIT | /api/v1/dashboard-review/{} | GET | getReviewQueueRecords | get Details For Case Id : {} ", caseId, caseId);
        return reviewDashboardService.getReviewRecordDetailsByCaseId(caseId);
    }

    @PostMapping("/{caseId}/clinician-feedback")
    public CommonResponseDto submitClinicianFeedback(
            @PathVariable String caseId,
            @Valid @RequestBody ClinicianFeedbackRequestDto request
    ) {
        log.info("HIT | /api/v1/dashboard-review/{}/clinician-feedback | POST | submitClinicianFeedback | request : {} ", caseId,request);
        return reviewDashboardService.submitClinicianFeedback(caseId, request);
    }

    @PostMapping("/clinician-feedback-train-model")
    public CommonResponseDto clinicianFeedbackTrainModel() {
        log.info("HIT | /api/v1/dashboard-review/clinician-feedback-train-model | POST | clinicianFeedbackTrainModel ");
        return reviewDashboardService.clinicianFeedbackTrainModel();
    }

    @PostMapping("/training-callback")
    public CommonResponseDto trainingCallback(
            @RequestBody @Valid ModelTrainingCallbackRequestDto request
    ) {
        log.info("HIT | /api/v1/models/training-callback | POST | version={} | status={} | message={}",
                request.getVersion(), request.getStatus(), request.getMessage());

        return reviewDashboardService.handleTrainingCallback(request);
    }

    @GetMapping("/model-registry")
    public ModelRegistryListResponseDto getAllModelRegistryDetails() {
        log.info("HIT | /api/v1/dashboard-review/model-registry | GET | getAllModelRegistryDetails");
        return reviewDashboardService.getAllModelRegistryDetails();
    }

    @GetMapping("/model-registry/{id}")
    public ModelRegistryItemResponseDto getModelRegistryDetailsById(@PathVariable String id) {
        log.info("HIT | /api/v1/dashboard-review/model-registry/{} | GET | getModelRegistryDetailsById", id);
        return reviewDashboardService.getModelRegistryDetailsById(id);
    }

    @GetMapping("/insights")
    public DashboardInsightsResponseDto getDashboardInsights() {
        log.info("HIT | /api/v1/dashboard-review/insights | GET | getDashboardInsights");
        return reviewDashboardService.getDashboardInsights();
    }

    @GetMapping("/insights/retraining-coverage")
    public DashboardRetrainingCoverageResponseDto getRetrainingCoverage() {
        log.info("HIT | /api/v1/dashboard-review/insights/retraining-coverage | GET | getRetrainingCoverage");
        return reviewDashboardService.getDashboardInsights().getRetrainingCoverage();
    }

    @GetMapping("/insights/dataset-readiness")
    public DashboardDatasetReadinessResponseDto getDatasetReadiness() {
        log.info("HIT | /api/v1/dashboard-review/insights/dataset-readiness | GET | getDatasetReadiness");
        return reviewDashboardService.getDashboardInsights().getDatasetReadiness();
    }

    @GetMapping("/insights/recent-activity")
    public DashboardRecentActivityResponseDto getRecentActivity() {
        log.info("HIT | /api/v1/dashboard-review/insights/recent-activity | GET | getRecentActivity");
        return reviewDashboardService.getDashboardInsights().getRecentActivity();
    }

    @GetMapping("/insights/label-coverage")
    public DashboardLabelCoverageResponseDto getLabelCoverage() {
        log.info("HIT | /api/v1/dashboard-review/insights/label-coverage | GET | getLabelCoverage");
        return reviewDashboardService.getDashboardInsights().getLabelCoverage();
    }

    @GetMapping("/insights/redeployment-status")
    public DashboardRedeploymentStatusResponseDto getRedeploymentStatus() {
        log.info("HIT | /api/v1/dashboard-review/insights/redeployment-status | GET | getRedeploymentStatus");
        return reviewDashboardService.getRedeploymentStatus();
    }

    @GetMapping("/insights/current-deployed-model")
    public DashboardCurrentDeployedModelResponseDto getCurrentDeployedModelDetails() {
        log.info("HIT | /api/v1/dashboard-review/insights/current-deployed-model | GET | getCurrentDeployedModelDetails");
        return reviewDashboardService.getCurrentDeployedModelDetails();
    }

}
