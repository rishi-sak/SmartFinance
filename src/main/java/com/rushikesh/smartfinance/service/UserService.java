
package com.rushikesh.smartfinance.service;

import com.rushikesh.smartfinance.entity.User;
import com.rushikesh.smartfinance.exception.EmailAlreadyRegisteredException;
import com.rushikesh.smartfinance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(
            String name,
            String email,
            String rawPassword) {

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException(
                    "An account with this email already exists."
            );
        }

        User user = new User(
                name.trim(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                "USER"
        );

        return userRepository.save(user);
    }
}
