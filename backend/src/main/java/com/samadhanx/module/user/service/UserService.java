package com.samadhanx.module.user.service;

import com.samadhanx.module.user.dto.UserResponse;
import com.samadhanx.module.user.entity.User;

import java.util.UUID;

public interface UserService {
    UserResponse getCurrentUserProfile(UUID userId);
    UserResponse getUserById(UUID userId);
    User findEntityById(UUID userId);
    User findEntityByEmail(String email);
}
