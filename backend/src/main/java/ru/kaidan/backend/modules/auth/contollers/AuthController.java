package ru.kaidan.backend.modules.auth.contollers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kaidan.backend.modules.auth.DTO.CookieResponse;
import ru.kaidan.backend.modules.auth.DTO.LoginRequest;
import ru.kaidan.backend.modules.auth.DTO.RegisterRequest;
import ru.kaidan.backend.modules.auth.service.AuthService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest authRequest) {
        try {
            CookieResponse cookies = authService.loginUser(authRequest);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.getAccessCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, cookies.getRefreshCookie().toString())
                    .body(Map.of("message", "User successfully logged in"));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Wrong username or password", "error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            CookieResponse cookies = authService.registerUser(registerRequest);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.getAccessCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, cookies.getRefreshCookie().toString())
                    .body(Map.of("message", "User successfully registered"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        try {
            CookieResponse cookies = authService.refreshToken(request);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.getAccessCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, cookies.getRefreshCookie().toString())
                    .body(Map.of("message", "Access token successfully refreshed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
}