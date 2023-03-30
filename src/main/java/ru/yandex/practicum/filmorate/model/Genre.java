package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    private long id;

    @NotBlank(message = "Wrong name")
    private String name;

    public Map<String, Object> toMap() {
        Map<String, Object> meaning = new HashMap<>();
        meaning.put("genre_name", name);
        return meaning;
    }
}