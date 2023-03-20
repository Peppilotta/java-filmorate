package ru.yandex.practicum.filmorate.sevice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Set;

@Service
@Slf4j
public class MpaService {

    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Set<Mpa> getMpas() {
        log.info("GET request - all MPA");
        return mpaStorage.getMpas();
    }

    public Mpa getMpa(int id) {
        log.info("GET request - MPA with id={}", id);
        mpaStorage.containsMpa(id);
        return mpaStorage.getMpa(id);
    }
}