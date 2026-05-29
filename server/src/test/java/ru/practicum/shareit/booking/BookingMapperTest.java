package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingMapperTest {

    @Test
    void toBookingDto_mapsCorrectly() {
        User owner = new User(1L, "Owner", "owner@test.com");
        User booker = new User(2L, "Booker", "booker@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Good drill");
        item.setAvailable(true);
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        BookingDto dto = BookingMapper.toBookingDto(booking);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
        assertEquals(2L, dto.getBooker().getId());
        assertEquals(1L, dto.getItem().getId());
    }

    @Test
    void toBookingShortDto_mapsCorrectly() {
        User booker = new User(2L, "Booker", "booker@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setOwner(new User(1L, "Owner", "owner@test.com"));
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        BookingShortDto dto = BookingMapper.toBookingShortDto(booking);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getBookerId());
    }
}