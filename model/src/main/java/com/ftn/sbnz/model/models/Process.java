package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.InstructionType;
import com.ftn.sbnz.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Process {

    private int id;
    private int priority;
    private int memoryRequirement;
    private ProcessStatus status;
    private int currentInstruction;
    private long lastStatusChange;
    private List<InstructionType> instructions;

    public Process(int id, int priority, int memoryRequirement, ProcessStatus status, int currentInstruction, List<InstructionType> instructions) {
        this.id = id;
        this.priority = priority;
        this.memoryRequirement = memoryRequirement;
        this.status = status;
        this.currentInstruction = currentInstruction;
        this.instructions = instructions;
        this.lastStatusChange = System.currentTimeMillis();
    }
}
