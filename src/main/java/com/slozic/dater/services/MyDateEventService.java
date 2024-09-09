package com.slozic.dater.services;

import com.slozic.dater.dto.response.DateEventData;
import com.slozic.dater.dto.response.DateEventResponse;
import com.slozic.dater.models.Date;
import com.slozic.dater.repositories.DateEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyDateEventService {
    private final DateEventRepository dateEventRepository;

    @Transactional(readOnly = true)
    public DateEventResponse getMyDateEventDtos(UUID currentUser) {
        List<Date> myDateList = dateEventRepository.findAllByCreatedBy(currentUser);
        return mapToResponse(myDateList);
    }

    private DateEventResponse mapToResponse(List<Date> myDateList) {
        List<DateEventData> dateEventData = myDateList.stream().map(mapEntityToDto())
                .collect(Collectors.toList());
        return new DateEventResponse(dateEventData);
    }

    private Function<Date, DateEventData> mapEntityToDto() {
        return date -> new DateEventData(
                date.getId().toString(),
                date.getTitle(),
                date.getLocation(),
                date.getDescription(),
                date.getUser().getUsername(),
                "",
                date.getScheduledTime().toString(),
                "");
    }
}