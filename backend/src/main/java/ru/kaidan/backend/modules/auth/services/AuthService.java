package ru.kaidan.backend.modules.auth.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kaidan.backend.modules.auth.DTO.CookieResponse;
import ru.kaidan.backend.modules.auth.DTO.LoginRequest;
import ru.kaidan.backend.modules.auth.DTO.RegisterRequest;
import ru.kaidan.backend.modules.auth.custom.CustomUserDetails;
import ru.kaidan.backend.modules.auth.entities.TokenType;
import ru.kaidan.backend.modules.user.entities.RoleType;
import ru.kaidan.backend.modules.user.entities.UserEntity;
import ru.kaidan.backend.modules.user.repositories.UserRepository;
import ru.kaidan.backend.utils.exceptions.custom.ExpiredRefreshToken;
import ru.kaidan.backend.utils.exceptions.custom.InvalidRefreshTokenException;
import ru.kaidan.backend.utils.exceptions.custom.RefreshTokenMissingException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public CookieResponse registerUser(RegisterRequest registerRequest) {
        UserEntity user = new UserEntity();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(RoleType.ROLE_USER);
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

    public CookieResponse refreshToken(HttpServletRequest request) {
        String refreshToken = jwtService.getTokenFromCookies(request, jwtService.refreshCookieName);
        if (refreshToken == null) {
            throw new RefreshTokenMissingException("Refresh token is missing");
        }

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, customUserDetails)) {
            throw new ExpiredRefreshToken("Expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(customUserDetails);
        jwtService.revokeAllUserTokens(user, TokenType.ACCESS);
        jwtService.saveUserToken(user, newAccessToken, TokenType.ACCESS);

        return jwtService.buildCookies(newAccessToken, refreshToken);
    }
}
