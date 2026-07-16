package com.example.excelvalidator.controller;

import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.model.response.BatchValidationResponse;
import com.example.excelvalidator.service.ExcelValidationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelValidationService service;

    public ExcelController(ExcelValidationService service){
        this.service = service;
    }
    @PostMapping(
            value = "/validate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ExcelValidationResponse> validateExcel(
            @RequestParam("file") MultipartFile excelFile,
            @RequestParam("rules") MultipartFile rulesFile,
            @RequestParam("fileType") String fileType
    ) throws Exception {
        validateUploadFile(excelFile);

        return ResponseEntity.ok(service.validate(excelFile, rulesFile, fileType));
    }

    @PostMapping(
            value = "/validate-batch",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BatchValidationResponse> validateBatch(
            @RequestParam("excelFiles")
            List<MultipartFile> excelFiles,

            @RequestParam("rulesFile")
            MultipartFile rulesFile
    ) {
        if (excelFiles == null || excelFiles.isEmpty()) {
            throw new IllegalArgumentException("Please upload at least one Excel file.");
        }

        if (rulesFile == null || rulesFile.isEmpty()) {
            throw new IllegalArgumentException("Please upload a rules file.");
        }

        return ResponseEntity.ok(service.validateBatch(excelFiles, rulesFile));
    }

    private void validateUploadFile(MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new IllegalArgumentException(
                    "Please upload a non=empty Excel file."
            );
        }

        String fileName = file.getOriginalFilename();

        if(fileName == null || fileName.isBlank()){
            throw new IllegalArgumentException(
                    "The uploaded file must have a name."
            );
        }

        String normalizedFileName = fileName.toLowerCase(Locale.ROOT);

        boolean isExcelFile = normalizedFileName.endsWith(".xlsx") || normalizedFileName.endsWith(".xls");

        if(!isExcelFile){
            throw new IllegalArgumentException(
                    "Only .xlsx and .xls files are allowed"
            );
        }
    }
}
