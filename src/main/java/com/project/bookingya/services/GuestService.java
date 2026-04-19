package com.project.bookingya.services;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.exceptions.EntityExistsException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Guest;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.shared.Constants;

@Service
public class GuestService {

    private final IGuestRepository guestRepository;
    private final ModelMapper mapper;

    public GuestService(IGuestRepository guestRepository, ModelMapper mapper) {
        this.guestRepository = guestRepository;
        this.mapper = mapper;
    }

    public Guest create(GuestDto guestDto) {
        validateUniqueGuest(guestDto, null);

        GuestEntity entity = mapper.map(guestDto, GuestEntity.class);
        GuestEntity saved = guestRepository.save(entity);
        return mapper.map(saved, Guest.class);
    }

    public Guest update(GuestDto guestDto, UUID id) {
        GuestEntity existing = guestRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.GUEST_NOT_FOUND));

        validateUniqueGuest(guestDto, existing);
        mapper.map(guestDto, existing);

        GuestEntity updated = guestRepository.save(existing);
        return mapper.map(updated, Guest.class);
    }

    public List<Guest> getAll() {
        List<GuestEntity> guests = guestRepository.findAll();
        Type listType = new TypeToken<List<Guest>>() {}.getType();
        return mapper.map(guests, listType);
    }

    public Guest getById(UUID id) {
        GuestEntity guest = guestRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.GUEST_NOT_FOUND));
        return mapper.map(guest, Guest.class);
    }

    public Guest getByIdentification(String identification) {
        GuestEntity guest = guestRepository.findByIdentification(identification)
            .orElseThrow(() -> new EntityNotExistsException(Constants.GUEST_NOT_FOUND));
        return mapper.map(guest, Guest.class);
    }

    public void delete(UUID id) {
        GuestEntity guest = guestRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.GUEST_NOT_FOUND));
        guestRepository.delete(guest);
    }

    private void validateUniqueGuest(GuestDto guestDto, GuestEntity existing) {
        boolean sameIdentification = existing != null && existing.getIdentification().equals(guestDto.getIdentification());
        boolean sameEmail = existing != null && existing.getEmail().equals(guestDto.getEmail());

        if (!sameIdentification && guestRepository.existsByIdentification(guestDto.getIdentification())) {
            throw new EntityExistsException(Constants.GUEST_EXISTS);
        }

        if (!sameEmail && guestRepository.existsByEmail(guestDto.getEmail())) {
            throw new EntityExistsException(Constants.GUEST_EMAIL_EXISTS);
        }
    }
}
