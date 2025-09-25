package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemState {

    private int availableMemory;
    private int totalMemory;
    private int criticalMemoryLimit;
    private boolean cpuEnabled;

    public SystemState(int availableMemory, int totalMemory, boolean cpuEnabled) {
        this.availableMemory = availableMemory;
        this.totalMemory = totalMemory;
        this.criticalMemoryLimit = 0;
        this.cpuEnabled = cpuEnabled;
    }
}
