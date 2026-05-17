package ru.practicum.shareit.item;

import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Name cannot be blank");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Description cannot be blank");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Available cannot be null");
        }
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        if (!existing.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Item not found for user: " + userId);
        }
        if (itemDto.getName() != null) existing.setName(itemDto.getName());
        if (itemDto.getDescription() != null) existing.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) existing.setAvailable(itemDto.getAvailable());
        return ItemMapper.toItemDto(itemRepository.save(existing));
    }

    @Override
    public ItemWithBookingsDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);
        List<Comment> comments = commentRepository.findAllByItem(item);
        dto.setComments(comments.stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    public List<ItemWithBookingsDto> getAllByOwner(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<Item> items = itemRepository.findAllByOwner(owner);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> allBookings = bookingRepository
                .findByItemInAndStatusOrderByStartAsc(items, BookingStatus.APPROVED);
        Map<Item, List<Booking>> bookingsByItem = allBookings.stream()
                .collect(Collectors.groupingBy(Booking::getItem));
        List<Comment> allComments = commentRepository.findAllByItemIn(items);
        Map<Item, List<Comment>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getItem));
        return items.stream().map(item -> {
            ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);
            List<Booking> bookings = bookingsByItem.getOrDefault(item, List.of());
            bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .reduce((first, second) -> second)
                    .ifPresent(b -> dto.setLastBooking(BookingMapper.toBookingShortDto(b)));
            bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .findFirst()
                    .ifPresent(b -> dto.setNextBooking(BookingMapper.toBookingShortDto(b)));
            List<CommentDto> comments = commentsByItem.getOrDefault(item, List.of())
                    .stream()
                    .map(ItemMapper::toCommentDto)
                    .collect(Collectors.toList());
            dto.setComments(comments);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return itemRepository.search(text).stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        boolean hasBooked = bookingRepository
                .findByBookerAndEndIsBefore(author, LocalDateTime.now(),
                        Sort.by(Sort.Direction.DESC, "start"))
                .stream()
                .anyMatch(b -> b.getItem().getId().equals(itemId)
                        && b.getStatus() == BookingStatus.APPROVED);
        if (!hasBooked) {
            throw new ValidationException("User has not booked this item");
        }
        if (text == null || text.isBlank()) {
            throw new ValidationException("Comment cannot be blank");
        }
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return ItemMapper.toCommentDto(commentRepository.save(comment));
    }
}