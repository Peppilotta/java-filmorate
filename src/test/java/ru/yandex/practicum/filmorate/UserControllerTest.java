package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserControllerTest {

    static UserController userController = new UserController();

    @Test
    void validate_StandardBehavior() {
        final User user = User.builder()
                .email("fet@mail.ru")
                .login("fet")
                .birthday(LocalDate.now().minusYears(35))
                .name("Theodor")
                .build();
        userController.validate(user);
    }

    @Test
    void validate_EmptyEmail() {
        User user = new User();
        Exception badName = assertThrows(ValidationException.class,
                () -> userController.validate(user));
        assertEquals("Wrong email",badName.getMessage());
    }

    @Test
    void validate_WrongEmail() {
        User user = new User();
        user.setEmail("email.mail.ru");
        Exception badName = assertThrows(ValidationException.class,
                () -> userController.validate(user));
        assertEquals("Wrong email",badName.getMessage());
    }

    @Test
    void validate_LongDescription() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("");
        Exception badName = assertThrows(ValidationException.class,
                () -> userController.validate(user));
        assertEquals("Wrong login",badName.getMessage());
    }

    @Test
    void validate_BadReleaseDate() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("Fet");
        user.setBirthday(LocalDate.parse("28.12.2025",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        Exception badName = assertThrows(ValidationException.class,
                () -> userController.validate(user));
        assertEquals("Bad birthday",badName.getMessage());
    }

    @Test
    void validate_NegativeDuration() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("Fet");
        user.setBirthday(LocalDate.parse("28.12.1985",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        userController.validate(user);
        assertEquals(user.getLogin(), user.getName());
    }
}