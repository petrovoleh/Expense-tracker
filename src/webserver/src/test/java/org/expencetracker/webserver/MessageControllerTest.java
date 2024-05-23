package org.expencetracker.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.MessageRepository;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageRepository messageRepository;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private Message message;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        message = new Message();
        message.setId("1");
        message.setUserId(user.getId());
        message.setText("Test message");
        message.setDate(LocalDateTime.now());

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testPostMessage_Success() throws Exception {
        Mockito.when(messageRepository.save(Mockito.any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/message/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.text").value("Test message"))
                .andExpect(jsonPath("$.date").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetMessagesByDateRange_Success() throws Exception {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now().plusDays(1);

        Mockito.when(messageRepository.findByUserIdAndDateBetween(user.getId(), fromDate, toDate))
                .thenReturn(Collections.singletonList(message));

        mockMvc.perform(get("/api/message/from/{fromDate}/to/{toDate}", fromDate.toString(), toDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].userId").value(user.getId()))
                .andExpect(jsonPath("$[0].text").value("Test message"))
                .andExpect(jsonPath("$[0].date").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetAllMessagesByUserId_Success() throws Exception {
        Mockito.when(messageRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(message));

        mockMvc.perform(get("/api/message/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].userId").value(user.getId()))
                .andExpect(jsonPath("$[0].text").value("Test message"))
                .andExpect(jsonPath("$[0].date").isNotEmpty());
    }
}
