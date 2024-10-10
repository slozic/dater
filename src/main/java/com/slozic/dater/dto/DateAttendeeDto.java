package com.slozic.dater.dto;

import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.models.DateAttendee;
import lombok.Builder;

@Builder
public record DateAttendeeDto(String id, String username, JoinDateStatus status) {
    public static DateAttendeeDto from(DateAttendee dateAttendee) {
        return new DateAttendeeDto(dateAttendee.getId().getAttendeeId().toString(), dateAttendee.getUser().getUsername(), requestStatus(dateAttendee));
    }

    private static JoinDateStatus requestStatus(DateAttendee dateAttendee) {
        JoinDateStatus joinDateStatus;
        if (dateAttendee.getSoftDeleted()) {
            joinDateStatus = JoinDateStatus.REJECTED;
        } else if (dateAttendee.getAccepted()) {
            joinDateStatus = JoinDateStatus.ACCEPTED;
        } else {
            joinDateStatus = JoinDateStatus.ON_WAITLIST;
        }
        return joinDateStatus;
    }

}
