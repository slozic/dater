package com.slozic.dater.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateAttendeeId implements Serializable {
    private UUID attendeeId;
    private UUID dateId;
}