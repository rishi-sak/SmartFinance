
package com.rushikesh.smartfinance.repository;

import com.rushikesh.smartfinance.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser_generatesId() {
        // Arrange
        User user = new User(
                "Rishi",
                "rishi@example.com",
                "encoded-password",
                "USER"
        );

        // Act
        User savedUser = userRepository.saveAndFlush(user);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("rishi@example.com");
    }

    @Test
    void findByEmail_returnsSavedUser() {
        // Arrange
        User user = new User(
                "Rishi",
                "rishi@example.com",
                "encoded-password",
                "USER"
        );

        userRepository.saveAndFlush(user);

        // Act
        Optional<User> result =
                userRepository.findByEmail("rishi@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Rishi");
        assertThat(result.get().getRole()).isEqualTo("USER");
    }

    @Test
    void findByEmail_returnsEmptyWhenEmailDoesNotExist() {
        // Act
        Optional<User> result =
                userRepository.findByEmail("missing@example.com");

        // Assert
        assertThat(result).isEmpty();
    }


    @Test
    void saveUser_rejectsDuplicateEmail() {
        // Arrange: save the first user
        User firstUser = new User(
                "Rishi",
                "rishi@example.com",
                "encoded-password",
                "USER"
        );

        userRepository.saveAndFlush(firstUser);

        // Act + Assert: attempt to save the same email again
        User duplicateUser = new User(
                "Another User",
                "rishi@example.com",
                "another-encoded-password",
                "USER"
        );

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}
