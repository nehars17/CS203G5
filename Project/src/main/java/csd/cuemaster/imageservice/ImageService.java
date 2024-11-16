package csd.cuemaster.imageservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    @Value("${profile.photo.directory}")
    private String directoryPath;
    
    public String saveImage(Long userId, MultipartFile image) {
        try {
            // Get the real path (current working directory)
            String realPath = System.getProperty("user.dir") + "/" + directoryPath;
            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdir();  // Create the directory if it doesn't exist
            }

            String filename = "ProfilePhoto_" + userId + ".jpg";
            File newFile = new File(folder, filename);

            // Save the uploaded file to the server's file system
            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                fos.write(image.getBytes());
                fos.flush();
            }

            return filename;  // Return the filename
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save profile photo", e);
        }
    }

    public ResponseEntity<Resource> getImage(String filename) {
        try {
            // Get the real path (current working directory)
            String realPath = System.getProperty("user.dir") + "/" + directoryPath;
            Path filepath = Paths.get(realPath).resolve(filename);
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
            return ResponseEntity.status(500).build();  // Handle any exceptions
        }
    }

    public ResponseEntity<Resource> deleteImage(String filename) {
        try {
            // Get the real path (current working directory)
            String realPath = System.getProperty("user.dir") + "/" + directoryPath;
            Path filepath = Paths.get(realPath).resolve(filename);
            
            // Check if the file exists before attempting to delete it
            File file = filepath.toFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    return ResponseEntity.ok().build();  // Return a success response
                } else {
                    return ResponseEntity.status(500).build(); // If deletion fails
                }
            } else {
                return ResponseEntity.notFound().build();  // If the file doesn't exist
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();  // Return a 500 (Internal Server Error) response if something goes wrong
        }
    }
}
