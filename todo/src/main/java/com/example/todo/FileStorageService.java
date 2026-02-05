package com.example.todo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private final Path rootDir;

  public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
    this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(rootDir);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to initialize upload directory", e);
    }
  }

  public StoredFile store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException("ファイルが選択されていません。");
    }
    String originalName = sanitizeOriginalFilename(file.getOriginalFilename());
    String extension = getExtension(originalName);
    String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
    Path target = rootDir.resolve(storedName);
    try {
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new BusinessException("ファイルの保存に失敗しました。");
    }
    return new StoredFile(originalName, storedName, file.getContentType(), file.getSize());
  }

  public Resource loadAsResource(String storedFilename) {
    try {
      Path file = rootDir.resolve(storedFilename).normalize();
      Resource resource = new UrlResource(file.toUri());
      if (!resource.exists()) {
        throw new TodoNotFoundException("ファイルが見つかりませんでした。");
      }
      return resource;
    } catch (IOException e) {
      throw new TodoNotFoundException("ファイルが見つかりませんでした。");
    }
  }

  public void delete(String storedFilename) {
    try {
      Path file = rootDir.resolve(storedFilename).normalize();
      Files.deleteIfExists(file);
    } catch (IOException e) {
      throw new BusinessException("ファイルの削除に失敗しました。");
    }
  }

  private String sanitizeOriginalFilename(String name) {
    if (name == null || name.isBlank()) {
      return "file";
    }
    String base = Paths.get(name).getFileName().toString();
    return base.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private String getExtension(String filename) {
    int dot = filename.lastIndexOf('.');
    if (dot < 0) {
      return "";
    }
    return filename.substring(dot);
  }

  public record StoredFile(String originalFilename, String storedFilename,
                           String contentType, long size) {
  }
}
