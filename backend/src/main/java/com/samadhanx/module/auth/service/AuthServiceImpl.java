package com.samadhanx.module.auth.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.auth.dto.AuthResponse;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import com.samadhanx.module.user.dto.UserResponse;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered: " + email);
        }

        RoleName targetRoleName = request.getRole() != null ? request.getRole() : RoleName.CITIZEN;

        if (!targetRoleName.isSelfRegisterable()) {
            log.warn("Attempted unauthorized self-registration for privileged role: {} by email: {}", targetRoleName, email);
            throw new BadRequestException("Self-registration is not allowed for privileged role: " + targetRoleName
                    + ". This role requires administrative onboarding or departmental verification.");
        }

        Role role = roleRepository.findByName(targetRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", targetRoleName));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user.addRole(role);
        User savedUser = userRepository.save(user);

        log.info("Successfully registered new user: {} with role: {}", email, targetRoleName);

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String token = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiryMs())
                .user(UserResponse.fromEntity(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(userPrincipal);

        User user = userRepository.findByIdWithRoles(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        log.info("User successfully logged in: {}", email);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiryMs())
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
