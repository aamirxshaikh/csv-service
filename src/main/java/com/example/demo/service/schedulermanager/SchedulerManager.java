package com.example.demo.service.schedulermanager;

public interface SchedulerManager {
  void handleMissingFile();

  void resetFailureCounter();

  void handleProcessingError(Exception e);

  boolean isSchedulerEnabled();
}
