package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.util.HttpHeaderConstants;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> create(
			@RequestHeader(HttpHeaderConstants.X_SHARER_USER_ID) long userId,
			@Valid @RequestBody BookItemRequestDto requestDto) {
		return bookingClient.bookItem(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(
			@RequestHeader(HttpHeaderConstants.X_SHARER_USER_ID) long userId,
			@PathVariable long bookingId,
			@RequestParam boolean approved) {
		return bookingClient.approve(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getById(
			@RequestHeader(HttpHeaderConstants.X_SHARER_USER_ID) long userId,
			@PathVariable long bookingId) {
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllByBooker(
			@RequestHeader(HttpHeaderConstants.X_SHARER_USER_ID) long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
			@Positive @RequestParam(defaultValue = "10") Integer size) {
		BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
		return bookingClient.getBookings(userId, bookingState, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllByOwner(
			@RequestHeader(HttpHeaderConstants.X_SHARER_USER_ID) long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
			@Positive @RequestParam(defaultValue = "10") Integer size) {
		BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
		return bookingClient.getAllByOwner(userId, bookingState, from, size);
	}
}