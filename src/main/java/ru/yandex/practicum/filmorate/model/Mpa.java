package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mpa {

    @PositiveOrZero
    private long id;

    @NotBlank(message = "Wrong name")
    private String name;

    public Map<String, Object> toMap() {
        Map<String, Object> meaning = new HashMap<>();
        meaning.put("mpa_name", name);
        return meaning;
    }
}