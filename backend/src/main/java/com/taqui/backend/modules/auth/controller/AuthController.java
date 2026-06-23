package com.taqui.backend.modules.auth.controller;

import com.taqui.backend.modules.user.dto.UserLoginDTO;
import com.taqui.backend.modules.user.dto.UserResponseDTO;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.mapper.UserMapper;
import com.taqui.backend.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserLoginDTO request) {
        User createdUser = userService.createUser(request);
        UserResponseDTO body = userMapper.toResponse(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
