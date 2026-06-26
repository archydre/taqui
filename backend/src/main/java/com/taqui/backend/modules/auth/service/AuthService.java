package com.taqui.backend.modules.auth.service;

import com.taqui.backend.modules.user.dto.LoginRequestDTO;
import com.taqui.backend.modules.user.dto.LoginResponseDTO;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. valida email + senha; senha errada -> BadCredentialsException (-> 401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. recarrega o user pra montar os claims (a autenticação já garantiu que existe)
        User user = userRepository.findByEmail(request.email()).orElseThrow();

        // 3. monta os claims do JWT
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("taqui")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(user.getUserId().toString())
                .claim("username", user.getUsername())
                .build();

        // 4. assina (HS256) e serializa o token
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponseDTO(token, "Bearer", expirationMinutes);
    }
}