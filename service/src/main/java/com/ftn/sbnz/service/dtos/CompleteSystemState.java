package com.ftn.sbnz.service.dtos;

import com.ftn.sbnz.model.models.CpuCore;
import com.ftn.sbnz.model.models.SystemState;
import lombok.Data;

import java.util.List;

@Data
public class CompleteSystemState {

    private SystemState systemState;
    private List<CpuCore> cores;
    private List<Process> processes;
}
