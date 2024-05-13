package org.expencetracker.webserver.component.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;

import org.expencetracker.webserver.component.payload.request.ChangePasswordRequest;
import org.expencetracker.webserver.component.payload.request.UpdateProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.LoginRequest;
import org.expencetracker.webserver.component.payload.request.SignupRequest;
import org.expencetracker.webserver.component.payload.response.JwtResponse;
import org.expencetracker.webserver.component.payload.response.MessageResponse;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtUtils.generateJwtToken(authentication);

			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
			return ResponseEntity.ok(new JwtResponse(jwt,
					userDetails.getId(),
					userDetails.getUsername(),
					userDetails.getEmail()));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

	@GetMapping("/check-auth")
	public ResponseEntity<String> checkAuthentication() {
		// Retrieve the username of the authenticated user
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// You can perform additional checks or return any information about the authenticated user
		return ResponseEntity.ok("Authenticated User: " + username);
	}

	@PostMapping("/changepassword")
	public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
		// Retrieve the username of the authenticated user
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// Find the user by username
		User user = userRepository.findByUsername(username).orElse(null);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
		}

		// Check if the old password matches
		if (!encoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid old password");
		}

		// Update the password
		user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
		userRepository.save(user);

		return ResponseEntity.ok("Password changed successfully");
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		// Check if username is empty or exceeds max size
		if (StringUtils.isBlank(signUpRequest.getUsername()) || signUpRequest.getUsername().length() > 254) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is empty or too long!"));
		}

		// Check if email is empty or exceeds max size
		if (StringUtils.isBlank(signUpRequest.getEmail()) || signUpRequest.getEmail().length() > 254) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is empty or too long!"));
		}

		// Check if password is empty or exceeds max size
		if (StringUtils.isBlank(signUpRequest.getPassword()) || signUpRequest.getPassword().length() > 254) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Password is empty or too long!"));
		}

		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(),
				signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		userRepository.save(user);
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		return ResponseEntity.ok(new JwtResponse(jwt,
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail()));
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

		// Perform any additional cleanup or validation before deleting the account
		// For example, you might want to check if the user has any associated data to delete

		// Delete the user account
		userRepository.delete(user);

		return ResponseEntity.ok("Account deleted successfully");
	}
	@PutMapping("/update")
	public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
		// Retrieve the authenticated user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		// Find the user by username
		User user = userRepository.findByUsername(username).orElse(null);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
		}

		// Update the user's name and email
		user.setUsername(updateRequest.getName());
		user.setEmail(updateRequest.getEmail());

		// Save the updated user
		userRepository.save(user);

		return ResponseEntity.ok("Profile updated successfully");
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
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body("Please select a file to upload");
		}

		try {
			// Specify the directory where you want to save the file
			String directoryPath = "./public/images/";
			File directory = new File(directoryPath);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			// Save the file to the specified directory
			byte[] bytes = file.getBytes();
			String filePath = directoryPath + file.getOriginalFilename();
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			fos.write(bytes);
			fos.close();

			return ResponseEntity.ok("File uploaded successfully");
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
		}
	}

}
