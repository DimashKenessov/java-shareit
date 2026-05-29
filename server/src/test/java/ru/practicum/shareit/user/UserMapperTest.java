package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserMapperTest {

    @Test
    void toUserDto_mapsCorrectly() {
        User user = new User(1L, "Test", "test@test.com");
        UserDto dto = UserMapper.toUserDto(user);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getName());
        assertEquals("test@test.com", dto.getEmail());
    }

    @Test
    void toUser_mapsCorrectly() {
        UserDto dto = new UserDto(1L, "Test", "test@test.com");
        User user = UserMapper.toUser(dto);
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Test", user.getName());
        assertEquals("test@test.com", user.getEmail());
    }
}