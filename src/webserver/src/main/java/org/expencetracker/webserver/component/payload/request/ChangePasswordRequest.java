package org.expencetracker.webserver.component.payload.request;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {
	@NotBlank(message = "Old password cannot be blank")
	private String oldPassword;

	@NotBlank(message = "New password cannot be blank")
	private String newPassword;

	// Constructor, getters, and setters
	public ChangePasswordRequest() {}

	public ChangePasswordRequest(String oldPassword, String newPassword) {
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
