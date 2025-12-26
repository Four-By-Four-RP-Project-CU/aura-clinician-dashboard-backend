package com.aura.clinician.api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.clinician.api.dto.DashboardResponse;
import com.aura.clinician.api.dto.FeedbackRequest;
import com.aura.clinician.service.FeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/feedback")
@Validated
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public DashboardResponse submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        return feedbackService.submitFeedback(request);
    }
}
