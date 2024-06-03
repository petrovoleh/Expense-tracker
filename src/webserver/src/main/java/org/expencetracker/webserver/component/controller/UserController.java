package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.payload.request.UpdateProfileRequest;
import org.expencetracker.webserver.component.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    // Constructor injection of UserService
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to delete the authenticated user's account.
     *
     * @return a ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount() {
        return userService.deleteAccount();
    }

    /**
     * Endpoint to upload an avatar image for the authenticated user.
     *
     * @param file the avatar image file to upload.
     * @return a ResponseEntity indicating the result of the upload operation.
     */
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    /**
     * Endpoint to update the profile information of the authenticated user.
     *
     * @param updateRequest the request containing the updated profile information.
     * @return a ResponseEntity indicating the result of the update operation.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest updateRequest) {
        return userService.updateProfile(updateRequest);
    }
}
