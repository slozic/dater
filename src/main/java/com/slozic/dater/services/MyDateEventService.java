package com.slozic.dater.services;

import com.slozic.dater.dto.response.MyDateEventData;
import com.slozic.dater.dto.response.MyDateEventResponse;
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
    public MyDateEventResponse getMyDateEvents(UUID currentUser) {
        List<Date> myDateList = dateEventRepository.findAllByCreatedBy(currentUser);
        return mapToResponse(myDateList);
    }

    private MyDateEventResponse mapToResponse(List<Date> myDateList) {
        List<MyDateEventData> dateEventData = myDateList.stream().map(mapEntityToDto())
                .collect(Collectors.toList());
        return new MyDateEventResponse(dateEventData);
    }

    private Function<Date, MyDateEventData> mapEntityToDto() {
        return date -> new MyDateEventData(
                date.getId().toString(),
                date.getTitle(),
                date.getLocation(),
                date.getDescription(),
                date.getUser().getUsername(),
                "",
                date.getScheduledTime().toString(),
                true);
    }
}