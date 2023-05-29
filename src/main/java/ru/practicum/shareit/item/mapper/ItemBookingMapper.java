package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.model.Item;


public class ItemBookingMapper {
    public static ItemBookingDto itemToDto(Item item) {
        ItemBookingDto itemWithBookingDto = new ItemBookingDto();
        itemWithBookingDto.setId(item.getId());
        itemWithBookingDto.setName(item.getName());
        itemWithBookingDto.setDescription(item.getDescription());
        itemWithBookingDto.setAvailable(item.getAvailable());
        itemWithBookingDto.setOwner(item.getOwner());
        return itemWithBookingDto;
    }
}
