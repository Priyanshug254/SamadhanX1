package com.samadhanx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.common.response.ApiError;
import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.infrastructure.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${samadhanx.security.bcrypt-strength:12}")
    private int bcryptStrength;

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/api/v1/demo/**",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/domains/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizations", "/api/v1/organizations/*", "/api/v1/organizations/code/*", "/api/v1/organizations/*/department-profile", "/api/v1/organizations/*/problem-categories", "/api/v1/organizations/*/university-profile", "/api/v1/organizations/*/resources", "/api/v1/organizations/*/faculty", "/api/v1/organizations/*/industry-profile").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/faculty/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/challenges", "/api/v1/challenges/*", "/api/v1/challenges/tracking/*", "/api/v1/challenges/*/timeline", "/api/v1/challenges/innovation-pipeline").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/teams/*", "/api/v1/teams/challenge/*", "/api/v1/teams/university/*", "/api/v1/teams/*/discussions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/proposals", "/api/v1/proposals/*", "/api/v1/proposals/tracking/*", "/api/v1/proposals/challenge/**", "/api/v1/proposals/*/evaluations", "/api/v1/proposals/*/timeline").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/hackathons", "/api/v1/hackathons/*", "/api/v1/hackathons/code/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/partners/**", "/api/v1/pilots/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** Used only for seeded legacy records; authentication is handled by Supabase. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiError error = ApiError.builder()
                    .code("UNAUTHORIZED")
                    .details("Full authentication is required to access this resource")
                    .build();
            ApiResponse<Void> apiResponse = ApiResponse.error("Unauthorized access", error);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiError error = ApiError.builder()
                    .code("FORBIDDEN")
                    .details("You do not have permission to access this resource")
                    .build();
            ApiResponse<Void> apiResponse = ApiResponse.error("Forbidden access", error);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        };
    }
}
