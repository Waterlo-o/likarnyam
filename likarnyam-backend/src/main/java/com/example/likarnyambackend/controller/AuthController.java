package com.example.likarnyambackend.controller;

import com.example.likarnyambackend.dto.request.ChangePasswordRequest;
import com.example.likarnyambackend.repository.UserRepository;
import com.example.likarnyambackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

record LoginRequest(String email, String password) {}
record LoginResponse(String token) {}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PatchMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok("Password changed");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getAuthInfo(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole().getName());
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}