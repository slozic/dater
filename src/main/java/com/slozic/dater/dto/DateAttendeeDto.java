package com.slozic.dater.dto;

import com.slozic.dater.models.DateAttendee;
import lombok.Builder;

@Builder
public record DateAttendeeDto(String id, String username, boolean accepted) {
    public static DateAttendeeDto from (DateAttendee dateAttendee){
        return new DateAttendeeDto(dateAttendee.getAttendeeId().toString(), dateAttendee.getUser().getUsername(), dateAttendee.getAccepted());
    }
}
