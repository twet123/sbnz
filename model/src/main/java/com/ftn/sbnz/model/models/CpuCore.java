package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.CpuCoreStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CpuCore {

    private Integer currentProcessId;
    private CpuCoreStatus status;
    private long lastStatusChange;
    private boolean stateEnabled;
}
