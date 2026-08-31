package com.samadhanx.infrastructure.security;

import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }

    /**
     * Keeps the existing application user/role model as the authorization
     * source, while letting Supabase remain the only password authority.
     * Existing users are matched by e-mail. A verified Supabase user signing
     * up for the first time receives the safe default CITIZEN role.
     */
    @Transactional
    public UserDetails loadOrProvisionSupabaseUser(String email, Claims claims) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Supabase token does not contain an email address");
        }

        return userRepository.findByEmailIgnoreCase(email)
                .map(UserPrincipal::create)
                .orElseGet(() -> UserPrincipal.create(provisionCitizen(email, claims)));
    }

    @SuppressWarnings("unchecked")
    private User provisionCitizen(String email, Claims claims) {
        Map<String, Object> metadata = claims.get("user_metadata", Map.class);
        String firstName = metadata != null && metadata.get("first_name") != null
                ? metadata.get("first_name").toString().trim() : "Supabase";
        String lastName = metadata != null && metadata.get("last_name") != null
                ? metadata.get("last_name").toString().trim() : "User";
        String phone = metadata != null && metadata.get("phone_number") != null
                ? metadata.get("phone_number").toString().trim() : null;

        Role citizen = roleRepository.findByName(RoleName.CITIZEN)
                .orElseThrow(() -> new UsernameNotFoundException("CITIZEN role is not configured"));
        User user = User.builder()
                .email(email.trim().toLowerCase())
                // This is deliberately not a password. Spring never authenticates it.
                .passwordHash("{supabase-managed}")
                .firstName(firstName.isBlank() ? "Supabase" : firstName)
                .lastName(lastName.isBlank() ? "User" : lastName)
                .phoneNumber(phone)
                .isActive(true)
                .isEmailVerified(claims.get("email_confirmed_at") != null)
                .build();
        user.addRole(citizen);
        return userRepository.save(user);
    }
}
