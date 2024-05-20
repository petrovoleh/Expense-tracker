package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;



    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody User userDetails) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPassword(userDetails.getPassword());
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount() {
        // Retrieve the username of the authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Find the user by username
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        // Delete the user account
        userRepository.delete(user);

        return ResponseEntity.ok("Account deleted successfully");
    }
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // Find the user by username
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Check if the file size exceeds 10 MB (10 * 1024 * 1024 bytes)
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the maximum limit of 10 MB");
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload");
        }

        try {
            // Generate a random filename using UUID
            String extension = getFileExtension(file.getOriginalFilename());
            if (extension.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Incorrect file extension");
            }
            String randomFileName = UUID.randomUUID().toString() + extension;

            // Specify the directory where you want to save the file
            String directoryPath = "./public/images/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save the file to the specified directory
            byte[] bytes = file.getBytes();
            String filePath = directoryPath + randomFileName;
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            fos.write(bytes);
            fos.close();

            // Update the user's avatar field and save the user object
            user.setAvatar(randomFileName);
            userRepository.save(user);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }


    // Method to get the file extension
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            if (fileName.endsWith(".jpeg")||fileName.endsWith(".jpg")|| fileName.endsWith(".png")){
                return fileName.substring(dotIndex);
            }
        }
        return ""; // No extension found
    }
}
