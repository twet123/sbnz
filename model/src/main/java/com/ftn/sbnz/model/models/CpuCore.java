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
    private boolean pagingFlag;

    public CpuCore() {
        this.currentProcessId = null;
        this.status = CpuCoreStatus.IDLE;
        this.lastStatusChange = System.currentTimeMillis();
        this.pagingFlag = false;
    }

    public CpuCore(Integer currentProcessId, CpuCoreStatus status, long lastStatusChange) {
        this.currentProcessId = currentProcessId;
        this.status = status;
        this.lastStatusChange = lastStatusChange;
        this.pagingFlag = false;
    }

    public void setStatus(CpuCoreStatus status) {
        this.status = status;
        this.lastStatusChange = System.currentTimeMillis();
    }
}
