package com.ftn.sbnz.service;

import com.ftn.sbnz.model.enums.InstructionType;
import com.ftn.sbnz.model.enums.ProcessStatus;
import com.ftn.sbnz.model.models.Process;
import com.ftn.sbnz.model.models.SystemState;
import com.ftn.sbnz.service.dtos.CompleteSystemState;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class SampleAppService {

    private static final Logger log = LoggerFactory.getLogger(SampleAppService.class);

    private final KieContainer kieContainer;

    @Autowired
    public SampleAppService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public void test() {
        SystemState systemState = new SystemState(8192, 8192, false);
        Process testProcess = new Process(1, 5, 1024, ProcessStatus.NEW, 0, Collections.nCopies(10, InstructionType.REGULAR));

        KieSession kieSession = kieContainer.newKieSession("forwardSession");
        kieSession.insert(systemState);
        kieSession.insert(testProcess);
        kieSession.fireAllRules();
        kieSession.dispose();
        log.info("Process status: {}", testProcess.getStatus());
    }

    public CompleteSystemState runSystem(CompleteSystemState completeSystemState) {
        KieSession kieSession = kieContainer.newKieSession("forwardSession");
        kieSession.insert(completeSystemState.getSystemState());
        completeSystemState.getCores().forEach(kieSession::insert);
        completeSystemState.getProcesses().forEach(kieSession::insert);
        kieSession.fireAllRules();
        kieSession.dispose();

        return completeSystemState;
    }
}
