package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void handleNotFound_returns404() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new NotFoundException("User not found: 99"));
        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found: 99"));
    }

    @Test
    void handleConflict_returns409() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new ConflictException("Email already exists"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void handleValidation_returns400() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new ValidationException("Name cannot be blank"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Name cannot be blank"));
    }

    @Test
    void handleForbidden_returns403() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new ForbiddenException("Access denied"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void handleOther_returns500() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unexpected error"));
    }
}