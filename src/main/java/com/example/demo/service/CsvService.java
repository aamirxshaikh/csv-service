package com.example.demo.service;

import com.example.demo.exception.CsvProcessingException;
import com.example.demo.exception.FileProcessingException;
import com.example.demo.service.fileprocessor.FileProcessor;
import com.example.demo.service.filevalidator.FileValidator;
import com.example.demo.service.schedulermanager.SchedulerManager;
import com.opencsv.exceptions.CsvException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CsvService {
  private static final Logger log = LoggerFactory.getLogger(CsvService.class);
  private static final String FILE_PATH = "/data.csv";

  private final FileProcessor fileProcessor;
  private final FileValidator fileValidator;
  private final SchedulerManager schedulerManager;
  private final Lock fileLock = new ReentrantLock();

  public CsvService(FileProcessor fileProcessor, FileValidator fileValidator, SchedulerManager schedulerManager) {
    this.fileProcessor = fileProcessor;
    this.fileValidator = fileValidator;
    this.schedulerManager = schedulerManager;
  }

  /**
   * Uploads a CSV file and processes the data.
   *
   * @param file The CSV file to upload
   */
  @Transactional
  public void uploadMultipartFile(MultipartFile file) {
    fileValidator.validateFile(file);

    try (InputStream inputStream = file.getInputStream()) {
      fileProcessor.processFile(inputStream);
    } catch (IOException e) {
      throw new FileProcessingException("Error processing uploaded file: " + e.getMessage());
    } catch (CsvException e) {
      throw new CsvProcessingException("Error processing CSV file: " + e.getMessage());
    }
  }

  /**
   * Processes the data from a scheduled file.
   */
  @Async
  @Scheduled(fixedRate = 10000)
  public void processFileFromLocation() {
    if (!schedulerManager.isSchedulerEnabled()) {
      log.debug("Scheduler is currently disabled");
      return;
    }

    if (!fileLock.tryLock()) {
      log.debug("Skipping execution as file processing is already in progress");
      return;
    }

    try {
      try (InputStream inputStream = getClass().getResourceAsStream(FILE_PATH)) {
        if (inputStream == null) {
          schedulerManager.handleMissingFile();
          return;
        }

        fileProcessor.processFile(inputStream);
        schedulerManager.resetFailureCounter();
      }
    } catch (IOException | CsvException e) {
      schedulerManager.handleProcessingError(e);
    } finally {
      fileLock.unlock();
    }
  }
}
