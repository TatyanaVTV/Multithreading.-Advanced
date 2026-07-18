package com.edu.eventms.controller;

import com.edu.eventms.model.Event;
import com.edu.eventms.model.Participant;
import com.edu.eventms.model.RegistrationResult;
import com.edu.eventms.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;

    @PostMapping("/{eventId}/register")
    public List<RegistrationResult> registerParticipants(
            @PathVariable Long eventId,
            @RequestBody List<Participant> newParticipants) {

        // Для демонстрации создаём объект мероприятия (в реальности получили бы из БД)
        var event = new Event();
        event.setId(eventId);
        event.setName("Тестовое мероприятие #" + eventId);

        log.info("Получен запрос на регистрацию {} участников на мероприятие {}", newParticipants.size(), eventId);

        // Запускаем многопоточную обработку
        var results = eventService.processEventRegistration(event, newParticipants);

        log.info("Ответ отправлен с результатами для {} участников", results.size());
        return results;
    }
}
