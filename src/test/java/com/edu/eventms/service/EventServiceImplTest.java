package com.edu.eventms.service;

import com.edu.eventms.model.Event;
import com.edu.eventms.model.Participant;
import com.edu.eventms.model.RegistrationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.edu.eventms.TestDataProvider.createParticipants;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class EventServiceImplTest {

    @Mock
    private ExecutorService executorService;

    @Mock
    private Future<RegistrationResult> future;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(executorService);
    }

    @Test
    void processEventRegistration_shouldProcessAllParticipantsSuccessfully() throws Exception {
        var event = new Event();
        event.setId(1L);
        event.setName("Test Event");

        var participants = List.of(
                Participant.builder().id(1L).name("Ivan").email("ivan@ex.com").build(),
                Participant.builder().id(2L).name("Maria").email("maria@ex.com").build()
        );

        var expectedResults = List.of(
                new RegistrationResult(1L, true, true,
                        true, "Участник Ivan обработан"),
                new RegistrationResult(2L, true, true,
                        true, "Участник Maria обработан")
        );

        when(executorService.invokeAll(anyList(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> List.of(future, future));

        when(future.isDone()).thenReturn(true);
        when(future.isCancelled()).thenReturn(false);
        when(future.get())
                .thenReturn(expectedResults.get(0))
                .thenReturn(expectedResults.get(1));

        var results = eventService.processEventRegistration(event, participants);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(RegistrationResult::isRegistered);
        assertThat(results).allMatch(RegistrationResult::isInvitationSent);
        assertThat(results).allMatch(RegistrationResult::isNotificationSent);
        assertThat(results).extracting(RegistrationResult::getParticipantId)
                .containsExactly(1L, 2L);
    }

    @Test
    void processEventRegistration_shouldHandleEmptyList() {
        var event = new Event();
        event.setId(1L);

        var participants = List.<Participant>of();

        var results = eventService.processEventRegistration(event, participants);

        assertThat(results).isEmpty();
    }

    @Test
    void processEventRegistration_shouldReturnTimedOutResults() throws Exception {
        var event = new Event();
        event.setId(1L);

        var participants = List.of(
                Participant.builder().id(1L).name("Ivan").email("ivan@ex.com").build(),
                Participant.builder().id(2L).name("Maria").email("maria@ex.com").build()
        );

        var future1 = org.mockito.Mockito.mock(Future.class);
        var future2 = org.mockito.Mockito.mock(Future.class);

        when(executorService.invokeAll(anyList(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> List.of(future1, future2));

        when(future1.isDone()).thenReturn(false);
        when(future1.isCancelled()).thenReturn(true);
        when(future2.isDone()).thenReturn(false);
        when(future2.isCancelled()).thenReturn(true);

        var results = eventService.processEventRegistration(event, participants);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> !r.isRegistered());
        assertThat(results).allMatch(r -> !r.isInvitationSent());
        assertThat(results).allMatch(r -> !r.isNotificationSent());
        assertThat(results)
                .extracting(RegistrationResult::getMessage)
                .allMatch(msg -> msg != null && msg.contains("Таймаут"));
    }

    @Test
    void processEventRegistration_shouldHandlePartialTimeout() throws Exception {
        var event = new Event();
        event.setId(1L);

        var participants = List.of(
                Participant.builder().id(1L).name("Fast").email("fast@ex.com").build(),
                Participant.builder().id(2L).name("Slow").email("slow@ex.com").build()
        );

        var futureSuccess = org.mockito.Mockito.mock(Future.class);
        var futureTimeout = org.mockito.Mockito.mock(Future.class);

        when(executorService.invokeAll(anyList(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> List.of(futureSuccess, futureTimeout));

        when(futureSuccess.isDone()).thenReturn(true);
        when(futureSuccess.isCancelled()).thenReturn(false);
        when(futureSuccess.get()).thenReturn(
                new RegistrationResult(1L, true, true, true, "Участник Fast обработан")
        );

        when(futureTimeout.isDone()).thenReturn(false);
        when(futureTimeout.isCancelled()).thenReturn(true);

        var results = eventService.processEventRegistration(event, participants);

        assertThat(results).hasSize(2);

        var firstResult = results.get(0);
        assertThat(firstResult.getParticipantId()).isEqualTo(1L);
        assertThat(firstResult.isRegistered()).isTrue();
        assertThat(firstResult.isInvitationSent()).isTrue();
        assertThat(firstResult.isNotificationSent()).isTrue();
        assertThat(firstResult.getMessage()).isEqualTo("Участник Fast обработан");

        var secondResult = results.get(1);
        assertThat(secondResult.getParticipantId()).isNull();
        assertThat(secondResult.isRegistered()).isFalse();
        assertThat(secondResult.isInvitationSent()).isFalse();
        assertThat(secondResult.isNotificationSent()).isFalse();
        assertThat(secondResult.getMessage()).contains("Таймаут");
    }

    @Test
    void processEventRegistration_shouldHandleException() throws Exception {
        var event = new Event();
        event.setId(1L);

        var participants = List.of(
                Participant.builder().id(1L).name("Test").email("test@ex.com").build()
        );

        when(executorService.invokeAll(anyList(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("Test exception"));

        var results = eventService.processEventRegistration(event, participants);

        assertThat(results).isEmpty();
    }

    @Test
    void processEventRegistration_shouldHandleLargeNumberOfParticipants() throws Exception {
        var event = new Event();
        event.setId(1L);

        var participants = createParticipants(20);

        var futures = java.util.stream.IntStream.range(0, 20)
                .mapToObj(i -> {
                    var f = org.mockito.Mockito.mock(Future.class);
                    try {
                        when(f.isDone()).thenReturn(true);
                        when(f.isCancelled()).thenReturn(false);
                        when(f.get()).thenReturn(
                                new RegistrationResult(
                                        (long) i,
                                        true,
                                        true,
                                        true,
                                        format("Участник User%d обработан", i)
                                )
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return f;
                })
                .toList();

        when(executorService.invokeAll(anyList(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> futures);

        long start = System.currentTimeMillis();
        var results = eventService.processEventRegistration(event, participants);
        long duration = System.currentTimeMillis() - start;

        assertThat(results).hasSize(20);
        assertThat(results).allMatch(RegistrationResult::isRegistered);
        assertThat(duration).isLessThan(100L);
    }
}
