package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void create_validUser_returnsUserDto() {
        UserDto dto = new UserDto(null, "Test", "test@test.com");
        UserDto result = userService.create(dto);
        assertNotNull(result.getId());
        assertEquals("Test", result.getName());
    }

    @Test
    void create_duplicateEmail_throwsConflictException() {
        userService.create(new UserDto(null, "User1", "same@test.com"));
        assertThrows(ConflictException.class,
                () -> userService.create(new UserDto(null, "User2", "same@test.com")));
    }

    @Test
    void update_validUpdate_returnsUpdatedDto() {
        UserDto created = userService.create(new UserDto(null, "Old", "old@test.com"));
        UserDto updated = userService.update(created.getId(),
                new UserDto(null, "New", null));
        assertEquals("New", updated.getName());
        assertEquals("old@test.com", updated.getEmail());
    }

    @Test
    void update_duplicateEmail_throwsConflictException() {
        userService.create(new UserDto(null, "User1", "email1@test.com"));
        UserDto user2 = userService.create(new UserDto(null, "User2", "email2@test.com"));
        assertThrows(ConflictException.class,
                () -> userService.update(user2.getId(),
                        new UserDto(null, null, "email1@test.com")));
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_returnsAllUsers() {
        userService.create(new UserDto(null, "User1", "u1@test.com"));
        userService.create(new UserDto(null, "User2", "u2@test.com"));
        List<UserDto> users = userService.getAll();
        assertNotNull(users);
    }

    @Test
    void delete_validUser_deletesSuccessfully() {
        UserDto created = userService.create(new UserDto(null, "ToDelete", "delete@test.com"));
        userService.delete(created.getId());
        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }

    @Test
    void delete_notFound_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.delete(999L));
    }
}