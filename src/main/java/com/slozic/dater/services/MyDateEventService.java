package com.slozic.dater.services;

import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.models.Date;
import com.slozic.dater.repositories.DateEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyDateEventService {

    private final DateEventRepository dateEventRepository;

    @Transactional(readOnly = true)
    public List<MyDateEventDto> getMyDateEventDtos(UUID currentUser) {
        List<Date> myDateList = dateEventRepository.findAllByCreatedBy(currentUser);
        return myDateList.stream().map(date -> new MyDateEventDto(
                        date.getId().toString(),
                        date.getTitle(),
                        date.getLocation(),
                        date.getDescription(),
                        "",
                        date.getScheduledTime().toString(),
                        "", true)).
                collect(Collectors.toList());
    }
}