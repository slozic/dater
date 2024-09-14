package com.slozic.dater.dto;

import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.models.DateAttendee;
import lombok.Builder;

@Builder
public record DateAttendeeDto(String id, String username, JoinDateStatus status) {
    public static DateAttendeeDto from(DateAttendee dateAttendee) {
        return new DateAttendeeDto(dateAttendee.getAttendeeId().toString(), dateAttendee.getUser().getUsername(), isAccepted(dateAttendee));
    }

    private static JoinDateStatus isAccepted(DateAttendee dateAttendee) {
        return dateAttendee.getAccepted() ? JoinDateStatus.ACCEPTED : JoinDateStatus.ON_WAITLIST;
    }
}
