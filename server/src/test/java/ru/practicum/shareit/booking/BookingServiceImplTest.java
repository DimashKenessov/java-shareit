package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void create_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        BookingRequestDto dto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        assertThrows(NotFoundException.class, () -> bookingService.create(99L, dto));
    }

    @Test
    void create_itemNotAvailable_throwsValidationException() {
        User user = new User(1L, "Test", "test@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setAvailable(false);
        item.setOwner(new User(2L, "Owner", "owner@test.com"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        BookingRequestDto dto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        assertThrows(ValidationException.class, () -> bookingService.create(1L, dto));
    }

    @Test
    void create_ownerBooksOwnItem_throwsNotFoundException() {
        User owner = new User(1L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        BookingRequestDto dto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        assertThrows(NotFoundException.class, () -> bookingService.create(1L, dto));
    }

    @Test
    void approve_bookingNotFound_throwsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.approve(1L, 99L, true));
    }

    @Test
    void getById_accessDenied_throwsNotFoundException() {
        User owner = new User(1L, "Owner", "owner@test.com");
        User booker = new User(2L, "Booker", "booker@test.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> bookingService.getById(99L, 1L));
    }
}