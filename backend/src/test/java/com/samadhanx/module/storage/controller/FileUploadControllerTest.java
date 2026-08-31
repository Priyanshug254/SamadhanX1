package com.samadhanx.module.storage.controller;

import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.auth.service.AuthService;
import com.samadhanx.module.role.entity.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private String citizenToken;

    @BeforeEach
    void setUp() {
        String email = "upload.citizen." + System.currentTimeMillis() + "@samadhanx.gov.in";
        authService.register(RegisterRequest.builder()
                .email(email)
                .password("Citizen@123456")
                .firstName("Meera")
                .lastName("Nair")
                .role(RoleName.CITIZEN)
                .build());

        var loginResp = authService.login(new LoginRequest(email, "Citizen@123456"));
        citizenToken = "Bearer " + loginResp.getAccessToken();
    }

    @Test
    void testUploadAndRetrieveFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "water_sample.jpg",
                "image/jpeg",
                "sample evidence binary data".getBytes()
        );

        var result = mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .header("Authorization", citizenToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.originalFileName").value("water_sample.jpg"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // verify public access to file without auth
        String fileName = com.jayway.jsonpath.JsonPath.read(content, "$.data.fileName");
        mockMvc.perform(get("/api/v1/files/" + fileName))
                .andExpect(status().isOk());
    }
}
