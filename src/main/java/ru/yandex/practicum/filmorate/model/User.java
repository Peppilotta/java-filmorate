package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @PositiveOrZero
    private long id;

    @NotBlank(message = "Email absent")
    @Email(message = "Wrong email")
    private String email;

    @NotBlank(message = "Wrong login")
    private String login;

    private String name;

    @Past(message = "Bad birthday")
    private LocalDate birthday;

    private Set<Friendship> friendships = new HashSet<>();

    public Map<String, Object> toMap() {
        Map<String, Object> meaning = new HashMap<>();
        meaning.put("login", login);
        meaning.put("user_name", name);
        meaning.put("email", email);
        meaning.put("birthday", birthday);
        return meaning;
    }
}