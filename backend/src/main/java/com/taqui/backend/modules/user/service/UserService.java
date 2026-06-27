package com.taqui.backend.modules.user.service;

import com.taqui.backend.modules.user.dto.RegisterRequestDTO;
import com.taqui.backend.modules.user.mapper.UserMapper;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.EmailAlreadyExistsException;
import com.taqui.backend.modules.user.exception.UsernameAlreadyExistsException;
import com.taqui.backend.modules.user.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> RESERVED_USERNAMES =
            Set.of("me", "admin", "root", "support", "api", "auth");

    @Transactional
    public User createUser(RegisterRequestDTO registerRequestDTO) {
        if(userRepository.existsByEmail(registerRequestDTO.email())) {
            throw new EmailAlreadyExistsException("Email já está sendo utilizado");
        }
        if(RESERVED_USERNAMES.contains(registerRequestDTO.username())) {
            throw new UsernameAlreadyExistsException("Username indisponível");
        }
        if(userRepository.existsByUsername(registerRequestDTO.username())) {
            throw new UsernameAlreadyExistsException("Username já está sendo utilizado");
        }
        User createdUser = userMapper.toEntity(registerRequestDTO);
        createdUser.setPassword(passwordEncoder.encode(createdUser.getPassword()));
        return userRepository.save(createdUser);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + username));
    }

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        String term = query == null ? "" : query.trim();
        if (term.length() < 2) {
            return Page.empty(pageable);
        }
        return userRepository
                .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(term, term, pageable);
    }
}
