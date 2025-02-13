package com.example.demo.controller;

import com.example.demo.service.CsvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv")
public class CsvController {
    private static final Logger log = LoggerFactory.getLogger(CsvController.class);
    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMultipartFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request.");

        try {
            log.info("Starting to process the CSV file...");
            csvService.uploadMultipartFile(file);
            log.info("CSV file processed successfully and data persisted.");
            return new ResponseEntity<>("CSV file uploaded and data persisted successfully!", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while uploading or processing CSV file: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error while uploading CSV file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
