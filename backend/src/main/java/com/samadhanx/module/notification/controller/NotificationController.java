package com.samadhanx.module.notification.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.notification.dto.NotificationResponse;
import com.samadhanx.module.notification.dto.RegisterDeviceTokenRequest;
import com.samadhanx.module.notification.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications & Device Tokens", description = "FCM Push Notification device token registration and user notification history")
public class NotificationController {

    private final PushNotificationService pushNotificationService;

    @PostMapping("/device-tokens")
    @Operation(summary = "Register FCM device token", description = "Registers or updates client device token for push notification delivery")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody RegisterDeviceTokenRequest request
    ) {
        pushNotificationService.registerDeviceToken(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Device token registered successfully", null));
    }

    @DeleteMapping("/device-tokens/{token}")
    @Operation(summary = "Unregister FCM device token", description = "Removes device token on user logout")
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String token
    ) {
        pushNotificationService.unregisterDeviceToken(currentUser.getId(), token);
        return ResponseEntity.ok(ApiResponse.ok("Device token unregistered successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves paginated notifications for current authenticated user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificationResponse> notifications = pushNotificationService.getUserNotifications(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("User notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        long count = pushNotificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        pushNotificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        pushNotificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    @GetMapping("/activity-feed")
    @Operation(summary = "Get recent ecosystem activity feed", description = "Retrieves live stream of recent ecosystem actions")
    public ResponseEntity<ApiResponse<java.util.List<com.samadhanx.module.notification.dto.EcosystemActivityFeedItem>>> getActivityFeed(
            @org.springframework.web.bind.annotation.RequestParam(name = "limit", required = false, defaultValue = "15") int limit
    ) {
        java.util.List<com.samadhanx.module.notification.dto.EcosystemActivityFeedItem> feed = pushNotificationService.getEcosystemActivityFeed(limit);
        return ResponseEntity.ok(ApiResponse.ok("Ecosystem activity feed retrieved", feed));
    }
}
