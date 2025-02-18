package com.example.demo.service.fileprocessor;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;

public interface FileProcessor {
  void processFile(InputStream inputStream) throws IOException, CsvException;
}
