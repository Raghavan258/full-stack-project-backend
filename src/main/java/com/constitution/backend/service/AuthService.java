package com.constitution.backend.service;

import com.constitution.backend.dto.*;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.UserRepository;
import com.constitution.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<String> register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException("Email is already registered", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new ApiException("Phone number is already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .role(req.getRole())
                .password(passwordEncoder.encode(req.getPassword()))
                .verified(false)
                .active(true)
                .build();
        userRepository.save(user);

        // Send OTP
        emailService.sendOtp(req.getEmail(), req.getFullName());

        log.info("New user registered: {} ({})", req.getEmail(), req.getRole());
        return ApiResponse.ok("Registration successful. Check your email for the OTP.", req.getEmail());
    }

    @Transactional
    public ApiResponse<AuthResponse> verifyOtp(OtpVerifyRequest req) {
        boolean valid = emailService.verifyOtp(req.getEmail(), req.getOtp());
        if (!valid) {
            throw new ApiException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setVerified(true);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return ApiResponse.ok("Email verified successfully!", buildAuthResponse(user, token));
    }

    public ApiResponse<AuthResponse> login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (DisabledException e) {
            throw new ApiException("Account not verified. Please check your email for OTP.", HttpStatus.FORBIDDEN);
        } catch (LockedException e) {
            throw new ApiException("Account is locked. Contact support.", HttpStatus.FORBIDDEN);
        } catch (BadCredentialsException e) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        log.info("Login: {}", user.getEmail());
        return ApiResponse.ok("Login successful", buildAuthResponse(user, token));
    }

    @Transactional
    public ApiResponse<String> resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("No user found with that email", HttpStatus.NOT_FOUND));
        if (user.isVerified()) {
            throw new ApiException("Account already verified", HttpStatus.BAD_REQUEST);
        }
        emailService.sendOtp(email, user.getFullName());
        return ApiResponse.ok("OTP resent to " + email);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .verified(user.isVerified())
                .build();
    }
}
