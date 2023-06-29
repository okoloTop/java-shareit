package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRepositoryTest {
    private final UserRepository userRepository;
    private final TestEntityManager tem;

    @Test
    void findByEmailTest() {
        User user = User.builder()
                .name("user")
                .email("user@example.com")
                .build();
        tem.persist(user);

        User user2 = userRepository.findUserByEmail("user@example.com").get();
        Assertions.assertThat(user2.getEmail()).isEqualTo("user@example.com");

    }
}
