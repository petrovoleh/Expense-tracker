package org.expencetracker.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.ChangePasswordRequest;
import org.expencetracker.webserver.component.payload.request.LoginRequest;
import org.expencetracker.webserver.component.payload.request.SignUpRequest;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String URL ="http://localhost:8080";

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder encoder;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void testAuthenticateUser_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetailsImpl userDetails = new UserDetailsImpl("1", "user", "email@example.com", "password");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String jwt = "jwtToken";
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwt);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(jwt));
    }

    @Test
    public void testAuthenticateUser_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid username or password"));
    }
    @Test
    @WithMockUser(username = "user")
    public void testChangePassword_Success() throws Exception {
        // Arrange
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPassword", "newPassword");
        User user = new User("user", "email@example.com", "encodedOldPassword");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(encoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())).thenReturn(true);
        when(encoder.encode(changePasswordRequest.getNewPassword())).thenReturn("encodedNewPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changepassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Password changed successfully"));
    }

    @Test
    @WithMockUser(username = "user")
    public void testChangePassword_InvalidOldPassword() throws Exception {
        // Arrange
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("wrongOldPassword", "newPassword");
        User user = new User("user", "email@example.com", "encodedOldPassword");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(encoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changepassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid old password"));
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        // Arrange
        SignUpRequest signupRequest = new SignUpRequest("newUser123", "email132@example.com", "password132");

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetailsImpl userDetails = new UserDetailsImpl("1", "newUser", "email@example.com", "encodedPassword");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String jwt = "jwtToken";
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwt);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(jwt));
    }

    @Test
    public void testRegisterUser_UsernameTaken() throws Exception {
        // Arrange
        SignUpRequest signupRequest = new SignUpRequest("existingUser", "email@example.com", "password");

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    public void testRegisterUser_EmailInUse() throws Exception {
        // Arrange
        SignUpRequest signupRequest = new SignUpRequest("newUser", "existingEmail@example.com", "password");

        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error: Email is already in use!"));
    }

//    @Test
//    @WithMockUser(username = "user")
//    public void testUpdateProfile_Success() throws Exception {
//        // Arrange
//        UpdateProfileRequest updateRequest = new UpdateProfileRequest("newName", "newEmail@example.com");
//        User user = new User("user", "email@example.com", "password");
//
//        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.put("/api/auth/update")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Profile updated successfully"));
//    }

//    @Test
//    @WithMockUser(username = "user")
//    public void testUpdateProfile_UserNotFound() throws Exception {
//        // Arrange
//        UpdateProfileRequest updateRequest = new UpdateProfileRequest("newName", "newEmail@example.com");
//
//        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        mockMvc.perform(MockMvcRequestBuilders.put("/api/auth/update")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
//                .andExpect(MockMvcResultMatchers.content().string("User not found"));
//    }
}
