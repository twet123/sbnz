package com.ftn.sbnz.service.tests;

import com.ftn.sbnz.listener.TriggeredRulesListener;
import com.ftn.sbnz.listener.WorkingMemoryListener;
import com.ftn.sbnz.model.enums.CpuCoreStatus;
import com.ftn.sbnz.model.enums.InstructionType;
import com.ftn.sbnz.model.enums.ProcessStatus;
import com.ftn.sbnz.model.events.CpuTemperatureEvent;
import com.ftn.sbnz.model.events.IOEvent;
import com.ftn.sbnz.model.models.CpuCore;
import com.ftn.sbnz.model.models.Process;
import com.ftn.sbnz.model.models.SystemState;
import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.time.SessionPseudoClock;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        KieSession kieSession = getSession();
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
        KieSession kieSession = getSession();
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
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

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
        KieSession kieSession = getSession();

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

    private KieSession getSession() {
        KieHelper kieHelper = new KieHelper();

        // adding template ruleset
        InputStream priorityBoostingTemplate = ForwardChainTests.class.getResourceAsStream("/rules/template/priority-boosting.drt");
        InputStream data = ForwardChainTests.class.getResourceAsStream("/rules/template/priority-boosting.xls");
        ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();
        String priorityBoostingRules = converter.compile(data, priorityBoostingTemplate, 2, 2);
        kieHelper.addContent(priorityBoostingRules, ResourceType.DRL);

        // adding regular ruleset
        InputStream regularRules = ForwardChainTests.class.getResourceAsStream("/rules/forward/forward.drl");
        Resource regularRulesRes = ResourceFactory.newInputStreamResource(regularRules);
        kieHelper.addResource(regularRulesRes, ResourceType.DRL);

        // CEP configuration
        KieBaseConfiguration kBaseConfig = KieServices.Factory.get().newKieBaseConfiguration();
        kBaseConfig.setOption(EventProcessingOption.STREAM);

        KieSessionConfiguration kSessionConfig = KieServices.Factory.get().newKieSessionConfiguration();
        kSessionConfig.setOption(ClockTypeOption.get("pseudo"));

        KieBase kBase = kieHelper.build(kBaseConfig);
        KieSession kieSession = kBase.newKieSession(kSessionConfig, null);
        kieSession.addEventListener(new TriggeredRulesListener());
        kieSession.addEventListener(new WorkingMemoryListener());
        return kieSession;
    }
}
