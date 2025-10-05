package com.ftn.sbnz.service.dtos;

public enum EventType {
    PROCESS_READY,
    PROCESS_BLOCKED,
    PROCESS_SCHEDULED,
    PAGING,
    PREEMPTED,
    IO_RECEIVED,
    PROCESS_FINISHED,
    END,
    PROCESS_UNBLOCKED
}
