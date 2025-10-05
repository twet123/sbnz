package com.ftn.sbnz.service.dtos;

import lombok.Data;

import java.util.List;

@Data
public class EventListDto {

    private int rulesFired;
    private List<EventDto> events;
}
