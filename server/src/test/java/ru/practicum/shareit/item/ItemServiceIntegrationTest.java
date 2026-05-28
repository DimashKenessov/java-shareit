package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
    }

    @Test
    void getAllByOwner_returnsItemsForOwner() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Power drill", true, null);
        itemService.create(owner.getId(), itemDto);
        List<ItemWithBookingsDto> items = itemService.getAllByOwner(owner.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Drill", items.get(0).getName());
    }

    @Test
    void create_andGetById_returnsCorrectItem() {
        ItemDto itemDto = new ItemDto(null, "Hammer", "Big hammer", true, null);
        ItemDto created = itemService.create(owner.getId(), itemDto);
        ItemWithBookingsDto found = itemService.getById(created.getId());
        assertEquals("Hammer", found.getName());
    }

    @Test
    void search_availableItem_found() {
        itemService.create(owner.getId(),
                new ItemDto(null, "SuperDrill", "Awesome drill", true, null));
        List<ItemDto> results = itemService.search("SuperDrill");
        assertEquals(1, results.size());
    }

    @Test
    void search_unavailableItem_notFound() {
        itemService.create(owner.getId(),
                new ItemDto(null, "HiddenItem", "Not available", false, null));
        List<ItemDto> results = itemService.search("HiddenItem");
        assertEquals(0, results.size());
    }

    @Test
    void update_validUpdate_returnsUpdatedItem() {
        ItemDto created = itemService.create(owner.getId(),
                new ItemDto(null, "OldName", "Old desc", true, null));
        ItemDto updated = itemService.update(owner.getId(), created.getId(),
                new ItemDto(null, "NewName", null, null, null));
        assertEquals("NewName", updated.getName());
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemService.getById(999L));
    }

    @Test
    void getAllByOwner_userNotFound_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemService.getAllByOwner(999L));
    }
}