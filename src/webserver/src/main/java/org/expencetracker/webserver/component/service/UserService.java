package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.UpdateProfileRequest;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return the authenticated user or null if not found.
     */
    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Deletes the account of the authenticated user.
     *
     * @return a ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> deleteAccount() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        userRepository.delete(user);
        return ResponseEntity.ok("Account deleted successfully");
    }

    /**
     * Handles the upload of a user's avatar image.
     *
     * @param file the avatar image file.
     * @return a ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<String> uploadAvatar(MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Validate file size and content
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the maximum limit of 10 MB");
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload");
        }
        if (!isValidImageFile(file)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type. Please upload a JPEG or PNG image.");
        }
        String extension = getFileExtension(file.getOriginalFilename());
        if (extension.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Incorrect file extension");
        }
        try {
            // Generate a random file name with the correct extension
            String randomFileName = UUID.randomUUID() + extension;
            String directoryPath = "./public/images/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save the file to the server
            String filePath = directoryPath + randomFileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(file.getBytes());
            }

            // Update the user's avatar in the database
            user.setAvatar(randomFileName);
            userRepository.save(user);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            logger.error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    /**
     * Updates the profile of the authenticated user.
     *
     * @param updateRequest the request containing the updated profile information.
     * @return a ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> updateProfile(UpdateProfileRequest updateRequest) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Update the user's profile with the new information
        user.setUsername(updateRequest.getName());
        user.setEmail(updateRequest.getEmail());
        userRepository.save(user);

        return ResponseEntity.ok("Profile updated successfully");
    }

    /**
     * Validates the uploaded image file.
     *
     * @param file the image file to validate.
     * @return true if the file is a valid image, false otherwise.
     */
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
            logger.error("Error reading file bytes", e);
        }

        return false;
    }

    /**
     * Extracts the file extension from the file name.
     *
     * @param fileName the file name.
     * @return the file extension or an empty string if no valid extension is found.
     */
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
