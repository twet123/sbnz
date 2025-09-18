package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Process {

    private int id;
    private int priority;
    private int memoryRequirement;
    private ProcessStatus status;
    private int currentInstruction;
    private int totalInstructions;
    private long lastStatusChange;
    private int safeMemoryLimit;
}
