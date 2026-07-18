package com.edu.eventms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RegistrationResult {
    private Long participantId;
    private boolean registered;
    private boolean invitationSent;
    private boolean notificationSent;
    private String message;
}
