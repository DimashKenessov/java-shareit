package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serialize() throws Exception {
        UserDto dto = new UserDto(1L, "Test", "test@test.com");
        var result = json.write(dto);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.email");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test@test.com");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"Test\",\"email\":\"test@test.com\"}";
        UserDto dto = json.parse(content).getObject();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test");
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
    }
}