package com.samadhanx.module.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.auth.service.AuthService;
import com.samadhanx.module.notification.dto.RegisterDeviceTokenRequest;
import com.samadhanx.module.notification.entity.enums.DeviceType;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.service.PushNotificationService;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    private String citizenToken;
    private User citizenUser;

    @BeforeEach
    void setUp() {
        String email = "notif.citizen." + System.currentTimeMillis() + "@samadhanx.gov.in";
        authService.register(RegisterRequest.builder()
                .email(email)
                .password("Citizen@123456")
                .firstName("Aakash")
                .lastName("Verma")
                .role(RoleName.CITIZEN)
                .build());

        var loginResp = authService.login(new LoginRequest(email, "Citizen@123456"));
        citizenToken = "Bearer " + loginResp.getAccessToken();
        citizenUser = userRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    @Test
    void testRegisterAndUnregisterDeviceToken() throws Exception {
        RegisterDeviceTokenRequest request = RegisterDeviceTokenRequest.builder()
                .token("fcm_token_test_abc123")
                .deviceType(DeviceType.ANDROID)
                .build();

        mockMvc.perform(post("/api/v1/notifications/device-tokens")
                        .header("Authorization", citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/notifications/device-tokens/fcm_token_test_abc123")
                        .header("Authorization", citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testNotificationLifecycleAndReadState() throws Exception {
        pushNotificationService.sendNotificationToUser(
                citizenUser.getId(),
                "Priority Alert",
                "Your challenge has been routed to Jal Sansthan",
                NotificationType.CHALLENGE_ROUTED,
                UUID.randomUUID().toString(),
                "CHALLENGE"
        );

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Priority Alert"))
                .andExpect(jsonPath("$.data.content[0].isRead").value(false));

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));

        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header("Authorization", citizenToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }
}
