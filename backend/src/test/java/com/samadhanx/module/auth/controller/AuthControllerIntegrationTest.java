package com.samadhanx.module.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.role.entity.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Authentication & User Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete User Journey: Register -> Login -> Fetch /api/v1/users/me")
    void completeUserJourneyTest() throws Exception {
        String testEmail = "citizen." + System.currentTimeMillis() + "@samadhanx.org";
        String testPassword = "SecurePassword@123";

        // 1. Register new citizen
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .firstName("Aarav")
                .lastName("Patel")
                .phoneNumber("+919123456789")
                .role(RoleName.CITIZEN)
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is(testEmail)))
                .andExpect(jsonPath("$.data.user.firstName", is("Aarav")))
                .andExpect(jsonPath("$.data.user.lastName", is("Patel")))
                .andReturn();

        // 2. Attempt duplicate registration -> Conflict 409
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("CONFLICT")));

        // 3. Login with valid credentials -> 200 OK
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is(testEmail)))
                .andReturn();

        // Extract JWT token
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String jwtToken = loginJson.get("data").get("accessToken").asText();

        // 4. Access /api/v1/users/me with Bearer token -> 200 OK
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is(testEmail)))
                .andExpect(jsonPath("$.data.fullName", is("Aarav Patel")))
                .andExpect(jsonPath("$.data.roles", hasSize(greaterThanOrEqualTo(1))));

        // 5. Access /api/v1/users/me without token -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("Should reject privileged role registration attempt")
    void shouldRejectPrivilegedRoleRegistration() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("illegal.admin." + System.currentTimeMillis() + "@samadhanx.org")
                .password("Password@123")
                .firstName("Fake")
                .lastName("Admin")
                .role(RoleName.SUPER_ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void shouldRejectInvalidPasswordLogin() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent.user@samadhanx.org")
                .password("WrongPassword@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Actuator health endpoint should be publicly accessible")
    void actuatorHealthShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
