package org.expencetracker.webserver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.expencetracker.webserver.component.models.Record;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.RecordRepository;
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

import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecordRepository recordRepository;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private Record record;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setUsername("testuser");

        record = new Record();
        record.setId("1");
        record.setCategory("Food");
        record.setDescription("Lunch");
        record.setValue(10.0);
        record.setUserId(user.getId());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAddRecord_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.save(Mockito.any(Record.class))).thenReturn(record);

        mockMvc.perform(post("/api/record/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.description").value("Lunch"))
                .andExpect(jsonPath("$.value").value(10.0));
    }


    @Test
    @WithMockUser(username = "testuser")
    public void testAddRecord_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        Mockito.when(recordRepository.save(Mockito.any(Record.class))).thenReturn(record);

        mockMvc.perform(post("/api/record/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetRecordsByUserId_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(record));

        mockMvc.perform(get("/api/record/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].description").value("Lunch"))
                .andExpect(jsonPath("$[0].value").value(10.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetRecordsByUserId_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/record/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRecord_Success() throws Exception {
        Record updatedRecord = new Record();
        updatedRecord.setId("1");
        updatedRecord.setCategory("Food");
        updatedRecord.setDescription("Dinner");
        updatedRecord.setValue(20.0);
        updatedRecord.setUserId(user.getId());

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findById("1")).thenReturn(Optional.of(record));
        Mockito.when(recordRepository.save(Mockito.any(Record.class))).thenReturn(updatedRecord);

        mockMvc.perform(put("/api/record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRecord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.description").value("Dinner"))
                .andExpect(jsonPath("$.value").value(20.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRecord_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRecord_RecordNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findById("1")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateRecord_Unauthorized() throws Exception {
        User anotherUser = new User();
        anotherUser.setId("2");
        anotherUser.setUsername("anotheruser");

        Record anotherRecord = new Record();
        anotherRecord.setId("1");
        anotherRecord.setCategory("Food");
        anotherRecord.setDescription("Lunch");
        anotherRecord.setValue(10.0);
        anotherRecord.setUserId(anotherUser.getId());

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findById("1")).thenReturn(Optional.of(anotherRecord));

        mockMvc.perform(put("/api/record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User unauthorized to change this record"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteRecord_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findById("1")).thenReturn(Optional.of(record));

        mockMvc.perform(delete("/api/record/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteRecord_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/record/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteRecord_RecordNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findById("1")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/record/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteRecord_Unauthorized() throws Exception {
        User anotherUser = new User();
        anotherUser.setId("2");
        anotherUser.setUsername("anotheruser");

        Record anotherRecord = new Record();
        anotherRecord.setId("1");
        anotherRecord.setCategory("Food");
        anotherRecord.setDescription("Lunch");
        anotherRecord.setValue(10.0);
        anotherRecord.setUserId(anotherUser.getId());

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(recordRepository.findById("1")).thenReturn(Optional.of(anotherRecord));

        mockMvc.perform(delete("/api/record/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User unauthorized to delete this record"));
    }
}
