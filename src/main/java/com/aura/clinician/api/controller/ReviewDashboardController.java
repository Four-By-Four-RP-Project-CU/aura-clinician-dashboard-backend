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




}

