package com.itf.TagAlongService.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitationDto {
    private List<ExtractedRegionDto> extractedRegions;
}

