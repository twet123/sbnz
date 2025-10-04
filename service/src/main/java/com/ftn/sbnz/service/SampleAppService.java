package com.ftn.sbnz.service;

import com.ftn.sbnz.service.dtos.EventListDto;
import com.ftn.sbnz.service.dtos.SystemStateDto;
import com.ftn.sbnz.utils.DroolsUtil;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SampleAppService {

    private static final Logger log = LoggerFactory.getLogger(SampleAppService.class);


    public EventListDto runSystem(SystemStateDto systemState) {
        KieSession kieSession = DroolsUtil.getSession();

        kieSession.insert(systemState.getSystem());
        systemState.getProcessModels().forEach(kieSession::insert);
        systemState.getProcesses().forEach(kieSession::insert);

        kieSession.fireUntilHalt();

        return new EventListDto();
    }
}
