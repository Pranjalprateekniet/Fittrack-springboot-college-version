package com.pranjal.fittrackv2.service;

import com.pranjal.fittrackv2.dto.AuthResponse;
import com.pranjal.fittrackv2.dto.LoginRequest;
import com.pranjal.fittrackv2.dto.RegisterRequest;
import com.pranjal.fittrackv2.dto.TokenRefreshRequest;
import com.pranjal.fittrackv2.entity.RefreshToken;
import com.pranjal.fittrackv2.entity.User;
import com.pranjal.fittrackv2.repository.RefreshTokenRepository;
import com.pranjal.fittrackv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);
        return "User registered successfully";
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(7L * 24 * 60 * 60))
                .build();

        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token has expired. Please sign in again");
        }

        User user = token.getUser();
        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
