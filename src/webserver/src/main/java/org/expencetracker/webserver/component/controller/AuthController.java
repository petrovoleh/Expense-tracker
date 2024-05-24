package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.payload.request.ChangePasswordRequest;
import org.expencetracker.webserver.component.payload.request.LoginRequest;
import org.expencetracker.webserver.component.payload.request.SignUpRequest;
import org.expencetracker.webserver.component.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for user authentication.
     *
     * @param loginRequest the login request containing username and password.
     * @return a ResponseEntity with the authentication result.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    /**
     * Endpoint to check the authentication status of the current user.
     *
     * @return a ResponseEntity with the authentication status.
     */
    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuthentication() {
        return authService.checkAuthentication();
    }

    /**
     * Endpoint to change the password of the authenticated user.
     *
     * @param changePasswordRequest the request containing old and new passwords.
     * @return a ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/changepassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        return authService.changePassword(changePasswordRequest);
    }

    /**
     * Endpoint for user registration.
     *
     * @param signUpRequest the sign-up request containing username, email, and password.
     * @return a ResponseEntity with the registration result.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.registerUser(signUpRequest);
    }
}
