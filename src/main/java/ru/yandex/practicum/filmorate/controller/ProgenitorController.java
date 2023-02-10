package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Progenitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ProgenitorController<P extends Progenitor> {
    private final Map<Long, P> items = new HashMap<>();
    private long meter = 1L;

    public P create(P item) {
        validate(item);
        item.setId(meter);
        items.put(meter, item);
        meter++;
        return item;
    }

    public P update(P item) {
        validate(item);
        long id = item.getId();
        if (items.containsKey(id)) {
            items.put(id,item);
        }
        return item;
    }

    public List<P> getAll() {
        return new ArrayList<>(items.values());
    }

    protected abstract void validate(P item);
}