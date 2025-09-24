package com.ftn.sbnz.service.tests;

import com.ftn.sbnz.model.enums.CpuCoreStatus;
import com.ftn.sbnz.model.enums.InstructionType;
import com.ftn.sbnz.model.enums.ProcessStatus;
import com.ftn.sbnz.model.events.CpuTemperatureEvent;
import com.ftn.sbnz.model.models.CpuCore;
import com.ftn.sbnz.model.models.Process;
import com.ftn.sbnz.model.models.SystemState;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ForwardChainTests {

    private static final String sessionName = "forwardSession";

    @Test
    public void testProcessBecomesRunningWhenSufficientResources() {
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

        Thread cpuTempThread = new Thread(() -> {
            for(int i = 0; i < 15; ++i) {
                CpuTemperatureEvent tempEvent = new CpuTemperatureEvent(105);
                kieSession.insert(tempEvent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        cpuTempThread.setDaemon(true);
        cpuTempThread.start();

        kieSession.fireUntilHalt();

//        assertAll(
//                () -> assertEquals(1, firedRules)
//        );

        kieSession.dispose();
    }

    private KieSession getSession() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        return kieContainer.newKieSession(sessionName);
    }
}
