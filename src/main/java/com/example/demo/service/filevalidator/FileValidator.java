package com.example.demo.service.filevalidator;

import com.example.demo.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

public interface FileValidator {
  void validateFile(MultipartFile file) throws InvalidFileException;
}
