package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_validItem_returns201() throws Exception {
        ItemDto input = new ItemDto(null, "Drill", "Good drill", true, null);
        ItemDto output = new ItemDto(1L, "Drill", "Good drill", true, null);
        when(itemService.create(anyLong(), any())).thenReturn(output);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_validItem_returns200() throws Exception {
        ItemDto output = new ItemDto(1L, "Updated", "desc", true, null);
        when(itemService.update(anyLong(), anyLong(), any())).thenReturn(output);
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ItemDto(null, "Updated", null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void getById_returns200() throws Exception {
        ItemWithBookingsDto output = new ItemWithBookingsDto(
                1L, "Drill", "Good drill", true, null, null, List.of());
        when(itemService.getById(1L)).thenReturn(output);
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByOwner_returns200() throws Exception {
        when(itemService.getAllByOwner(1L)).thenReturn(List.of());
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void search_emptyText_returnsEmptyList() throws Exception {
        when(itemService.search("")).thenReturn(List.of());
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void search_withText_returnsItems() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(
                new ItemDto(1L, "Drill", "Good drill", true, null)));
        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addComment_returns200() throws Exception {
        CommentDto output = new CommentDto(1L, "Great!", "Author",
                LocalDateTime.now());
        when(itemService.addComment(anyLong(), anyLong(), anyString())).thenReturn(output);
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great!\"}"))
                .andExpect(status().isOk());
    }
}