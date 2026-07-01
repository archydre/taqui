package com.taqui.backend.modules.auth.controller;

import com.taqui.backend.modules.auth.exception.EmailDeliveryException;
import com.taqui.backend.modules.auth.service.AuthService;
import com.taqui.backend.modules.auth.service.EmailVerificationService;
import com.taqui.backend.modules.user.dto.LoginRequestDTO;
import com.taqui.backend.modules.user.dto.LoginResponseDTO;
import com.taqui.backend.modules.user.dto.RegisterRequestDTO;
import com.taqui.backend.modules.user.dto.UserResponseDTO;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.mapper.UserMapper;
import com.taqui.backend.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        User createdUser = userService.createUser(request);
        // envio best-effort: a conta já está persistida, uma falha no email não desfaz o cadastro
        try {
            emailVerificationService.sendVerificationEmail(createdUser);
        } catch (EmailDeliveryException ex) {
            log.warn("Falha ao enviar email de verificação para {}: {}", createdUser.getEmail(), ex.getMessage());
        }
        UserResponseDTO body = userMapper.toResponse(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }
}