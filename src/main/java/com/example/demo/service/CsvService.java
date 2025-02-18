package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.CsvProcessingException;
import com.example.demo.exception.FileProcessingException;
import com.example.demo.exception.InvalidFileException;
import com.example.demo.repository.UserRepository;
import com.opencsv.CSVReader;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CsvService {
  private static final Logger log = LoggerFactory.getLogger(CsvService.class);
  private final UserRepository userRepository;

  private static final String CSV_CONTENT_TYPE = "text/csv";
  private static final String FILE_PATH = "/data.csv";
  private static final int MAX_CONSECUTIVE_FAILURES = 3;

  private final Lock fileLock = new ReentrantLock();
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private volatile boolean schedulerEnabled = true;

  public CsvService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Uploads a CSV file and processes the data.
   *
   * @param file The CSV file to upload
   */
  @Transactional
  public void uploadMultipartFile(MultipartFile file) {
    validateFile(file);

    try (InputStream inputStream = file.getInputStream()) {
      readAndPersistData(inputStream);
    } catch (IOException e) {
      throw new FileProcessingException("Error processing uploaded file: " + e.getMessage());
    } catch (CsvException e) {
      throw new CsvProcessingException("Error processing CSV file: " + e.getMessage());
    }
  }

  /**
   * Reads the data from the CSV file and persists it to the database.
   *
   * @param inputStream The input stream of the CSV file
   * @throws IOException  If an I/O error occurs
   * @throws CsvException If an error occurs while parsing the CSV file
   */
  private void readAndPersistData(InputStream inputStream) throws IOException, CsvException {
    log.info("Starting to upload CSV file...");

    CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
    List<String[]> items = csvReader.readAll();

    if (!items.isEmpty()) {
      items.removeFirst();
    }

    log.debug("CSV file parsed successfully. Total items found: {}", items.size());

    List<User> users = new ArrayList<>();
    for (String[] i : items) {
      User user = new User(i[0], Integer.parseInt(i[1]), i[2]);
      users.add(user);
    }

    log.debug("Persisting {} users to the database", users.size());
    userRepository.saveAll(users);

    log.info("CSV file processed and users persisted successfully.");
  }

  /**
   * Processes the data from a scheduled file.
   */
  @Async
  @Scheduled(fixedRate = 10000)
  public void processFileFromLocation() {
    if (!schedulerEnabled) {
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
          handleMissingFile();
          return;
        }

        readAndPersistData(inputStream);
        resetFailureCounter();
      }
    } catch (IOException | CsvException e) {
      handleProcessingError(e);
    } finally {
      fileLock.unlock();
    }
  }

  // Handles the case when the scheduled file is missing.
  private void handleMissingFile() {
    int failures = consecutiveFailures.incrementAndGet();
    log.error("Data file not found at: {} (consecutive failures: {}/{})",
            FILE_PATH, failures, MAX_CONSECUTIVE_FAILURES);

    if (failures >= MAX_CONSECUTIVE_FAILURES) {
      schedulerEnabled = false;
      log.error("Disabled scheduler due to {} consecutive failures", failures);
    }
  }

  // Resets the consecutive failure counter.
  private void resetFailureCounter() {
    consecutiveFailures.set(0);
    log.debug("Reset consecutive failure counter");
  }

  /**
   * Handles an error that occurred during processing of the scheduled file.
   *
   * @param e The exception that occurred
   */
  private void handleProcessingError(Exception e) {
    log.error("Error processing scheduled file: {}", e.getMessage());
    consecutiveFailures.set(0); // Reset for transient errors
  }

  // Validates the uploaded file.
  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new InvalidFileException("File is empty");
    }

    if (!CSV_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
      throw new InvalidFileException("Invalid file type. Only CSV files are allowed");
    }
  }
}
