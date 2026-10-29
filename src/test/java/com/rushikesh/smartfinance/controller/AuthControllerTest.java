package com.rushikesh.smartfinance.controller;

import tools.jackson.databind.ObjectMapper;

import com.rushikesh.smartfinance.dto.RegisterRequest;
import com.rushikesh.smartfinance.entity.User;
import com.rushikesh.smartfinance.service.UserService;
import com.rushikesh.smartfinance.exception.EmailAlreadyRegisteredException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.rushikesh.smartfinance.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void register_validRequest_returnsCreatedResponse() throws Exception {
        // Arrange
        User user = new User(
                "Rishi",
                "rishi@example.com",
                "encoded-password",
                "USER"
        );

        when(userService.registerUser(
                anyString(), anyString(), anyString()
        )).thenReturn(user);

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(
                                        "Rishi",
                                        "rishi@example.com",
                                        "myPassword123"
                                )
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rishi"))
                .andExpect(jsonPath("$.email").value("rishi@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_invalidEmail_returnsBadRequest() throws Exception {
        String requestBody = """
            {
                "name": "Rishi",
                "email": "not-an-email",
                "password": "myPassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }


    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        // Arrange
        when(userService.registerUser(
                "Rishi",
                "rishi@example.com",
                "myPassword123"
        )).thenThrow(
                new EmailAlreadyRegisteredException(
                        "An account with this email already exists."
                )
        );

        String requestBody = """
            {
                "name": "Rishi",
                "email": "rishi@example.com",
                "password": "myPassword123"
            }
            """;

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("An account with this email already exists."));

        verify(userService).registerUser(
                "Rishi",
                "rishi@example.com",
                "myPassword123"
        );
    }

    @Test
    void unauthenticatedRequestToProtectedEndpoint_returnsUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/protected-test"))
                .andExpect(status().isUnauthorized());
    }

}