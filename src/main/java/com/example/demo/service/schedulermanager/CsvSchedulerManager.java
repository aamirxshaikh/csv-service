package com.example.demo.service.schedulermanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CsvSchedulerManager implements SchedulerManager {
  private static final Logger log = LoggerFactory.getLogger(CsvSchedulerManager.class);
  private static final int MAX_CONSECUTIVE_FAILURES = 3;

  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private volatile boolean schedulerEnabled = true;

  @Override
  public void handleMissingFile() {
    int failures = consecutiveFailures.incrementAndGet();
    log.error("Data file not found (consecutive failures: {}/{})", failures, MAX_CONSECUTIVE_FAILURES);

    if (failures >= MAX_CONSECUTIVE_FAILURES) {
      schedulerEnabled = false;
      log.error("Disabled scheduler due to {} consecutive failures", failures);
    }
  }

  @Override
  public void resetFailureCounter() {
    consecutiveFailures.set(0);
    log.debug("Reset consecutive failure counter");
  }

  @Override
  public void handleProcessingError(Exception e) {
    log.error("Error processing scheduled file: {}", e.getMessage());
    consecutiveFailures.set(0); // Reset for transient errors
  }

  @Override
  public boolean isSchedulerEnabled() {
    return schedulerEnabled;
  }
}
