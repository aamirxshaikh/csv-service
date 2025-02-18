package com.example.demo.service.filevalidator;

import com.example.demo.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvFileValidator implements FileValidator {
  private static final String CSV_CONTENT_TYPE = "text/csv";

  @Override
  public void validateFile(MultipartFile file) throws InvalidFileException {
    if (file.isEmpty()) {
      throw new InvalidFileException("File is empty");
    }

    if (!CSV_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
      throw new InvalidFileException("Invalid file type. Only CSV files are allowed");
    }
  }
}
