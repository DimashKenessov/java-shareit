package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker(User booker, Sort sort);

    List<Booking> findByBookerAndStartBeforeAndEndAfter(
            User booker, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerAndEndIsBefore(User booker, LocalDateTime end, Sort sort);

    List<Booking> findByBookerAndStartIsAfter(User booker, LocalDateTime start, Sort sort);

    List<Booking> findByBookerAndStatus(User booker, BookingStatus status, Sort sort);

    List<Booking> findByItemIn(List<Item> items, Sort sort);

    List<Booking> findByItemInAndStartBeforeAndEndAfter(
            List<Item> items, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItemInAndEndIsBefore(List<Item> items, LocalDateTime end, Sort sort);

    List<Booking> findByItemInAndStartIsAfter(List<Item> items, LocalDateTime start, Sort sort);

    List<Booking> findByItemInAndStatus(List<Item> items, BookingStatus status, Sort sort);

    List<Booking> findByItemAndStatusOrderByStartAsc(Item item, BookingStatus status);

    List<Booking> findByItemInAndStatusOrderByStartAsc(List<Item> items, BookingStatus status);
}