package com.example.demo.controller;

import com.example.demo.exception.CsvProcessingException;
import com.example.demo.exception.FileProcessingException;
import com.example.demo.service.CsvService;
import com.opencsv.exceptions.CsvException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/csv")
public class CsvController {
  private static final Logger log = LoggerFactory.getLogger(CsvController.class);
  private final CsvService csvService;

  public CsvController(CsvService csvService) {
    this.csvService = csvService;
  }

  @PostMapping("/upload")
  public ResponseEntity<String> uploadMultipartFile(@RequestParam("file") @Valid @NotEmpty MultipartFile file) {
    log.info("Received file upload request.");

    try {
      csvService.uploadMultipartFile(file);
      log.info("CSV file processed successfully and data persisted.");
      return new ResponseEntity<>("CSV file uploaded and data persisted successfully!", HttpStatus.OK);
    } catch (IOException e) {
      throw new FileProcessingException("Error processing file " + e.getMessage());
    } catch (CsvException e) {
      throw new CsvProcessingException("Error processing CSV file: " + e.getMessage());
    }
  }
}
