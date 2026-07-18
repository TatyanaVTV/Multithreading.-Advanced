package com.edu.eventms.service;

import com.edu.eventms.model.Event;
import com.edu.eventms.model.Participant;
import com.edu.eventms.model.RegistrationResult;
import java.util.List;

public interface EventService {
    List<RegistrationResult> processEventRegistration(Event event, List<Participant> newParticipants);
}
