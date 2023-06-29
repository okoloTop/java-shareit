package ru.practicum.shareit.item;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestRepository itemRequestRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private User user2;
    private User user3;
    private ItemDto itemDto;
    private Item item;
    private ItemDto itemDtoForAssert;
    private ItemBookingDto itemBookingForAssert;
    private Item item2;


    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).name("user").email("user@example.com")
                .build();
        user2 = User.builder()
                .id(2L).name("user2").email("user2@example.com")
                .build();
        user3 = User.builder()
                .id(3L).name("user3").email("user3@example.com")
                .build();
        itemDto = new ItemDto();
        itemDto.setName("молоток");
        itemDto.setDescription("крепкий молоток");
        itemDto.setAvailable(true);

        item = Item.builder()
                .id(1L)
                .name("молоток").description("крепкий молоток").available(true)
                .owner(user)
                .build();

        itemDtoForAssert = new ItemDto();
        itemDtoForAssert.setId(1L);
        itemDtoForAssert.setName("молоток");
        itemDtoForAssert.setDescription("крепкий молоток");
        itemDtoForAssert.setAvailable(true);
        itemDtoForAssert.setRequestId(null);

        itemBookingForAssert = new ItemBookingDto();
        itemBookingForAssert.setId(1L);
        itemBookingForAssert.setName("молоток");
        itemBookingForAssert.setDescription("крепкий молоток");
        itemBookingForAssert.setAvailable(true);
        itemBookingForAssert.setComments(new ArrayList<>());
        itemBookingForAssert.setLastBooking(null);
        itemBookingForAssert.setNextBooking(null);

        item2 = Item.builder()
                .id(2L)
                .name("drill").description("best drill").available(false)
                .owner(user)
                .build();
    }

    @Test
    void createItemNormalTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto added = itemService.createItem(1L, itemDto);

        Assertions.assertThat(added)
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(itemDtoForAssert);
        Assertions.assertThat(added.getRequestId()).isNull();

        Mockito.verify(userRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void createItemWithRequestIdNormalTest() {
        itemDto.setRequestId(1L);
        itemDtoForAssert.setRequestId(1L);

        User requestor = User.builder()
                .id(2L).name("requestor").email("requestor@example.com")
                .build();

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setItems(List.of(item));
        itemRequest.setRequestor(requestor);
        itemRequest.setDescription("хороший молоток");
        itemRequest.setCreated(LocalDateTime.now());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        item.setRequest(itemRequest);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto added = itemService.createItem(1L, itemDto);


        Assertions.assertThat(added)
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(itemDtoForAssert);
        Assertions.assertThat(added.getRequestId()).isEqualTo(itemRequest.getId());

        Mockito.verify(userRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository, itemRequestRepository);
    }

    @Test
    void saveItem_NotExistUser_ReturnNotFoundException() {
        when(userRepository.findById(999L)).thenThrow(FoundException.class);

        Throwable thrown = Assertions.catchException(() -> itemService.createItem(999L, itemDto));

        Assertions.assertThat(thrown)
                .isInstanceOf(FoundException.class);

        Mockito.verify(userRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoInteractions(itemRepository, itemRequestRepository);
    }

    @Test
    void createItemWrongRequestTest() {
        itemDto.setRequestId(999L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(999L))
                .thenThrow(FoundException.class);

        Throwable thrown = Assertions.catchException(() -> itemService.createItem(1L, itemDto));

        Assertions.assertThat(thrown)
                .isInstanceOf(FoundException.class);

        Mockito.verify(userRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(userRepository, itemRequestRepository);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void updateItemNormalTest() throws AccessDeniedException {
        itemDto.setId(1L);
        itemDto.setName("обновленный молоток");
        itemDto.setDescription("хороший обновленный молоток");
        itemDto.setAvailable(false);
        ItemDto dataToUpdate = itemDto;

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto updated = itemService.updateItem(1L, 1L, dataToUpdate);

        Assertions.assertThat(updated.getName()).isEqualTo("обновленный молоток");
        Assertions.assertThat(updated.getDescription()).isEqualTo("хороший обновленный молоток");
        Assertions.assertThat(updated.getAvailable()).isEqualTo(false);

        Mockito.verify(itemRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateWrongItemTest() {
        itemDto.setId(999L);
        itemDto.setName("обновленный молоток");
        itemDto.setDescription("хороший обновленный молоток");
        itemDto.setAvailable(false);
        ItemDto dataToUpdate = itemDto;

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenThrow(FoundException.class);

        Throwable thrown = Assertions.catchException(() -> itemService.updateItem(1L, 999L, dataToUpdate));

        Assertions.assertThat(thrown)
                .isInstanceOf(FoundException.class);

        Mockito.verify(itemRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItemWrongUserTest() {
        itemDto.setId(1L);
        itemDto.setName("обновленный молоток");
        itemDto.setDescription("хороший обновленный молоток");
        itemDto.setAvailable(false);
        ItemDto dataToUpdate = itemDto;

        Throwable thrown = Assertions.catchException(() -> itemService.updateItem(99L, 1L, dataToUpdate));

        Assertions.assertThat(thrown)
                .isInstanceOf(FoundException.class);


        Mockito.verify(userRepository, Mockito.times(1)).findById(99L);
        Mockito.verifyNoMoreInteractions(itemRepository);
    }


    @Test
    void findItemByIdNormalTest() {
        itemBookingForAssert.setOwner(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByCommentAndItemId(1L))
                .thenReturn(new ArrayList<>());

        ItemBookingDto actual = itemService.findItemBookingById(1L, 1L);

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(itemBookingForAssert);
    }

    @Test
    void findItemByIdWithBookingNormalTest() {
        BookingItemDto last = new BookingItemDto(1L, 2L, LocalDateTime.now().minusDays(4).withNano(0), LocalDateTime.now().minusDays(2).withNano(0), Status.APPROVED);
        BookingItemDto next = new BookingItemDto(2L, 3L, LocalDateTime.now().plusDays(1).withNano(0), LocalDateTime.now().plusDays(2).withNano(0), Status.APPROVED);
        Booking bookingLast = new Booking(1L, LocalDateTime.now().minusDays(4).withNano(0), LocalDateTime.now().minusDays(2).withNano(0), item, user2, Status.APPROVED);
        Booking bookingNext = new Booking(2L, LocalDateTime.now().plusDays(1).withNano(0), LocalDateTime.now().plusDays(2).withNano(0), item, user3, Status.APPROVED);
        itemBookingForAssert.setNextBooking(next);
        itemBookingForAssert.setLastBooking(last);
        itemBookingForAssert.setOwner(user);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn((List.of(bookingLast)));
        when(bookingRepository.findAllByItemIdOrderByStartAsc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(bookingNext));

        ItemBookingDto actual = itemService.findItemBookingById(1L, 1L);

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(itemBookingForAssert);
    }

    @Test
    void findItemWithWrongIdTest() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchException(() -> itemService.findItemBookingById(999L, 1L));

        Assertions.assertThat(thrown)
                .isInstanceOf(FoundException.class)
                .hasMessage("Вещь не найдена");

        Mockito.verify(itemRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void findAllItemsByUserIdNormalTest() {
        List<Item> items = List.of(item, item2);

        when(itemRepository.findAllByOwnerId(anyLong()))
                .thenReturn(items);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        List<ItemBookingDto> result = itemService.findAllByUserId(1L);

        Assertions.assertThat(result)
                .hasSize(2);

        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(anyLong());
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void searchItemByTextNormalTest() {
        List<Item> items = List.of(item, item2);
        when(itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase(any()))
                .thenReturn(items);

        List<ItemDto> result = itemService.findItemsByQueryText("молоток");

        Assertions.assertThat(result)
                .hasSize(2);

        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemByAvailableAndQueryContainWithIgnoreCase(any());
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void searchItemEmptyTextTest() {
        List<ItemDto> result = itemService.findItemsByQueryText("");

        Assertions.assertThat(result)
                .hasSize(0).isEqualTo(new ArrayList<ItemDto>());

        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void addEmptyCommentToItemTest() {
        CommentInDto commentNewDto = new CommentInDto();
        commentNewDto.setText("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when((itemRepository.findById(1L))).thenReturn(Optional.of(item));
        Throwable thrown = Assertions.catchException(() -> itemService
                .addCommentToItem(1L, 1L, commentNewDto));

        Assertions.assertThat(thrown)
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void addCommentToItemNormalTest() {
        LocalDateTime now = LocalDateTime.of(2023, 06, 01, 01, 12, 12);
        Booking booking = Booking.builder()
                .id(1L)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .status(Status.APPROVED)
                .item(item)
                .booker(user2)
                .build();

        CommentInDto commentNewDto = new CommentInDto();
        commentNewDto.setText("новый комментарий");

        Comment comment = new Comment();
        comment.setItem(item.getId());
        comment.setCreated(now);
        comment.setText("новый комментарий");
        comment.setAuthorId(user.getId());

        CommentDto commentDtoMustBe = CommentMapper.commentToDto(comment);
        commentDtoMustBe.setAuthorName("user2");
        commentDtoMustBe.setCreated(now);

        List<Booking> bookingList = List.of(booking);

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when((itemRepository.findById(1L))).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemUserIdAndItemIdOrderByStartDesc(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(bookingList);

        CommentDto result = itemService.addCommentToItem(2L, 1L, commentNewDto);
        result.setCreated(now);

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(commentDtoMustBe);

        Mockito.verify(commentRepository, Mockito.times(1))
                .save(any(Comment.class));
        Mockito.verifyNoMoreInteractions(bookingRepository, commentRepository);
    }
}
