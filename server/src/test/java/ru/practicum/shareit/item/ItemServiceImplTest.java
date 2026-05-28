package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> itemService.create(99L, new ItemDto(null, "name", "desc", true, null)));
    }

    @Test
    void create_blankName_throwsValidationException() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class,
                () -> itemService.create(1L, new ItemDto(null, "", "desc", true, null)));
    }

    @Test
    void create_nullAvailable_throwsValidationException() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class,
                () -> itemService.create(1L, new ItemDto(null, "name", "desc", null, null)));
    }

    @Test
    void create_success_returnsItemDto() {
        User user = new User(1L, "Test", "test@test.com");
        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Drill");
        savedItem.setDescription("Good drill");
        savedItem.setAvailable(true);
        savedItem.setOwner(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.save(any())).thenReturn(savedItem);
        ItemDto result = itemService.create(1L,
                new ItemDto(null, "Drill", "Good drill", true, null));
        assertEquals("Drill", result.getName());
    }

    @Test
    void update_wrongOwner_throwsNotFoundException() {
        User owner = new User(1L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "Other", "other@test.com")));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class,
                () -> itemService.update(2L, 1L, new ItemDto(null, "name", "desc", true, null)));
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.getById(99L));
    }

    @Test
    void search_emptyText_returnsEmptyList() {
        assertEquals(0, itemService.search("").size());
    }
}