package com.example.computershop.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemStorageService implements StorageService {
    private final Path location;
    public FileSystemStorageService() {
        this.location = Paths.get("src/main/resources/static/uploads");
    }
    @Override
    public void store(MultipartFile file) {
        try {
            Path destinationFile = this.location.resolve(Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();
            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void init() {
        try {
            Files.createDirectories(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void delete(String fileName) {
        try {
            Path filePath = Paths.get("src/main/resources/static/uploads").resolve(fileName).normalize().toAbsolutePath();
            if (Files.isDirectory(filePath)) {
                // If it's a directory, we should not try to delete it
                // Cannot delete directory
            } else {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
