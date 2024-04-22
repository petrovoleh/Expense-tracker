package org.expencetracker.webserver.component.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.LoginRequest;
import org.expencetracker.webserver.component.payload.request.SignupRequest;
import org.expencetracker.webserver.component.payload.response.JwtResponse;
import org.expencetracker.webserver.component.payload.response.MessageResponse;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;

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
}
