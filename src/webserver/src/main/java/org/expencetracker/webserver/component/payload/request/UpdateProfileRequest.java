package org.expencetracker.webserver.component.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

	@NotBlank(message = "Name cannot be blank")
	@Size(max = 255, message = "Name must be less than 255 characters")
	private String name;

	@NotBlank(message = "Email cannot be blank")
	@Size(max = 255, message = "Email must be less than 255 characters")
	@Email(message = "Invalid email format")
	private String email;

	// Getters and setters

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
