package com.ftn.sbnz.service.dtos;

import com.ftn.sbnz.model.models.CpuCore;
import com.ftn.sbnz.model.models.Process;
import com.ftn.sbnz.model.models.SystemState;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SystemStateDto {

    private SystemDto system;
    private List<ProcessDto> processes;

    public SystemState getSystemStateModel() {
        return new SystemState(system.getTotalMemory());
    }

    public List<CpuCore> getCpuCoreModels() {
        return Collections.nCopies(system.getCpuCores(), new CpuCore());
    }

    public List<Process> getProcessModels() {
        return processes.stream().map(p -> new Process(
                Integer.parseInt(p.getId()),
                p.getPriority(),
                p.getMemoryRequirement(),
                p.getSafeMemoryLimit(),
                p.getInstructionTypes()
        )).collect(Collectors.toList());
    }
}
