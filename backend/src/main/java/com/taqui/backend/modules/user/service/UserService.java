package com.taqui.backend.modules.user.service;

import com.taqui.backend.modules.user.dto.RegisterRequestDTO;
import com.taqui.backend.modules.user.mapper.UserMapper;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(RegisterRequestDTO registerRequestDTO) {
        if(userRepository.existsByEmail(registerRequestDTO.email())) {
            throw new EmailAlreadyExistsException("Email já está sendo utilizado");
        }
        User createdUser = userMapper.toEntity(registerRequestDTO);
        createdUser.setPassword(passwordEncoder.encode(createdUser.getPassword()));
        return userRepository.save(createdUser);
    }
}
