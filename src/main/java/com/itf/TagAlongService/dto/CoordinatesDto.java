package com.itf.TagAlongService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinatesDto {
    private double topX;
    private double topY;
    private double bottomX;
    private double bottomY;
}

