package io.github.ale.tripscheduler.service;

import io.github.ale.tripscheduler.dto.request.RegisterRequest;
import io.github.ale.tripscheduler.entity.UserAccount;
import io.github.ale.tripscheduler.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository,  PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        UserAccount user = UserAccount.builder()
                .username(request.getUsername())
                .password(encryptedPassword)
                .build();

        userAccountRepository.save(user);
    }
}