package com.edu.eventms.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Event {
    private Long id;
    private String name;
    private LocalDateTime date;
    private String location;
    private List<Participant> participants;
}
