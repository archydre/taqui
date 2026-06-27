package com.taqui.backend.modules.user.controller;

import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;
import com.taqui.backend.modules.user.dto.UserResponseDTO;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.mapper.UserMapper;
import com.taqui.backend.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserPublicInfoDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        return ResponseEntity.ok(userMapper.toUserPublicInfo(user));
    }
}
