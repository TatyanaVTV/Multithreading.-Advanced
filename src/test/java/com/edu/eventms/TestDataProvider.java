package com.edu.eventms;

import com.edu.eventms.model.Participant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class TestDataProvider {

    public static Participant createParticipant(Long id, String name, String email) {
        return Participant.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    public static List<Participant> createParticipants(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> Participant.builder()
                        .id((long) i)
                        .name(format("User%d", i))
                        .email(format("user%d@ex.com", i))
                        .build())
                .collect(Collectors.toList());
    }
}
