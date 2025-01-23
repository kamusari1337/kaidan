package ru.kaidan.backend.modules.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kaidan.backend.modules.auth.DTO.CookieResponse;
import ru.kaidan.backend.modules.auth.DTO.LoginRequest;
import ru.kaidan.backend.modules.auth.DTO.RegisterRequest;
import ru.kaidan.backend.modules.auth.details.CustomUserDetails;
import ru.kaidan.backend.modules.auth.entities.TokenType;
import ru.kaidan.backend.modules.user.entities.RoleType;
import ru.kaidan.backend.modules.user.entities.UserEntity;
import ru.kaidan.backend.modules.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.refreshToken.cookie-name}")
    private String refreshCookieName;

    public CookieResponse registerUser(RegisterRequest registerRequest) {
        UserEntity user = new UserEntity();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(RoleType.USER);
        userRepository.save(user);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(customUserDetails);
        String refreshToken = jwtService.generateRefreshToken(customUserDetails);
        jwtService.saveUserToken(user, accessToken, TokenType.ACCESS);
        jwtService.saveUserToken(user, refreshToken, TokenType.REFRESH);

        return jwtService.buildCookies(accessToken, refreshToken);
    }

    public CookieResponse loginUser(LoginRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword())
        );
        UserEntity user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(customUserDetails);
        String refreshToken = jwtService.generateRefreshToken(customUserDetails);
        jwtService.revokeAllUserTokens(user);
        jwtService.saveUserToken(user, accessToken, TokenType.ACCESS);
        jwtService.saveUserToken(user, refreshToken, TokenType.REFRESH);
        return jwtService.buildCookies(accessToken, refreshToken);
    }

    public CookieResponse refreshToken(HttpServletRequest request) throws Exception {
        String refreshToken = jwtService.getTokenFromCookies(request, refreshCookieName);
        if (refreshToken == null) {
            throw new Exception("Refresh token is missing");
        }

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new Exception("Invalid refresh token");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, customUserDetails)) {
            throw new Exception("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(customUserDetails);
        jwtService.revokeAllUserTokens(user, TokenType.ACCESS);
        jwtService.saveUserToken(user, newAccessToken, TokenType.ACCESS);

        return jwtService.buildCookies(newAccessToken, refreshToken);
    }
}
