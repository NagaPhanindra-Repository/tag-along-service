package com.itf.TagAlongService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractedRegionDto {
    private CoordinatesDto coordinates;
    private int pageNumber;
    private double ocrConfidence;
}

