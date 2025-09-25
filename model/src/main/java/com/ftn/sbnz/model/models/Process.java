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
    private int safeMemoryLimit;
    private ProcessStatus status;
    private int currentInstruction;
    private long lastStatusChange;
    private List<InstructionType> instructions;

    public Process(int id, int priority, int memoryRequirement, ProcessStatus status, int currentInstruction, List<InstructionType> instructions) {
        this.id = id;
        this.priority = priority;
        this.memoryRequirement = memoryRequirement;
        this.safeMemoryLimit = memoryRequirement;
        this.status = status;
        this.currentInstruction = currentInstruction;
        this.lastStatusChange = System.currentTimeMillis();
        this.instructions = instructions;
    }

    public Process(int id, int priority, int memoryRequirement, ProcessStatus status, int currentInstruction, long lastStatusChange, List<InstructionType> instructions) {
        this.id = id;
        this.priority = priority;
        this.memoryRequirement = memoryRequirement;
        this.safeMemoryLimit = memoryRequirement;
        this.status = status;
        this.currentInstruction = currentInstruction;
        this.lastStatusChange = lastStatusChange;
        this.instructions = instructions;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
        this.lastStatusChange = System.currentTimeMillis();
    }
}
