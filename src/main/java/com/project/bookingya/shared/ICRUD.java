package com.project.bookingya.shared;

import java.util.List;
import java.util.UUID;

public interface ICRUD<I, O> {
    List<O> getAll();
    O getById(UUID id);
    O create(I obj);
    O update(I obj, UUID id);
    void delete(UUID id);
}
