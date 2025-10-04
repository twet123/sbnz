package com.ftn.sbnz.service.tests;

import com.ftn.sbnz.model.enums.CpuCoreStatus;
import com.ftn.sbnz.model.enums.InstructionType;
import com.ftn.sbnz.model.enums.ProcessStatus;
import com.ftn.sbnz.model.events.CpuTemperatureEvent;
import com.ftn.sbnz.model.events.IOEvent;
import com.ftn.sbnz.model.events.PageFaultEvent;
import com.ftn.sbnz.model.models.CpuCore;
import com.ftn.sbnz.model.models.Process;
import com.ftn.sbnz.model.models.SystemState;
import com.ftn.sbnz.utils.DroolsUtil;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionPseudoClock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RuleTests {

    @Test
    public void testProcessBecomesRunningWhenSufficientResources() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(8192, 8192, true);
        Process process = new Process(1, 5, 1024, ProcessStatus.NEW, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(process);
        kieSession.insert(core);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, process.getStatus()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertNotEquals(0, process.getLastStatusChange()),
                () -> assertEquals(14, firedRules),
                () -> assertEquals(CpuCoreStatus.IDLE, core.getStatus())
        );

        kieSession.dispose();
    }

    @Test
    public void testProcessDoesNotBecomeRunningWhenInsufficientResources() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(1000, 1000, true);
        Process process = new Process(1, 5, 1024, ProcessStatus.NEW, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(process);
        kieSession.insert(core);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.NEW, process.getStatus()),
                () -> assertEquals(1000, systemState.getAvailableMemory()),
                () -> assertEquals(0, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testMultipleProcessesPriorityRecognition() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(8192, 8192, true);
        Process processHighPriority = new Process(1, 5, 1024, ProcessStatus.NEW, 0, Collections.nCopies(5, InstructionType.REGULAR));
        Process processLowPriority = new Process(2, 1, 1024, ProcessStatus.NEW, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(processHighPriority);
        kieSession.insert(processLowPriority);
        kieSession.insert(core);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, processLowPriority.getStatus()),
                () -> assertEquals(ProcessStatus.EXIT, processHighPriority.getStatus()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertNotEquals(0, processHighPriority.getLastStatusChange()),
                () -> assertNotEquals(0, processLowPriority.getLastStatusChange()),
                () -> assertTrue(processHighPriority.getLastStatusChange() < processLowPriority.getLastStatusChange()),
                () -> assertEquals(22, firedRules),
                () -> assertEquals(CpuCoreStatus.IDLE, core.getStatus())
        );

        kieSession.dispose();
    }

    @Test
    public void testMultipleCores() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(8192, 8192, true);
        Process processHighPriority = new Process(1, 5, 1024, ProcessStatus.NEW, 0, Collections.nCopies(5, InstructionType.REGULAR));
        Process processLowPriority = new Process(2, 1, 1024, ProcessStatus.NEW, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(null, CpuCoreStatus.IDLE, 0);
        CpuCore core2 = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(processHighPriority);
        kieSession.insert(processLowPriority);
        kieSession.insert(core1);
        kieSession.insert(core2);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, processLowPriority.getStatus()),
                () -> assertEquals(ProcessStatus.EXIT, processHighPriority.getStatus()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertNotEquals(0, processHighPriority.getLastStatusChange()),
                () -> assertNotEquals(0, processLowPriority.getLastStatusChange()),
                // we wont know in which order the processes will execute since they are being executed "in parallel"
                () -> assertEquals(22, firedRules),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertEquals(CpuCoreStatus.IDLE, core2.getStatus())
        );

        kieSession.dispose();
    }

    @Test
    public void testCpuTemperatureEvents() {
        KieSession kieSession = DroolsUtil.getSession();
        SessionPseudoClock clock = kieSession.getSessionClock();

        SystemState systemState = new SystemState(7168, 8192, true);
        Process processHighPriority = new Process(1, 5, 1024, ProcessStatus.RUNNING, 0, Collections.nCopies(5, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(1, CpuCoreStatus.BUSY, 0);

        kieSession.insert(systemState);
        kieSession.insert(processHighPriority);
        kieSession.insert(core1);
        for(int i = 0; i < 5; ++i) {
            CpuTemperatureEvent tempEvent = new CpuTemperatureEvent(105);
            kieSession.insert(tempEvent);
            clock.advanceTime(1, TimeUnit.SECONDS);
        }

        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.READY, processHighPriority.getStatus()),
                () -> assertEquals(7168, systemState.getAvailableMemory()),
                () -> assertFalse(systemState.isCpuEnabled()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                () -> assertEquals(2, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testCpuTemperatureEventsNotTriggered() {
        KieSession kieSession = DroolsUtil.getSession();
        SessionPseudoClock clock = kieSession.getSessionClock();

        SystemState systemState = new SystemState(7168, 8192, true);
        Process processHighPriority = new Process(1, 5, 1024, ProcessStatus.RUNNING, 0, Collections.nCopies(5, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(1, CpuCoreStatus.BUSY, 0);

        kieSession.insert(systemState);
        kieSession.insert(processHighPriority);
        kieSession.insert(core1);
        for(int i = 0; i < 5; ++i) {
            CpuTemperatureEvent tempEvent = new CpuTemperatureEvent(105);
            kieSession.insert(tempEvent);
            clock.advanceTime(10, TimeUnit.SECONDS);
        }

        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, processHighPriority.getStatus()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertTrue(systemState.isCpuEnabled()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                () -> assertEquals(7, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testCpuCooledDownTriggered() {
        KieSession kieSession = DroolsUtil.getSession();
        SessionPseudoClock clock = kieSession.getSessionClock();

        SystemState systemState = new SystemState(7168, 8192, false);
        Process processHighPriority = new Process(1, 5, 1024, ProcessStatus.READY, 1, Collections.nCopies(5, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(processHighPriority);
        kieSession.insert(core1);
        for(int i = 0; i < 5; ++i) {
            CpuTemperatureEvent tempEvent = new CpuTemperatureEvent(49);
            kieSession.insert(tempEvent);
            clock.advanceTime(1, TimeUnit.SECONDS);
        }

        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, processHighPriority.getStatus()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertTrue(systemState.isCpuEnabled()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                () -> assertEquals(8, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testPriorityBoostingTemplate() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(8192, 8192, false);
        Process process = new Process(1, 5, 1024, ProcessStatus.READY, 0, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(process);
        kieSession.insert(core);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertTrue(process.getPriority() > 5),
                () -> assertEquals(1, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testPreemption() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(6144, 8192, true);
        Process processLowPriority = new Process(1, 3, 1024, ProcessStatus.RUNNING, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(1, CpuCoreStatus.BUSY, 0);
        Process processHighPriority = new Process(2, 8, 1024, ProcessStatus.READY, 0, Collections.nCopies(10, InstructionType.REGULAR));

        kieSession.insert(systemState);
        kieSession.insert(processLowPriority);
        kieSession.insert(core1);
        kieSession.insert(processHighPriority);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, processLowPriority.getStatus()),
                () -> assertEquals(ProcessStatus.EXIT, processHighPriority.getStatus()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                () -> assertTrue(processLowPriority.getLastStatusChange() > processHighPriority.getLastStatusChange()),
                () -> assertEquals(26, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testIOBlocking() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(7168, 8192, true);
        Process processLowPriority = new Process(1, 3, 1024, ProcessStatus.RUNNING, 0, List.of(InstructionType.REGULAR, InstructionType.IO, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(1, CpuCoreStatus.BUSY, 0);

        kieSession.insert(systemState);
        kieSession.insert(processLowPriority);
        kieSession.insert(core1);
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.BLOCKED, processLowPriority.getStatus()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                () -> assertEquals(2, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testIOUnblocking() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(7168, 8192, true);
        Process processLowPriority = new Process(1, 3, 1024, ProcessStatus.BLOCKED, 1, List.of(InstructionType.REGULAR, InstructionType.IO, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(null, CpuCoreStatus.IDLE, 0);

        kieSession.insert(systemState);
        kieSession.insert(processLowPriority);
        kieSession.insert(core1);
        kieSession.insert(new IOEvent(1));
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, processLowPriority.getStatus()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertEquals(5, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testPagingOnPageFaultEvent() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(7168, 8192, true);
        Process process = new Process(1, 5, 1024, ProcessStatus.RUNNING, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core = new CpuCore(1, CpuCoreStatus.BUSY, 0);

        kieSession.insert(systemState);
        kieSession.insert(process);
        kieSession.insert(core);
        kieSession.insert(new PageFaultEvent(1));
        int firedRules = kieSession.fireAllRules();

        System.out.println(firedRules);

        assertAll(
                () -> assertEquals(ProcessStatus.EXIT, process.getStatus()),
                () -> assertEquals(CpuCoreStatus.IDLE, core.getStatus()),
                () -> assertNull(core.getCurrentProcessId()),
                () -> assertEquals(8192, systemState.getAvailableMemory()),
                () -> assertTrue(firedRules > 12) // 12 to execute the process normally, but since paging happened it will require more
        );

        kieSession.dispose();
    }

    @Test
    public void testCoreReturnsToBusyAfterPagingTimeout() {
        KieSession kieSession = DroolsUtil.getSession();

        SystemState systemState = new SystemState(7168, 8192, true);
        // set lastStatusChange to current time for accurate testing
        CpuCore core = new CpuCore(1, CpuCoreStatus.PAGING, System.currentTimeMillis());

        kieSession.insert(systemState);
        kieSession.insert(core);

        try {
            Thread.sleep(2001);
        } catch (InterruptedException ignored) {}

        kieSession.fireAllRules();

        assertEquals(CpuCoreStatus.BUSY, core.getStatus());

        kieSession.dispose();
    }

    @Test
    public void testCpuThrashingAndProcessSuspension() {
        KieSession kieSession = DroolsUtil.getSession();
        SessionPseudoClock clock = kieSession.getSessionClock();

        // set availableMemory < criticalMemoryLimit
        SystemState systemState = new SystemState(1000, 8192, 2048, true);
        Process processLowPriority = new Process(1, 3, 1024, ProcessStatus.RUNNING, 0, Collections.nCopies(10, InstructionType.REGULAR));
        CpuCore core1 = new CpuCore(1, CpuCoreStatus.BUSY, 0);

        kieSession.insert(systemState);
        kieSession.insert(processLowPriority);
        kieSession.insert(core1);

        for(int i = 0; i < 5; ++i) {
            kieSession.insert(new PageFaultEvent(1));
            clock.advanceTime(1, TimeUnit.SECONDS);
        }
        int firedRules = kieSession.fireAllRules();

        assertAll(
                () -> assertEquals(ProcessStatus.SUSPENDED, processLowPriority.getStatus()),
                () -> assertEquals(CpuCoreStatus.IDLE, core1.getStatus()),
                () -> assertNull(core1.getCurrentProcessId()),
                // 1 for the first paging fault, 1 for thrashing detection, 1 for suspension
                () -> assertEquals(3, firedRules)
        );

        kieSession.dispose();
    }

    @Test
    public void testResumeSuspendedProcessWhenMemoryIsSufficient() {
        KieSession kieSession = DroolsUtil.getSession();

        // availableMemory > process safeMemoryLimit
        SystemState systemState = new SystemState(4096, 8192, true);
        Process process = new Process(1, 5, 1024, ProcessStatus.SUSPENDED, 0, Collections.nCopies(10, InstructionType.REGULAR));
        process.setSafeMemoryLimit(2048); // Set safe memory limit for the process

        kieSession.insert(systemState);
        kieSession.insert(process);
        kieSession.fireAllRules();

        assertEquals(ProcessStatus.READY, process.getStatus());

        kieSession.dispose();
    }
}
