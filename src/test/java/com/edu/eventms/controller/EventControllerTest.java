package com.edu.eventms.controller;

import com.edu.eventms.model.Participant;
import com.edu.eventms.model.RegistrationResult;
import com.edu.eventms.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.edu.eventms.TestDataProvider.createParticipant;
import static com.edu.eventms.TestDataProvider.createParticipants;
import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    void registerParticipants_shouldReturn200AndResults() throws Exception {
        var eventId = 101L;
        var participants = List.of(
                createParticipant(1L, "Ivan", "ivan@ex.com"),
                createParticipant(2L, "Maria", "maria@ex.com")
        );

        var expectedResults = List.of(
                new RegistrationResult(1L, true,
                        true, true, "Участник Ivan обработан"),
                new RegistrationResult(2L, true,
                        true, true, "Участник Maria обработан")
        );

        when(eventService.processEventRegistration(any(), eq(participants)))
                .thenReturn(expectedResults);

        mockMvc.perform(post("/api/events/{eventId}/register", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participants)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].participantId").value(1))
                .andExpect(jsonPath("$[0].registered").value(true))
                .andExpect(jsonPath("$[0].invitationSent").value(true))
                .andExpect(jsonPath("$[0].notificationSent").value(true))
                .andExpect(jsonPath("$[0].message").value("Участник Ivan обработан"))
                .andExpect(jsonPath("$[1].participantId").value(2))
                .andExpect(jsonPath("$[1].registered").value(true));
    }

    @Test
    void registerParticipants_shouldReturn400ForInvalidRequestBody() throws Exception {
        var eventId = 101L;
        var invalidJson = "{\"invalid\":\"data\"}";

        mockMvc.perform(post("/api/events/{eventId}/register", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerParticipants_shouldReturn200WithEmptyResults() throws Exception {
        var eventId = 101L;
        var participants = List.<Participant>of();

        when(eventService.processEventRegistration(any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/events/{eventId}/register", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participants)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void registerParticipants_shouldHandleSingleParticipant() throws Exception {
        var eventId = 101L;
        var participants = List.of(
                createParticipant(1L, "Ivan", "ivan@ex.com")
        );

        var expectedResults = List.of(
                new RegistrationResult(1L, true,
                        true, true, "Участник Ivan обработан")
        );

        when(eventService.processEventRegistration(any(), eq(participants)))
                .thenReturn(expectedResults);

        mockMvc.perform(post("/api/events/{eventId}/register", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participants)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].participantId").value(1))
                .andExpect(jsonPath("$[0].registered").value(true));
    }

    @Test
    void registerParticipants_shouldHandleLargePayload() throws Exception {
        var eventId = 101L;
        var participants = createParticipants(20);
        var expectedResults = participants.stream()
                .map(participant -> RegistrationResult.builder()
                        .participantId(participant.getId())
                        .registered(true)
                        .invitationSent(true)
                        .notificationSent(true)
                        .message(format("Участник %s обработан", participant.getName()))
                        .build()
                )
                .toList();

        when(eventService.processEventRegistration(any(), any()))
                .thenReturn(expectedResults);

        mockMvc.perform(post("/api/events/{eventId}/register", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participants)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20));
    }
}
