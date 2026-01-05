package com.aura.clinician.repository;

import com.aura.clinician.Entities.ReviewQueueRecordEntity;
import com.aura.clinician.api.dto.FilterDtos.DashboardReviewNeedFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueueRepository {
    Page<ReviewQueueRecordEntity> findReviewQueueNeedReview(DashboardReviewNeedFilterDto filterDto, Pageable pageable);

    ReviewQueueRecordEntity findReviewRecordByCaseId(String caseId);
}
