package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.UpdateProfileRequest;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    public ResponseEntity<?> deleteAccount() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        userRepository.delete(user);
        return ResponseEntity.ok("Account deleted successfully");
    }

    public ResponseEntity<String> uploadAvatar(MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the maximum limit of 10 MB");
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload");
        }

        // Check file content type and file signature
        if (!isValidImageFile(file)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type. Please upload a JPEG or PNG image.");
        }


        try {
            String extension = getFileExtension(file.getOriginalFilename());
            if (extension.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Incorrect file extension");
            }
            String randomFileName = UUID.randomUUID() + extension;
            String directoryPath = "./public/images/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            byte[] bytes = file.getBytes();
            String filePath = directoryPath + randomFileName;
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                fos.write(bytes);
            }

            user.setAvatar(randomFileName);
            userRepository.save(user);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    public ResponseEntity<?> updateProfile(UpdateProfileRequest updateRequest) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        user.setUsername(updateRequest.getName());
        user.setEmail(updateRequest.getEmail());

        userRepository.save(user);
        return ResponseEntity.ok("Profile updated successfully");
    }



    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/jpg")
                && !contentType.equalsIgnoreCase("image/png"))) {
            return false;
        }

        try {
            byte[] fileBytes = file.getBytes();
            // Check the file signature for JPEG and PNG files
            if ((fileBytes.length > 3 && fileBytes[0] == (byte) 0xFF && fileBytes[1] == (byte) 0xD8 && fileBytes[2] == (byte) 0xFF) || // JPEG, JPG
                    (fileBytes.length > 8 && fileBytes[0] == (byte) 0x89 && fileBytes[1] == (byte) 0x50 && fileBytes[2] == (byte) 0x4E && fileBytes[3] == (byte) 0x47 && // PNG
                            fileBytes[4] == (byte) 0x0D && fileBytes[5] == (byte) 0x0A && fileBytes[6] == (byte) 0x1A && fileBytes[7] == (byte) 0x0A)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex);
            if (extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".png")) {
                return extension;
            }
        }
        return "";
    }
}
