package com.aura.clinician.api.dto.ResponseDtos;

import com.aura.clinician.utils.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQueueResponseDto extends PaginatedResponse {
    private List<ReviewQueueItemResponseDto> records;
}
