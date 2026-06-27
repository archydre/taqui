package com.taqui.backend.modules.user.controller;

import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.mapper.UserMapper;
import com.taqui.backend.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{username}")
    public ResponseEntity<UserPublicInfoDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        return ResponseEntity.ok(userMapper.toUserPublicInfo(user));
    }
}
