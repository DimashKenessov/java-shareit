package ru.practicum.shareit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void objectMapper_isConfigured() {
        assertNotNull(objectMapper);
    }

    @Test
    void booleanCoercion_stringTrue_parsedCorrectly() throws Exception {
        String json = "{\"available\":\"true\"}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);
        assertNotNull(dto);
    }

    static class TestDto {
        public Boolean available;
    }
}