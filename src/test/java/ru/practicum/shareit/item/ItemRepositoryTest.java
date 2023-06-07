package ru.practicum.shareit.item;


import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {

    private final TestEntityManager tem;
    private final ItemRepository itemRepository;

    @Test
    void testFindListItemsTest() {

        User user = User.builder().name("user").email("user@example.com").build();
        tem.persist(user);
        Item item1 = Item.builder().name("молоток").description("крепкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item1);
        Item item2 = Item.builder().name("стол").description("деревянный")
                .owner(user).available(true).build();
        tem.persist(item2);
        Item item3 = Item.builder().name("бензопила").description("немецкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item3);

        List<Item> result1 = itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase("инструмент");
        Assertions.assertThat(result1).isNotNull().hasSize(2);
        Assertions.assertThat(result1)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(item1, item3));
    }

    @Test
    void testFindOneItemTest() {

        User user = User.builder().name("user").email("user@example.com").build();
        tem.persist(user);
        Item item1 = Item.builder().name("молоток").description("крепкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item1);
        Item item2 = Item.builder().name("стол").description("деревянный")
                .owner(user).available(true).build();
        tem.persist(item2);
        Item item3 = Item.builder().name("бензопила").description("немецкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item3);

        List<Item> result2 = itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase("стол");
        Assertions.assertThat(result2).isNotNull().hasSize(1);
        Assertions.assertThat(result2)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(item2));
    }

    @Test
    void testNoFoundItemTest() {
        User user = User.builder().name("user").email("user@example.com").build();
        tem.persist(user);
        Item item1 = Item.builder().name("молоток").description("крепкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item1);
        Item item2 = Item.builder().name("стол").description("деревянный")
                .owner(user).available(true).build();
        tem.persist(item2);
        Item item3 = Item.builder().name("бензопила").description("немецкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item3);

        List<Item> result2 = itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase("напильник");
        Assertions.assertThat(result2).isNotNull().hasSize(0);
    }

    @Test
    void testFindEmptyTextTest() {
        User user = User.builder().name("user").email("user@example.com").build();
        tem.persist(user);
        Item item1 = Item.builder().name("молоток").description("крепкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item1);
        Item item2 = Item.builder().name("стол").description("деревянный")
                .owner(user).available(true).build();
        tem.persist(item2);
        Item item3 = Item.builder().name("бензопила").description("немецкий инструмент")
                .owner(user).available(true).build();
        tem.persist(item3);

        List<Item> result2 = itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase("");
        Assertions.assertThat(result2).isNotNull().hasSize(0);
    }
}
