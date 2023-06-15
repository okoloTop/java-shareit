package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.CreateUserDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(
            @Value("${shareit-server.url}") String serverUrl,
            RestTemplateBuilder builder
    ) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()

        );
    }

    public ResponseEntity<Object> registerUser(CreateUserDto dto) {
        return post("", null, null, dto);
    }

    public ResponseEntity<Object> updateUser(long userId, CreateUserDto dto) {
        return patch("/" + userId, null, null, dto);
    }

    public ResponseEntity<Object> getUser(long userId) {
        return get("/" + userId, userId);
    }

    public ResponseEntity<Object> findAll() {
        return get("");
    }

    public void deleteUser(long userId) {
        delete("/" + userId, null, null);
    }
}
