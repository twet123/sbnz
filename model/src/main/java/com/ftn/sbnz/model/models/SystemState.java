package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemState {

    private int availableMemory;
    private int totalMemory;
    private boolean cpuEnabled;
}
