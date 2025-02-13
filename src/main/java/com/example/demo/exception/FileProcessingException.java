package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error processing file")
public class FileProcessingException extends RuntimeException {
  public FileProcessingException(String message) {
    super(message);
  }
}
