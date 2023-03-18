package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmGenre {

    @Positive
    long filmId;

    @Positive
    long genreId;

    public Map<String, Object> toMap() {
        Map<String, Object> meaning = new HashMap<>();
        meaning.put("film_id", filmId);
        meaning.put("genre_id", genreId);
        return meaning;
    }
}