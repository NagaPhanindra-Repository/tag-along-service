package com.itf.TagAlongService.dto;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BarcodeDataDto {
    private String fieldId;
    private String fieldName;
    private String fieldValue;
    private String fieldType;
    private CitationDto citation;
}



