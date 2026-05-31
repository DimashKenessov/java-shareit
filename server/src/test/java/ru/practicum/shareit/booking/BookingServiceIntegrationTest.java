package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@test.com"));
        item = new Item();
        item.setName("Drill");
        item.setDescription("Good drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void create_validBooking_returnsBookingDto() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto result = bookingService.create(booker.getId(), dto);
        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void create_ownerBooksOwnItem_throwsNotFoundException() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        assertThrows(NotFoundException.class,
                () -> bookingService.create(owner.getId(), dto));
    }

    @Test
    void approve_validApproval_returnsApprovedBooking() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.create(booker.getId(), dto);
        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approve_wrongUser_throwsForbiddenException() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.create(booker.getId(), dto);
        assertThrows(Exception.class,
                () -> bookingService.approve(booker.getId(), booking.getId(), true));
    }

    @Test
    void approve_alreadyApproved_throwsValidationException() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.create(booker.getId(), dto);
        bookingService.approve(owner.getId(), booking.getId(), true);
        assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), booking.getId(), true));
    }

    @Test
    void getById_validAccess_returnsBookingDto() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto booking = bookingService.create(booker.getId(), dto);
        BookingDto result = bookingService.getById(booker.getId(), booking.getId());
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getAllByBooker_allStates_returnsBookings() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), dto);
        List<BookingDto> all = bookingService.getAllByBooker(booker.getId(), BookingState.ALL);
        List<BookingDto> future = bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE);
        List<BookingDto> waiting = bookingService.getAllByBooker(booker.getId(), BookingState.WAITING);
        assertNotNull(all);
        assertNotNull(future);
        assertNotNull(waiting);
    }

    @Test
    void getAllByOwner_allStates_returnsBookings() {
        BookingRequestDto dto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), dto);
        List<BookingDto> all = bookingService.getAllByOwner(owner.getId(), BookingState.ALL);
        List<BookingDto> future = bookingService.getAllByOwner(owner.getId(), BookingState.FUTURE);
        assertNotNull(all);
        assertNotNull(future);
    }
}