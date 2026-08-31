package com.samadhanx.module.auth.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.infrastructure.security.UserPrincipal;
import com.samadhanx.module.auth.dto.AuthResponse;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role citizenRole;

    @BeforeEach
    void setUp() {
        citizenRole = Role.builder()
                .id(UUID.randomUUID())
                .name(RoleName.CITIZEN)
                .description("Citizen role")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new citizen user")
    void shouldRegisterCitizenUserSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .email("priya.sharma@example.com")
                .password("StrongPassword@123")
                .firstName("Priya")
                .lastName("Sharma")
                .phoneNumber("+919876543210")
                .role(RoleName.CITIZEN)
                .build();

        when(userRepository.existsByEmailIgnoreCase("priya.sharma@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.CITIZEN)).thenReturn(Optional.of(citizenRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password_abc");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("priya.sharma@example.com")
                .firstName("Priya")
                .lastName("Sharma")
                .passwordHash("hashed_password_abc")
                .phoneNumber("+919876543210")
                .isActive(true)
                .build();
        savedUser.addRole(citizenRole);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt.token.mock");
        when(jwtService.getExpiryMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt.token.mock", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("priya.sharma@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email is already registered")
    void shouldThrowConflictExceptionWhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing.user@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmailIgnoreCase("existing.user@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject self-registration attempt for SUPER_ADMIN role")
    void shouldRejectSelfRegistrationForSuperAdmin() {
        RegisterRequest request = RegisterRequest.builder()
                .email("hacker@example.com")
                .password("Password@123")
                .firstName("Malicious")
                .lastName("Actor")
                .role(RoleName.SUPER_ADMIN)
                .build();

        when(userRepository.existsByEmailIgnoreCase("hacker@example.com")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertTrue(exception.getMessage().contains("Self-registration is not allowed for privileged role: SUPER_ADMIN"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject self-registration attempt for GOVERNMENT_ADMIN role")
    void shouldRejectSelfRegistrationForGovernmentAdmin() {
        RegisterRequest request = RegisterRequest.builder()
                .email("fakegov@example.com")
                .password("Password@123")
                .firstName("Fake")
                .lastName("Admin")
                .role(RoleName.GOVERNMENT_ADMIN)
                .build();

        when(userRepository.existsByEmailIgnoreCase("fakegov@example.com")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertTrue(exception.getMessage().contains("Self-registration is not allowed for privileged role: GOVERNMENT_ADMIN"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("CorrectPassword@123")
                .build();

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .firstName("Valid")
                .lastName("User")
                .passwordHash("hashed")
                .isActive(true)
                .build();
        user.addRole(citizenRole);

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(principal)).thenReturn("jwt.login.token");
        when(jwtService.getExpiryMs()).thenReturn(86400000L);
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.login.token", response.getAccessToken());
        assertEquals("user@example.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException when login credentials fail")
    void shouldFailLoginWithBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("WrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
