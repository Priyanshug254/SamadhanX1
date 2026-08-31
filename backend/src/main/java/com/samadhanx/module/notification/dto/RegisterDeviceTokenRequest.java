package com.samadhanx.module.notification.dto;

import com.samadhanx.module.notification.entity.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceTokenRequest {

    @Schema(example = "fcm_token_string_from_flutter_client_xxxx", description = "FCM registration token")
    @NotBlank(message = "Token is required")
    private String token;

    @Schema(example = "ANDROID", description = "ANDROID, IOS, or WEB")
    @Builder.Default
    private DeviceType deviceType = DeviceType.ANDROID;
}
