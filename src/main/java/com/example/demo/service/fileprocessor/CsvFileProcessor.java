package com.example.demo.service.fileprocessor;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvFileProcessor implements FileProcessor {
  private static final Logger log = LoggerFactory.getLogger(CsvFileProcessor.class);
  private final UserRepository userRepository;

  public CsvFileProcessor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void processFile(InputStream inputStream) throws IOException, CsvException {
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
}
