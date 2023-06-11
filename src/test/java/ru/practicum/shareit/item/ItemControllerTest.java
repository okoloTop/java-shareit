package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private Item item;
    private ItemDto addedItem;
    private ItemDto addedItem2;
    private ItemBookingDto itemBookingDto;


    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@example.com")
                .build();
        item = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .owner(user)
                .build();
        addedItem = new ItemDto(1L, "name", "description", true, null);

        addedItem2 = new ItemDto(1L, "name2", "description2", true, null);

        itemBookingDto = new ItemBookingDto(2L, "item", "item desc", true, null, null, new ArrayList<>(), null);
    }

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ItemMapper.itemToDto(item));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(addedItem))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    void createItemEmptyNameTest() throws Exception {
        addedItem.setName(null);
        ItemDto wrong = addedItem;
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ItemMapper.itemToDto(item));

        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(wrong))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createItemEmptyDescriptionTest() throws Exception {
        addedItem.setDescription(null);
        ItemDto wrong = addedItem;
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ItemMapper.itemToDto(item));

        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(wrong))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void createItemEmptyAvailableTest() throws Exception {
        addedItem.setAvailable(null);
        ItemDto wrong = addedItem;
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ItemMapper.itemToDto(item));

        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(wrong))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ItemMapper.itemToDto(item));

        mockMvc.perform(patch("/items/{id}", 1L)
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(addedItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void updateItemWrongUserTest() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ItemMapper.itemToDto(item));

        mockMvc.perform(patch("/items/{id}", 1L)
                .content(objectMapper.writeValueAsString(addedItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isInternalServerError());
    }

    @Test
    void findByItemIdTest() throws Exception {
        when(itemService.findItemBookingById(anyLong(), anyLong()))
                .thenReturn(itemBookingDto);

        mockMvc.perform(get("/items/{id}", 1L)
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void findByWrongItemIdTest() throws Exception {
        when(itemService.findItemBookingById(999L, 1L))
                .thenThrow(FoundException.class);

        mockMvc.perform(get("/items/{id}", 999L)
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void findAllByUserIdTest() throws Exception {
        List<ItemBookingDto> items = List.of(itemBookingDto);
        when(itemService.findAllByUserId(1L))
                .thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").value("item"))
                .andExpect(jsonPath("$[0].description").value("item desc"));

    }

    @Test
    void searchTest() throws Exception {
        List<ItemDto> items = List.of(addedItem, addedItem2);
        when(itemService.findItemsByQueryText("name"))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].name").value("name2"));
    }

    @Test
    void searchEmptyTest() throws Exception {

        when(itemService.findItemsByQueryText("name"))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/items/search")
                .param("text", "")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void createCommentTest() throws Exception {

        CommentInDto commentInDto = new CommentInDto("first comment2");

        CommentDto commentDto = new CommentDto(1L, "first comment", "user", LocalDateTime.now());

        when(itemService.addCommentToItem(anyLong(), anyLong(), any(CommentInDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(commentInDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(commentDto)));
    }
}
