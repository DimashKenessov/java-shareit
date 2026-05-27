package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getAllByOwner_returnsItemsForOwner() {
        User user = userRepository.save(new User(null, "Owner", "owner@test.com"));
        ItemDto itemDto = new ItemDto(null, "Drill", "Power drill", true, null);
        itemService.create(user.getId(), itemDto);
        List<ItemWithBookingsDto> items = itemService.getAllByOwner(user.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Drill", items.get(0).getName());
    }
}