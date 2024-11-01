package csd.cuemaster.imageservice;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    @Value("${profile.photo.directory}")
    private String directoryPath;

    public String saveImage(Long userId, MultipartFile image) {
        try {
            Path path = Paths.get(directoryPath);
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }

            String filename = "ProfilePhoto_" + userId + ".jpg";
            Path filePath = path.resolve(filename);
            image.transferTo(filePath.toFile());

            return filename; // You can return the filename or full path as needed
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save profile photo", e);
        }
    }

    public ResponseEntity<Resource> getImage(String filename) {
        try {
            Path filepath = Paths.get(directoryPath).resolve(filename);
            Resource resource = new UrlResource(filepath.toUri());

            // Ensure the resource exists before returning it
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build(); // Handle any exceptions
        }
    }
}