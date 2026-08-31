package com.samadhanx.module.user.service;

import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.user.dto.UserResponse;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUserProfile(UUID userId) {
        User user = findEntityById(userId);
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = findEntityById(userId);
        return UserResponse.fromEntity(user);
    }

    @Override
    public User findEntityById(UUID userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    public User findEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
