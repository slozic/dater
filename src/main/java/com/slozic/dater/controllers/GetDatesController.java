package com.slozic.dater.controllers;

import com.slozic.dater.dto.CreateDateEventRequest;
import com.slozic.dater.dto.DateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dates")
@Slf4j
public class GetDatesController {

    private List<DateEvent> dateEvents = new ArrayList<>();

    @GetMapping
    public List<DateEvent> getAllDates() {
        return List.of(new DateEvent(UUID.randomUUID().toString(), "slozic", "johanna", "Caffe @ Brandenburg", ""));
    }

    @GetMapping("/{id}")
    public DateEvent getDateById(@PathVariable("id") final String id) {
        log.info("Request for date by id:", id);
        return new DateEvent(id, "slozic", "johanna", "Caffe @ Brandenburg", "");
    }

    @PostMapping
    public DateEvent createDateEvent(@RequestBody final CreateDateEventRequest request) {
        final DateEvent dateEvent =
                new DateEvent(UUID.randomUUID().toString(), request.dateOwner(), "", request.location(), request.description());
        dateEvents.add(dateEvent);
        return dateEvent;
    }

}
