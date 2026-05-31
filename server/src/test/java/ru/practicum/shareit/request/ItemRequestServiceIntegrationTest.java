package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(null, "User", "user@test.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@test.com"));
    }

    @Test
    void create_validRequest_returnsDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDto result = requestService.create(user.getId(), dto);
        assertNotNull(result.getId());
        assertEquals("Need a drill", result.getDescription());
    }

    @Test
    void create_userNotFound_throwsNotFoundException() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need something");
        assertThrows(NotFoundException.class, () -> requestService.create(999L, dto));
    }

    @Test
    void getOwn_returnsOwnRequests() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        requestService.create(user.getId(), dto);
        List<ItemRequestDto> result = requestService.getOwn(user.getId());
        assertEquals(1, result.size());
    }

    @Test
    void getAll_returnsOtherUsersRequests() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        requestService.create(user.getId(), dto);
        List<ItemRequestDto> result = requestService.getAll(otherUser.getId());
        assertEquals(1, result.size());
    }

    @Test
    void getAll_doesNotReturnOwnRequests() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        requestService.create(user.getId(), dto);
        List<ItemRequestDto> result = requestService.getAll(user.getId());
        assertEquals(0, result.size());
    }

    @Test
    void getById_validRequest_returnsDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDto created = requestService.create(user.getId(), dto);
        ItemRequestDto result = requestService.getById(user.getId(), created.getId());
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> requestService.getById(user.getId(), 999L));
    }
}