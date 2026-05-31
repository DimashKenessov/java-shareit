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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        assertThrows(NotFoundException.class,
                () -> bookingService.create(99L, dto));
    }

    @Test
    void create_itemNotFound_throwsNotFoundException() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        BookingRequestDto dto = new BookingRequestDto(99L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        assertThrows(NotFoundException.class,
                () -> bookingService.create(1L, dto));
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
        assertThrows(ValidationException.class,
                () -> bookingService.create(1L, dto));
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
        assertThrows(NotFoundException.class,
                () -> bookingService.create(1L, dto));
    }

    @Test
    void create_endBeforeStart_throwsValidationException() {
        User booker = new User(1L, "Booker", "booker@test.com");
        User owner = new User(2L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        BookingRequestDto dto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1));
        assertThrows(ValidationException.class,
                () -> bookingService.create(1L, dto));
    }

    @Test
    void approve_bookingNotFound_throwsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> bookingService.approve(1L, 99L, true));
    }

    @Test
    void approve_wrongOwner_throwsForbiddenException() {
        User owner = new User(1L, "Owner", "owner@test.com");
        User other = new User(2L, "Other", "other@test.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(Exception.class,
                () -> bookingService.approve(2L, 1L, true));
    }

    @Test
    void approve_alreadyProcessed_throwsValidationException() {
        User owner = new User(1L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class,
                () -> bookingService.approve(1L, 1L, true));
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
        assertThrows(NotFoundException.class,
                () -> bookingService.getById(99L, 1L));
    }

    @Test
    void getAllByBooker_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByBooker(99L, BookingState.ALL));
    }

    @Test
    void getAllByBooker_allStates_returnsBookings() {
        User booker = new User(1L, "Booker", "booker@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBooker(any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findByBookerAndStartBeforeAndEndAfter(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByBookerAndEndIsBefore(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByBookerAndStartIsAfter(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByBookerAndStatus(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertNotNull(bookingService.getAllByBooker(1L, BookingState.ALL));
        assertNotNull(bookingService.getAllByBooker(1L, BookingState.CURRENT));
        assertNotNull(bookingService.getAllByBooker(1L, BookingState.PAST));
        assertNotNull(bookingService.getAllByBooker(1L, BookingState.FUTURE));
        assertNotNull(bookingService.getAllByBooker(1L, BookingState.WAITING));
        assertNotNull(bookingService.getAllByBooker(1L, BookingState.REJECTED));
    }

    @Test
    void getAllByOwner_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByOwner(99L, BookingState.ALL));
    }

    @Test
    void getAllByOwner_noItems_throwsNotFoundException() {
        User owner = new User(1L, "Owner", "owner@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner(owner)).thenReturn(Collections.emptyList());
        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByOwner(1L, BookingState.ALL));
    }

    @Test
    void getAllByOwner_allStates_returnsBookings() {
        User owner = new User(1L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner(owner)).thenReturn(List.of(item));
        when(bookingRepository.findByItemIn(any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemInAndStartBeforeAndEndAfter(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemInAndEndIsBefore(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemInAndStartIsAfter(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemInAndStatus(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertNotNull(bookingService.getAllByOwner(1L, BookingState.ALL));
        assertNotNull(bookingService.getAllByOwner(1L, BookingState.CURRENT));
        assertNotNull(bookingService.getAllByOwner(1L, BookingState.PAST));
        assertNotNull(bookingService.getAllByOwner(1L, BookingState.FUTURE));
        assertNotNull(bookingService.getAllByOwner(1L, BookingState.WAITING));
        assertNotNull(bookingService.getAllByOwner(1L, BookingState.REJECTED));
    }
}