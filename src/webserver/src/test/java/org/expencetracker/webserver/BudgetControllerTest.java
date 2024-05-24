package org.expencetracker.webserver;

import org.expencetracker.webserver.component.models.Budget;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.BudgetRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetRepository budgetRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        budget = new Budget();
        budget.setId("1");
        budget.setUserId(user.getId());
        budget.setName("Test Budget");
        budget.setBudget(1000);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAddBudget_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.save(Mockito.any(Budget.class))).thenReturn(budget);

        mockMvc.perform(post("/api/budget/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Budget"))
                .andExpect(jsonPath("$.budget").value(1000));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAddBudget_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/budget/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetBudgetsByUserId_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(budget));

        mockMvc.perform(get("/api/budget/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Budget"))
                .andExpect(jsonPath("$[0].budget").value(1000));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetBudgetsByUserId_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budget/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateBudget_Success() throws Exception {
        Budget updatedBudget = budget;
        updatedBudget.setName("Updated Budget");
        updatedBudget.setBudget(2000);

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));
        Mockito.when(budgetRepository.save(Mockito.any(Budget.class))).thenReturn(updatedBudget);

        mockMvc.perform(put("/api/budget/{id}", budget.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBudget)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Budget"))
                .andExpect(jsonPath("$.budget").value(2000));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateBudget_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/budget/{id}", budget.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateBudget_BudgetNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findById(budget.getId())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/budget/{id}", budget.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateBudget_Unauthorized() throws Exception {
        User anotherUser = new User();
        anotherUser.setId("2");
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("anotheruser@example.com");

        budget.setUserId(anotherUser.getId());

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));

        mockMvc.perform(put("/api/budget/{id}", budget.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not authorized to update this budget"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudget_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));

        mockMvc.perform(delete("/api/budget/{id}", budget.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Budget deleted successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudget_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/budget/{id}", budget.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudget_BudgetNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findById(budget.getId())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/budget/{id}", budget.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudget_Unauthorized() throws Exception {
        User anotherUser = new User();
        anotherUser.setId("2");
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("anotheruser@example.com");

        budget.setUserId(anotherUser.getId());

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));

        mockMvc.perform(delete("/api/budget/{id}", budget.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not authorized to delete this budget"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudgetsByUserId_Success() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(budget));

        mockMvc.perform(delete("/api/budget/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Budgets deleted successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudgetsByUserId_UserNotFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/budget/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().                string("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteBudgetsByUserId_NoBudgetsFound() throws Exception {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(budgetRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/api/budget/user"))
                .andExpect(status().isNotFound());
    }

}