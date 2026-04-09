package com.constitution.backend.config;

import com.constitution.backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger UI
                .requestMatchers(
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/v3/api-docs/**", "/v3/api-docs"
                ).permitAll()

                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Public read-only
                .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/forums/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/study-notes/**").permitAll()

                // Admin only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Queries — citizens submit, responders answer, admin manages
                .requestMatchers(HttpMethod.POST, "/api/queries").hasRole("CITIZEN")
                .requestMatchers(HttpMethod.GET, "/api/queries/my").hasRole("CITIZEN")
                .requestMatchers("/api/queries/**").authenticated()

                // Article mutations
                .requestMatchers(HttpMethod.POST, "/api/articles/**")
                    .hasAnyRole("EDUCATOR", "LEGAL_EXPERT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/articles/**")
                    .hasAnyRole("EDUCATOR", "LEGAL_EXPERT", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/articles/**")
                    .hasAnyRole("LEGAL_EXPERT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/articles/**")
                    .hasRole("ADMIN")

                // Study note mutations
                .requestMatchers(HttpMethod.POST, "/api/study-notes/**")
                    .hasAnyRole("EDUCATOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/study-notes/**")
                    .hasAnyRole("EDUCATOR", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/study-notes/**")
                    .hasAnyRole("EDUCATOR", "ADMIN")

                // Quiz management
                .requestMatchers(HttpMethod.POST, "/api/quizzes/**").hasAnyRole("EDUCATOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasAnyRole("EDUCATOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasRole("ADMIN")

                // Everything else — authenticated
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
