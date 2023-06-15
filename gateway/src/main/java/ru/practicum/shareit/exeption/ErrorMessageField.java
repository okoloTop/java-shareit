package ru.practicum.shareit.exeption;

import lombok.Getter;

@Getter
public class ErrorMessageField extends ErrorMessage {

    private final String field;

    public ErrorMessageField(String field, String error) {
        super(error);
        this.field = field;
    }
}
