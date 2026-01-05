package com.aura.clinician.service;

import com.aura.clinician.Entities.ClinicianFeedbackEntity;
import com.aura.clinician.Entities.ModelRegistryEntity;
import com.aura.clinician.Entities.ReviewQueueRecordEntity;
import com.aura.clinician.Enums.ClinicianFinalDecision;
import com.aura.clinician.Enums.TrainingStatus;
import com.aura.clinician.api.dto.FilterDtos.DashboardReviewNeedFilterDto;
import com.aura.clinician.api.dto.RequestDtos.ClinicianFeedbackRequestDto;
import com.aura.clinician.api.dto.RequestDtos.ModelTrainingCallbackRequestDto;
import com.aura.clinician.api.dto.RequestDtos.RetrainRequestDto;
import com.aura.clinician.api.dto.ResponseDtos.*;
import com.aura.clinician.api.dto.RetrainCaseDto;
import com.aura.clinician.api.dto.ScoreDto;
import com.aura.clinician.exception.BadRequestException;
import com.aura.clinician.exception.InternalServerErrorException;
import com.aura.clinician.exception.NotFoundException;
import com.aura.clinician.repository.ClinicianFeedbackRepository;
import com.aura.clinician.repository.ModelRegistryRepository;
import com.aura.clinician.repository.ReviewQueueRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewDashboardService {
    private final ReviewQueueRepository reviewQueueRepository;
    private final ClinicianFeedbackRepository clinicianFeedbackRepository;
    private final ModelRegistryRepository modelRegistryRepository;
    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;

    @Value("${python.service.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    public ReviewQueueResponseDto getReviewQueueRecords(DashboardReviewNeedFilterDto filterDto, Pageable pageable) {

        long start = System.currentTimeMillis();

        try {
            log.info("HIT | ReviewDashboardService#getReviewQueueRecords | filterDto={} | pageable={}",
                    filterDto, pageable);

            Page<ReviewQueueRecordEntity> page =
                    reviewQueueRepository.findReviewQueueNeedReview(filterDto, pageable);

            List<ReviewQueueItemResponseDto> items = page.getContent().stream()
                    .map(this::mapToDto)
                    .toList();

            ReviewQueueResponseDto response = ReviewQueueResponseDto.builder()
                    .records(items)
                    .totalRecords(page.getTotalElements())
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalPages(page.getTotalPages())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .build();

            log.info("SUCCESS | ReviewDashboardService#getReviewQueueRecords | returned={} | total={} | tookMs={}",
                    items.size(), page.getTotalElements(), (System.currentTimeMillis() - start));

            return response;

        } catch (IllegalArgumentException e) {
            log.warn("BAD_REQUEST | ReviewDashboardService#getReviewQueueRecords | filterDto={} | pageable={} | msg={}",
                    filterDto, pageable, e.getMessage(), e);
            throw new BadRequestException("Invalid filters or paging parameters.");

        } catch (DataAccessException e) {

            log.error("DB_ERROR | ReviewDashboardService#getReviewQueueRecords | filterDto={} | pageable={}",
                    filterDto, pageable, e);
            throw new InternalServerErrorException("Database error while fetching review queue records.");

        } catch (Exception e) {

            log.error("UNEXPECTED_ERROR | ReviewDashboardService#getReviewQueueRecords | filterDto={} | pageable={}",
                    filterDto, pageable, e);
            throw new InternalServerErrorException("Unexpected error while fetching review queue records.");
        }
    }

    public ReviewQueueItemResponseDto getReviewRecordDetailsByCaseId(String caseId) {

        long start = System.currentTimeMillis();

        try {
            if (caseId == null || caseId.isBlank()) {
                throw new IllegalArgumentException("caseId is required");
            }

            String trimmedCaseId = caseId.trim();
            log.info("HIT | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={}", trimmedCaseId);

            ReviewQueueRecordEntity record =
                    reviewQueueRepository.findReviewRecordByCaseId(trimmedCaseId);

            if (record == null) {
                log.warn("NOT_FOUND | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={}", trimmedCaseId);
                throw new NotFoundException("Review record not found for caseId: " + trimmedCaseId);
            }

            ReviewQueueItemResponseDto dto = mapToDto(record);

            log.info("SUCCESS | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={} | tookMs={}",
                    trimmedCaseId, (System.currentTimeMillis() - start));

            return dto;

        }catch (NotFoundException e) {
            log.warn("NOT_FOUND | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={} | msg={}",
                    caseId, e.getMessage(), e);
            throw new NotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("BAD_REQUEST | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={} | msg={}",
                    caseId, e.getMessage(), e);
            throw new BadRequestException(e.getMessage());

        } catch (DataAccessException e) {
            log.error("DB_ERROR | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={}", caseId, e);
            throw new InternalServerErrorException("Database error while fetching review record details.");

        } catch (Exception e) {
            log.error("UNEXPECTED_ERROR | ReviewDashboardService | getReviewRecordDetailsByCaseId | caseId={}", caseId, e);
            throw new InternalServerErrorException("Unexpected error while fetching review record details.");
        }
    }

    public CommonResponseDto submitClinicianFeedback(
            String caseId,
            @Valid ClinicianFeedbackRequestDto request
    ) {
        try {
            long start = System.currentTimeMillis();

            if (!StringUtils.hasText(caseId)) {
                throw new IllegalArgumentException("caseId is required");
            }

            if (request == null || request.getClinicianFinalDecision() == null) {
                throw new IllegalArgumentException("clinicianFinalDecision is required");
            }

            if (request.getClinicianFinalDecision() == ClinicianFinalDecision.CORRECTED) {
                if (!StringUtils.hasText(request.getCorrectedDrug())
                        || !StringUtils.hasText(request.getCorrectedStep())) {
                    throw new IllegalArgumentException("For CORRECTED decision, correctedDrug and correctedStep are required");
                }
            }

            if (clinicianFeedbackRepository.existsByCaseId(caseId)) {
                throw new IllegalStateException("Feedback already submitted for caseId=" + caseId);
            }

            Instant now = Instant.now();

            ClinicianFeedbackEntity entity = ClinicianFeedbackEntity.builder()
                    .caseId(caseId)
                    .patientAge(request.getPatientAge())
                    .patientGender(request.getPatientGender())
                    .uct(request.getUct())
                    .aect(request.getAect())
                    .predictedDrug(request.getPredictedDrug())
                    .predictedStep(request.getPredictedStep())
                    .confidencePredictedDrugStep(request.getConfidencePredictedDrugStep())
                    .clinicianFinalDecision(request.getClinicianFinalDecision())
                    .comment(request.getComment())
                    .reviewedBy(StringUtils.hasText(request.getReviewedBy()) ? request.getReviewedBy() : "CLINICIAN")
                    .createdAt(now)
                    .updatedAt(now)
                    .trained(false)
                    .build();

            if (request.getClinicianFinalDecision() == ClinicianFinalDecision.CORRECTED) {
                entity.setCorrectedDrug(request.getCorrectedDrug());
                entity.setCorrectedStep(request.getCorrectedStep());
            }

            clinicianFeedbackRepository.save(entity);
            log.info("SUCCESS | ReviewDashboardService | submitClinicianFeedback | caseId={} | tookMs={}",
                    caseId, (System.currentTimeMillis() - start));

            return CommonResponseDto.builder()
                    .success(true)
                    .message("Feedback saved")
                    .build();

        } catch (IllegalArgumentException ex) {
            log.warn("submitClinicianFeedback validation failed | caseId={} | error={}", caseId, ex.getMessage());
            return CommonResponseDto.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build();

        } catch (IllegalStateException ex) {
            log.warn("submitClinicianFeedback conflict | caseId={} | error={}", caseId, ex.getMessage());
            return CommonResponseDto.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build();

        } catch (Exception ex) {
            log.error("submitClinicianFeedback unexpected error | caseId={}", caseId, ex);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Unexpected error occurred")
                    .build();
        }
    }

    public CommonResponseDto clinicianFeedbackTrainModel() {

        // 1) Pull all untrained
        List<ClinicianFeedbackEntity> untrained = clinicianFeedbackRepository.findByTrainedFalse();

        if (CollectionUtils.isEmpty(untrained)) {
            return CommonResponseDto.builder()
                    .success(true)
                    .message("No untrained clinician feedback records found")
                    .build();
        }

        // 2) Convert into retrain cases (filter out decisions you don’t want to train on)
        List<RetrainCaseDto> cases = untrained.stream()
                .filter(this::isTrainableDecision)
                .map(this::toRetrainCase)
                .collect(Collectors.toList());

        if (cases.isEmpty()) {
            return CommonResponseDto.builder()
                    .success(true)
                    .message("Untrained records found, but none are eligible for training (only ACCEPTED/CORRECTED are used)")
                    .build();
        }

        RetrainRequestDto payload = RetrainRequestDto.builder()
                .min_new(1)
                .force(true)
                .cases(cases)
                .build();

        // 3) Call python /retrain
        String url = pythonBaseUrl + "/retrain";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RetrainRequestDto> httpEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<RetrainResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    RetrainResponseDto.class
            );

            //Check success
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Python retrain failed | status={} | body={}", response.getStatusCode(), response.getBody());
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Python retrain failed with status: " + response.getStatusCode())
                        .build();
            }

            // 5) Mark as trained=true
            List<String> trainedIds = untrained.stream()
                    .filter(this::isTrainableDecision)
                    .map(ClinicianFeedbackEntity::getId)
                    .collect(Collectors.toList());

            RetrainResponseDto pythonResp = response.getBody();

            if (pythonResp == null) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Python retrain response body is empty")
                        .build();
            }

            log.info("Python retrain response: {}", pythonResp);


            if (!"STARTED".equalsIgnoreCase(pythonResp.getStatus().toString())) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Python retrain not started. status=" + pythonResp.getStatus())
                        .build();
            }

            // Create model registry record (RETRAINING)
            createModelRegistryRecordIfNotExists(pythonResp.getVersion(), trainedIds.size());

            // Mark feedback rows trained
            bulkMarkTrained(trainedIds);

            return CommonResponseDto.builder()
                    .success(true)
                    .message("Retrain triggered successfully. Marked " + trainedIds.size() + " records as trained.")
                    .build();

        } catch (Exception ex) {
            log.error("Error calling python retrain service | url={}", url, ex);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error calling python retrain service: " + ex.getMessage())
                    .build();
        }
    }


    public CommonResponseDto handleTrainingCallback(ModelTrainingCallbackRequestDto request) {

        try {
            // 1) Validate
            if (request == null) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Request body is required")
                        .build();
            }
            if (!StringUtils.hasText(request.getVersion())) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("version is required")
                        .build();
            }
            if (!StringUtils.hasText(request.getStatus())) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("status is required")
                        .build();
            }
            if (!StringUtils.hasText(request.getMessage())) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("message is required")
                        .build();
            }

            String version = request.getVersion().trim();

            // 2) Find or create
            ModelRegistryEntity entity = modelRegistryRepository.findByModelVersion(version)
                    .orElseGet(() -> ModelRegistryEntity.builder()
                            .modelVersion(version)
                            .createdAt(Instant.now())
                            .trainedCases(0)
                            .status(TrainingStatus.RETRAINING)
                            .promoted(false)
                            .build()
                    );

            // 3) Map callback -> internal status
            TrainingStatus mappedStatus = mapCallbackToTrainingStatus(request.getStatus(), request.getMessage());

            // 4) Update fields
            entity.setStatus(mappedStatus);
            entity.setCallbackUpdatedAt(Instant.now());

            // store metrics if provided
            if (request.getMetrics() != null && !request.getMetrics().isEmpty()) {
                entity.setMetrics(request.getMetrics());
            }

            // promoted flag based on callback status
            boolean promoted = "promoted".equalsIgnoreCase(request.getStatus());
            entity.setPromoted(promoted);

            // OPTIONAL: you can also store reason/message somewhere
            // If you want, add fields in entity: lastMessage, lastReason

            modelRegistryRepository.save(entity);

            log.info("Model training callback processed | version={} | status={} | promoted={}",
                    version, entity.getStatus(), entity.isPromoted());

            return CommonResponseDto.builder()
                    .success(true)
                    .message("Callback processed for version=" + version)
                    .build();

        } catch (Exception ex) {
            log.error("Error processing training callback", ex);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Unexpected error occurred")
                    .build();
        }
    }

    public ModelRegistryListResponseDto getAllModelRegistryDetails() {

        // sort newest first
        List<ModelRegistryEntity> entities = modelRegistryRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<ModelRegistryItemResponseDto> records = entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ModelRegistryListResponseDto.builder()
                .records(records)
                .totalRecords(records.size())
                .build();
    }

    public ModelRegistryItemResponseDto getModelRegistryDetailsById(String id) {

        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("model registry id is required");
        }

        ModelRegistryEntity entity = modelRegistryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model registry record not found for id=" + id));

        return toDto(entity);
    }

    private TrainingStatus mapCallbackToTrainingStatus(String status, String message) {
        String s = status == null ? "" : status.toLowerCase(Locale.ROOT);
        String m = message == null ? "" : message.toLowerCase(Locale.ROOT);

        // status: promoted | not_promoted | skipped | error
        // message: training_complete | training_failed

        if ("error".equals(s) || "training_failed".equals(m)) {
            return TrainingStatus.FAILED;
        }
        if ("skipped".equals(s)) {
            return TrainingStatus.SKIPPED;
        }
        if ("promoted".equals(s)) {
            return TrainingStatus.DEPLOYED; // promoted implies deployed
        }
        if ("not_promoted".equals(s) && "training_complete".equals(m)) {
            return TrainingStatus.READY;
        }

        // default safe fallback
        return TrainingStatus.READY;
    }

    private boolean isTrainableDecision(ClinicianFeedbackEntity e) {
        if (e.getClinicianFinalDecision() == null) return false;
        return e.getClinicianFinalDecision() == ClinicianFinalDecision.ACCEPTED
                || e.getClinicianFinalDecision() == ClinicianFinalDecision.CORRECTED;
    }

    private RetrainCaseDto toRetrainCase(ClinicianFeedbackEntity e) {
        RetrainCaseDto dto = RetrainCaseDto.builder()
                .patientAge(e.getPatientAge())
                .gender(e.getPatientGender() == null ? null : e.getPatientGender().toLowerCase())
                .uct(ScoreDto.builder().totalScore(e.getUct() != null ? e.getUct().getTotalScore() : null).build())
                .aect(ScoreDto.builder().totalScore(e.getAect() != null ? e.getAect().getTotalScore() : null).build())
                .confidencePredictedDrugStep(e.getConfidencePredictedDrugStep())
                .predictedDrug(e.getPredictedDrug())
                .predictedStep(e.getPredictedStep())
                // Map clinician final decision to python field name
                .adminDecision(e.getClinicianFinalDecision().name())
                .correctedDrug(e.getCorrectedDrug())
                .correctedStep(e.getCorrectedStep())
                .build();

        return dto;
    }

    private void bulkMarkTrained(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) return;

        Query query = new Query(Criteria.where("_id").in(ids));
        Update update = new Update()
                .set("trained", true)
                .set("updatedAt", Instant.now());

        mongoTemplate.updateMulti(query, update, ClinicianFeedbackEntity.class);
    }

    private ReviewQueueItemResponseDto mapToDto(ReviewQueueRecordEntity e) {
        return ReviewQueueItemResponseDto.builder()
                .id(e.getId())
                .caseId(e.getCaseId())
                .patientAge(e.getPatientAge())
                .patientGender(e.getPatientGender())
                .hospital(e.getHospital())
                .visitDate(e.getVisitDate())
                .symptoms(e.getSymptoms())
                .urticariaType(e.getUrticariaType())
                .uct(e.getUct())
                .aect(e.getAect())
                .shapAvailable(e.getShapAvailable())
                .gradCamAvailable(e.getGradCamAvailable())
                .gradCamHeatMapImage(e.getGradCamHeatMapImage())
                .images(e.getImages())
                .shapScores(e.getShapScores())
                .overallConfidenceScore(e.getOverallConfidenceScore())
                .risks(e.getRisks())
                .predictedDrug(e.getPredictedDrug())
                .predictedStep(e.getPredictedStep())
                .confidencePredictedDrugStep(e.getConfidencePredictedDrugStep())
                .recommendations(e.getRecommendations())
                .clinicianFinalStatus(e.getClinicianFinalStatus())
                .comment(e.getComment())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private void createModelRegistryRecordIfNotExists(String version, int trainedCases) {
        if (!StringUtils.hasText(version)) {
            throw new IllegalArgumentException("Python response model version is missing");
        }

        if (modelRegistryRepository.existsByModelVersion(version)) {
            // Avoid duplicates if user clicks twice
            return;
        }

        ModelRegistryEntity entity = ModelRegistryEntity.builder()
                .modelVersion(version)
                .status(TrainingStatus.RETRAINING)
                .createdAt(Instant.now())
                .trainedCases(trainedCases)
                .metrics(null)
                .featureColumns(null)
                .callbackUpdatedAt(null)
                .promoted(false)
                .build();

        modelRegistryRepository.save(entity);
    }

    private ModelRegistryItemResponseDto toDto(ModelRegistryEntity e) {
        return ModelRegistryItemResponseDto.builder()
                .id(e.getId())
                .modelVersion(e.getModelVersion())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .trainedCases(e.getTrainedCases())
                .metrics(e.getMetrics())
                .featureColumns(e.getFeatureColumns())
                .callbackUpdatedAt(e.getCallbackUpdatedAt())
                .promoted(e.isPromoted())
                .build();
    }

}

