
package com.rushikesh.smartfinance.service;

import com.rushikesh.smartfinance.entity.User;
import com.rushikesh.smartfinance.exception.EmailAlreadyRegisteredException;
import com.rushikesh.smartfinance.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_savesUserWithEncodedPasswordAndUserRole() {
        // Arrange
        when(userRepository.existsByEmail("rishi@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("myPassword123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.registerUser(
                "Rishi",
                "rishi@example.com",
                "myPassword123"
        );

        // Assert
        assertThat(result.getName()).isEqualTo("Rishi");
        assertThat(result.getEmail()).isEqualTo("rishi@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getPassword()).isNotEqualTo("myPassword123");
        assertThat(result.getRole()).isEqualTo("USER");

        verify(userRepository).existsByEmail("rishi@example.com");
        verify(passwordEncoder).encode("myPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_normalizesEmail() {
        // Arrange
        when(userRepository.existsByEmail("rishi@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("myPassword123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.registerUser(
                " Rishi ",
                "  Rishi@Example.com  ",
                "myPassword123"
        );

        // Assert
        assertThat(result.getName()).isEqualTo("Rishi");
        assertThat(result.getEmail()).isEqualTo("rishi@example.com");

        verify(userRepository).existsByEmail("rishi@example.com");
    }

    @Test
    void registerUser_throwsExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("rishi@example.com"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() ->
                userService.registerUser(
                        "Rishi",
                        "rishi@example.com",
                        "myPassword123"
                )
        ).isInstanceOf(EmailAlreadyRegisteredException.class);

        // No password hashing or saving should happen
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }
}
