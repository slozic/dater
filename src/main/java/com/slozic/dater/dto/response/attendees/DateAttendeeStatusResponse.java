package com.slozic.dater.dto.response.attendees;

import com.slozic.dater.dto.enums.JoinDateStatus;

public record DateAttendeeStatusResponse(JoinDateStatus joinDateStatus, String attendeeId, String dateId) {
}
