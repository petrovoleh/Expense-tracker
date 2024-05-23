package org.expencetracker.webserver;

import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.payload.request.UpdateProfileRequest;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteAccount_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/user/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteAccount_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/user/delete"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUploadAvatar_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "test image".getBytes());
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUploadAvatar_UserNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "test image".getBytes());
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUploadAvatar_FileSizeExceedsLimit() throws Exception {
        byte[] largeFile = new byte[10 * 1024 * 1024 + 1]; // Just over 10MB
        MockMultipartFile file = new MockMultipartFile("file", "largefile.png", MediaType.IMAGE_PNG_VALUE, largeFile);
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File size exceeds the maximum limit of 10 MB"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUploadAvatar_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "emptyfile.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(multipart("/api/user/avatar")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please select a file to upload"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateProfile_Success() throws Exception {
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest("newname", "newemail@example.com");
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateProfile_UserNotFound() throws Exception {
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest("newname", "newemail@example.com");
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }
}
