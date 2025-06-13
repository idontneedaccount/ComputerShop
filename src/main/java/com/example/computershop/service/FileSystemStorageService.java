package com.example.computershop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path location;
    
    public FileSystemStorageService() {
        this.location = Paths.get("src/main/resources/static/assets/images/product/laptop");
    }
    
    @Override
    public void store(MultipartFile file) {
        try {
            Path destinationFile = this.location.resolve(Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();
            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("File stored successfully: {}", file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Failed to store file: {}", file.getOriginalFilename(), e);
        }
    }
    
    @Override
    public void init() {
        try {
            Files.createDirectories(location);
            logger.info("Storage directory initialized: {}", location);
        } catch (IOException e) {
            logger.error("Failed to initialize storage directory: {}", location, e);
        }
    }
    
    @Override
    public void delete(String fileName) {
        try {
            Path filePath = Paths.get("src/main/resources/static/assets/images/product/laptop").resolve(fileName).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
            logger.info("File deleted successfully: {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", fileName, e);
        }
    }
}
