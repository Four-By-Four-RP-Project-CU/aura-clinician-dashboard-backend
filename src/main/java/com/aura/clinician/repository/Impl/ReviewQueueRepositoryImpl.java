package com.aura.clinician.repository.Impl;

import com.aura.clinician.Entities.ReviewQueueRecordEntity;
import com.aura.clinician.Enums.FinalStatus;
import com.aura.clinician.api.dto.FilterDtos.DashboardReviewNeedFilterDto;
import com.aura.clinician.repository.ReviewQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewQueueRepositoryImpl implements ReviewQueueRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ReviewQueueRecordEntity> findReviewQueueNeedReview(DashboardReviewNeedFilterDto filter, Pageable pageable) {

        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        // Always only NEED_REVIEW
        criteria.add(Criteria.where("clinicianFinalStatus").is(FinalStatus.NEED_REVIEW));

        // Filter out finalStatus that are ACCEPTED, REJECTED, or CORRECTED
        criteria.add(Criteria.where("finalStatus").nin(FinalStatus.ACCEPTED, FinalStatus.REJECTED, FinalStatus.CORRECTED));

        // Uncertainty filter
        if (filter != null && filter.getUncertaintyLevel() != null) {
            switch (filter.getUncertaintyLevel()) {
                case HIGH -> criteria.add(Criteria.where("overallConfidenceScore").lt(0.40));
                case MEDIUM ->
                    criteria.add(Criteria.where("overallConfidenceScore").gte(0.40).lt(0.70));

                case LOW -> criteria.add(Criteria.where("overallConfidenceScore").gte(0.70));

            }
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria));
        }

        // Sorting rules
        Sort sort = buildSort(filter, pageable);
        query.with(sort);

        // Count first (before skip/limit)
        long total = mongoTemplate.count(query, ReviewQueueRecordEntity.class);

        // Pagination
        query.with(pageable);

        List<ReviewQueueRecordEntity> results = mongoTemplate.find(query, ReviewQueueRecordEntity.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public ReviewQueueRecordEntity findReviewRecordByCaseId(String caseId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("caseId").is(caseId));
        query.addCriteria(Criteria.where("clinicianFinalStatus").is(FinalStatus.NEED_REVIEW));

        return mongoTemplate.findOne(query, ReviewQueueRecordEntity.class);
    }

    private Sort buildSort(DashboardReviewNeedFilterDto filter, Pageable pageable) {
        if (filter != null && filter.isLowestConfidenceFirst()) {
            // lowest confidence first
            return Sort.by(Sort.Direction.ASC, "overallConfidenceScore")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        if (filter != null && filter.isLatestFirst()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        // fallback to whatever Pageable has (your @PageableDefault)
        return pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
    }

}
