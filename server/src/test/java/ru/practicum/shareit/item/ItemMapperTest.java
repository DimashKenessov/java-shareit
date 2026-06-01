package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemMapperTest {

    @Test
    void toItemDto_withoutRequest_mapsCorrectly() {
        User owner = new User(1L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Good drill");
        item.setAvailable(true);
        item.setOwner(owner);
        ItemDto dto = ItemMapper.toItemDto(item);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Drill", dto.getName());
        assertNull(dto.getRequestId());
    }

    @Test
    void toItemDto_withRequest_mapsRequestId() {
        User owner = new User(1L, "Owner", "owner@test.com");
        User requestor = new User(2L, "Requestor", "req@test.com");
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Need drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Good drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        ItemDto dto = ItemMapper.toItemDto(item);
        assertEquals(5L, dto.getRequestId());
    }

    @Test
    void toItemWithBookingsDto_mapsCorrectly() {
        User owner = new User(1L, "Owner", "owner@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Good drill");
        item.setAvailable(true);
        item.setOwner(owner);
        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Drill", dto.getName());
    }

    @Test
    void toCommentDto_mapsCorrectly() {
        User author = new User(1L, "Author", "author@test.com");
        Item item = new Item();
        item.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        CommentDto dto = ItemMapper.toCommentDto(comment);
        assertNotNull(dto);
        assertEquals("Great item!", dto.getText());
        assertEquals("Author", dto.getAuthorName());
    }
}