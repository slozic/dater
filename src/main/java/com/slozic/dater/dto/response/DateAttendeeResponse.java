package com.slozic.dater.dto.response;

import com.slozic.dater.dto.DateAttendeeDto;

import java.util.List;

public record DateAttendeeResponse(String dateId, List<DateAttendeeDto> dateAttendees) {
}
