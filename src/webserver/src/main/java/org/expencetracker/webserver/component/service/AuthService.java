package org.expencetracker.webserver.component.service;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.ChangePasswordRequest;
import org.expencetracker.webserver.component.payload.request.LoginRequest;
import org.expencetracker.webserver.component.payload.request.SignUpRequest;
import org.expencetracker.webserver.component.payload.response.JwtResponse;
import org.expencetracker.webserver.component.payload.response.MessageResponse;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Authenticates a user based on the provided login request.
     *
     * @param loginRequest the login request containing username and password.
     * @return a ResponseEntity with the JWT token and user details if authentication is successful.
     */
    public ResponseEntity<?> authenticateUser(@Valid LoginRequest loginRequest) {
        try {
            return getResponseEntity(loginRequest.getUsername(), loginRequest.getPassword());
        } catch (AuthenticationException e) {
            logger.error("AuthenticationException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    /**
     * This method authenticates a user with the given username and password,
     * generates a JWT token if authentication is successful, and returns a ResponseEntity
     * containing the JWT token and user details.
     *
     * @param username the username of the user attempting to authenticate
     * @param password the password of the user attempting to authenticate
     * @return a ResponseEntity containing the JWT token and user details if authentication is successful,
     *         or an error response if authentication fails
     */
    private ResponseEntity<?> getResponseEntity(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail()));
    }

    /**
     * Checks the authentication status of the current user.
     *
     * @return a ResponseEntity with the authenticated username.
     */
    public ResponseEntity<String> checkAuthentication() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok("Authenticated User: " + username);
    }

    /**
     * Changes the password for the authenticated user.
     *
     * @param changePasswordRequest the request containing old and new passwords.
     * @return a ResponseEntity indicating the result of the password change.
     */
    public ResponseEntity<?> changePassword(@Valid ChangePasswordRequest changePasswordRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (!encoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid old password");
        }

        user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    /**
     * Registers a new user.
     *
     * @param signUpRequest the sign-up request containing username, email, and password.
     * @return a ResponseEntity with the JWT token and user details if registration is successful.
     */
    public ResponseEntity<?> registerUser(@Valid SignUpRequest signUpRequest) {
        if (StringUtils.isBlank(signUpRequest.getUsername()) || signUpRequest.getUsername().length() > 50) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is empty or too long!"));
        }

        if (StringUtils.isBlank(signUpRequest.getEmail()) || signUpRequest.getEmail().length() > 255) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is empty or too long!"));
        }

        if (StringUtils.isBlank(signUpRequest.getPassword()) || signUpRequest.getPassword().length() > 255) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password is empty or too long!"));
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return getResponseEntity(signUpRequest.getUsername(), signUpRequest.getPassword());
    }
}
