package com.aura.clinician.api.dto.ResponseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelRegistryListResponseDto {
    private List<ModelRegistryItemResponseDto> records;
    private long totalRecords;
}
