package com.edu.eventms.service;

import com.edu.eventms.model.Event;
import com.edu.eventms.model.Participant;
import com.edu.eventms.model.RegistrationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final ExecutorService executorService;

    @Override
    public List<RegistrationResult> processEventRegistration(Event event, List<Participant> newParticipants) {
        var results = new ArrayList<RegistrationResult>(newParticipants.size());

        // Используем invokeAll для параллельной обработки каждого участника
        try {
            var tasks = newParticipants.stream()
                    .map(participant -> (java.util.concurrent.Callable<RegistrationResult>) () -> {
                        // Имитация регистрации
                        var registered = registerParticipant(event, participant);
                        // Имитация отправки приглашения
                        var invitationSent = sendInvitation(participant);
                        // Имитация отправки уведомления
                        var notificationSent = sendNotification(participant);

                        var message = format("Участник %s обработан", participant.getName());
                        return RegistrationResult.builder()
                                .participantId(participant.getId())
                                .registered(registered)
                                .invitationSent(invitationSent)
                                .notificationSent(notificationSent)
                                .message(message)
                                .build();
                    })
                    .toList();

            // Запускаем все задачи параллельно и ждём завершения
            var futures = executorService.invokeAll(tasks, 30, SECONDS);

            for (var future : futures) {
                if (future.isDone() && !future.isCancelled()) {
                    results.add(future.get());
                } else {
                    // Если не успело, добавляем результат с ошибкой
                    results.add(RegistrationResult.builder()
                            .participantId(null)
                            .registered(false)
                            .invitationSent(false)
                            .notificationSent(false)
                            .message("Таймаут обработки")
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при параллельной обработке участников", e);
        }

        log.info("Обработка завершена. Успешно: {}", results.stream().filter(RegistrationResult::isRegistered).count());
        return results;
    }

    // Вспомогательные методы с имитацией задержек
    private boolean registerParticipant(Event event, Participant participant) {
        try {
            // Имитация длительности регистрации (0.5–1.5 сек)
            Thread.sleep((long) (500 + Math.random() * 1000));
            log.info("Зарегистрирован участник {} на мероприятие {}", participant.getName(), event.getName());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean sendInvitation(Participant participant) {
        try {
            Thread.sleep((long) (300 + Math.random() * 700));
            log.info("Приглашение отправлено на email {}", participant.getEmail());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean sendNotification(Participant participant) {
        try {
            Thread.sleep((long) (200 + Math.random() * 500));
            log.info("Уведомление отправлено участнику {}", participant.getName());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
