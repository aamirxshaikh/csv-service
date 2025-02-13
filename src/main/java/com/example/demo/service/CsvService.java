package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {
    private static final Logger log = LoggerFactory.getLogger(CsvService.class);
    private final UserRepository userRepository;
    private static final String FILE_PATH = "/data.csv";

    public CsvService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void uploadMultipartFile(MultipartFile file) throws IOException, CsvException {
        try {
            readAndPersistData(file.getInputStream());
        } catch (Exception e) {
            log.error("Error processing CSV file: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void readAndPersistData(InputStream inputStream) throws IOException, CsvException {
        log.info("Starting to upload CSV file...");

        CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
        List<String[]> records = csvReader.readAll();

        if (!records.isEmpty()) {
            records.removeFirst();
        }

        log.debug("CSV file parsed successfully. Total records found: {}", records.size());

        List<User> users = new ArrayList<>();
        for (String[] record : records) {
            User user = new User(record[0], Integer.parseInt(record[1]), record[2]);
            users.add(user);
        }

        log.debug("Persisting {} users to the database", users.size());
        userRepository.saveAll(users);

        log.info("CSV file processed and users persisted successfully.");
    }

    @Scheduled(fixedRate = 10000)
    public void processFileFromLocation() throws IOException, CsvException {
        try {
            readAndPersistData(getClass().getResourceAsStream(FILE_PATH));
        } catch (Exception e) {
            log.error("Error processing CSV file: {}", e.getMessage(), e);
            throw e;
        }
    }
}
