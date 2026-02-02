package com.itf.TagAlongService.controller;

import com.itf.TagAlongService.dto.CreateUserDto;
import com.itf.TagAlongService.dto.AuthenticationSuccessResponse;
import com.itf.TagAlongService.dto.LoginDto;
import com.itf.TagAlongService.dto.QrBarcodeResponse;
import com.itf.TagAlongService.dto.BarcodeDataDto;
import com.itf.TagAlongService.dto.CitationDto;
import com.itf.TagAlongService.dto.ExtractedRegionDto;
import com.itf.TagAlongService.dto.CoordinatesDto;
import com.itf.TagAlongService.model.TagAlongUser;
import com.itf.TagAlongService.service.AuthenticationService;
import com.itf.TagAlongService.util.QrDetectionUtil;
import lombok.AllArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationSuccessResponse> authenticate(@RequestBody LoginDto loginDto){
        String token = authenticationService.login(loginDto);

        AuthenticationSuccessResponse jwtAuthResponse = new AuthenticationSuccessResponse();
        jwtAuthResponse.setAccessToken(token);

        return ResponseEntity.ok(jwtAuthResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<TagAlongUser> signIn(@RequestBody CreateUserDto createUserDto){
        TagAlongUser token = authenticationService.createUser(createUserDto);

        return ResponseEntity.ok(token);
    }

    @GetMapping("/extract-qr-data")
    public ResponseEntity<QrBarcodeResponse> extractQrDataFromPdf() {
        try {
            // Load the PDF from src/main/resources/input/test_qr_pdf.pdf
            ClassPathResource resource = new ClassPathResource("input/test_qr_pdf.pdf");
            try (InputStream pdfStream = resource.getInputStream()) {
                // Detect all QR codes in the PDF
                List<QrDetectionUtil.QrCodeData> qrDataList = QrDetectionUtil.detectQrCodesInPdf(pdfStream);

                // Convert to response DTOs
                List<BarcodeDataDto> barcodeDataList = new ArrayList<>();
                for (QrDetectionUtil.QrCodeData qrData : qrDataList) {
                    CoordinatesDto coordinates = new CoordinatesDto(
                            qrData.getTopX(),
                            qrData.getTopY(),
                            qrData.getBottomX(),
                            qrData.getBottomY()
                    );

                    ExtractedRegionDto region = new ExtractedRegionDto(
                            coordinates,
                            qrData.getPageNumber(),
                            qrData.getOcrConfidence()
                    );

                    BarcodeDataDto barcode = new BarcodeDataDto(
                            qrData.getFieldId(),
                            qrData.getFieldName(),
                            qrData.getFieldValue(),
                            qrData.getFieldType(),
                            new CitationDto(Collections.singletonList(region))
                    );

                    barcodeDataList.add(barcode);
                }

                QrBarcodeResponse response = new QrBarcodeResponse(barcodeDataList);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new QrBarcodeResponse(Collections.emptyList()));
        }
    }
    }


