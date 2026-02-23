package com.salesinsight.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadDir;

    public LocalFileStorageService(@Value("${storage.local.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads: " + uploadDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String filename = UUID.randomUUID() + extension;
        Path targetPath = uploadDir.resolve(filename);

        try {
            Files.copy(file.getInputStream(), targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo: " + filename, e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            Files.deleteIfExists(Paths.get(fileUrl));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao deletar arquivo: " + fileUrl, e);
        }
    }
}