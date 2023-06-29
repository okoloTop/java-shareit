package ru.practicum.shareit.exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handlerValidateException(final IllegalArgumentException e) {
        log.warn(e.getMessage());
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ErrorMessageField> handlerValidateException(final MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        List<ErrorMessageField> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(
                fieldError -> {
                    errors.add(new ErrorMessageField(fieldError.getField(), fieldError.getDefaultMessage()));
                });
        return errors;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handlerException(final RuntimeException e) {
        String msg = "Произошла непредвиденная ошибка.";
        log.warn(msg);
        return new ErrorMessage(msg);
    }
}
