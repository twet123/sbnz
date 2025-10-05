package com.ftn.sbnz.service;

import com.ftn.sbnz.listener.TriggeredRulesListener;
import com.ftn.sbnz.model.events.CpuTemperatureEvent;
import com.ftn.sbnz.model.events.IOEvent;
import com.ftn.sbnz.model.events.PageFaultEvent;
import com.ftn.sbnz.service.dtos.*;
import com.ftn.sbnz.utils.DroolsUtil;
import org.apache.commons.math3.util.Pair;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SampleAppService {

    private final WsHandler wsHandler;
    private float startingTemp = 50;
    private Random random = new Random();

    @Autowired
    public SampleAppService(WsHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    private static final Logger log = LoggerFactory.getLogger(SampleAppService.class);

    public EventListDto runSystem(SystemStateDto systemState) {
        TriggeredRulesListener rulesListener = new TriggeredRulesListener();

        KieSession kieSession = DroolsUtil.getSession();

        kieSession.addEventListener((AgendaEventListener) rulesListener);
        kieSession.addEventListener((RuleRuntimeEventListener) rulesListener);

        kieSession.insert(systemState.getSystemStateModel());
        systemState.getCpuCoreModels().forEach(kieSession::insert);
        systemState.getProcessModels().forEach(kieSession::insert);

        List<Thread> ioThreads = systemState.getProcesses().stream()
                .filter(processDto -> !processDto.getIoInstructions().isEmpty())
                .map(processDtoWithIo -> new Thread(() -> {
                    while (true) {
                        kieSession.insert(new IOEvent(Integer.parseInt(processDtoWithIo.getId())));
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException ignored) {
                            break;
                        }
                    }
                }))
                .collect(Collectors.toList());

        List<Thread> pageFaultThreads = systemState.getProcesses().stream()
                .map(processDto -> new Thread(() -> {
                    while(true) {
                        if (Math.random() < 0.2) {
                            kieSession.insert(new PageFaultEvent(Integer.parseInt(processDto.getId())));
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                            break;
                        }
                    }
                }))
                .collect(Collectors.toList());

        Thread temperatureThread = new Thread(() -> {
            while(true) {
                float change = (random.nextFloat() - 0.5f) * 4.0f;
                startingTemp += change;
                startingTemp = Math.max(20.0f, Math.min(130.0f, startingTemp));

                CpuTemperatureEvent lastEvent = new CpuTemperatureEvent(startingTemp);
                kieSession.insert(lastEvent);

                try {
                    wsHandler.sendToAll(String.valueOf(lastEvent.getTemperature()));
                    log.info("Sending temperature event: " + lastEvent.getTemperature());
                } catch (IOException e) {
                    log.error("Error sending temperature event to websocket: " + e.getMessage());
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        });

        temperatureThread.start();
        ioThreads.forEach(Thread::start);
        pageFaultThreads.forEach(Thread::start);

        kieSession.fireUntilHalt();
        kieSession.dispose();

        temperatureThread.interrupt();
        ioThreads.forEach(Thread::interrupt);
        pageFaultThreads.forEach(Thread::interrupt);

        return processTriggeredRules(rulesListener.getFiredRules());
    }

    private EventListDto processTriggeredRules(List<Pair<String, List<String>>> triggeredRules) {
        EventListDto eventListDto = new EventListDto();
        List<EventDto> eventDtos = new ArrayList<>();

        for (Pair<String, List<String>> triggeredRule : triggeredRules) {
            EventDto eventDto = new EventDto();

            if (triggeredRule.getFirst().contains("Make process ready")) {
                eventDto.setEventType(EventType.PROCESS_READY);
            } else if (triggeredRule.getFirst().contains("Schedule process")) {
                eventDto.setEventType(EventType.PROCESS_SCHEDULED);
            } else if (triggeredRule.getFirst().contains("Finish executing the process")) {
                eventDto.setEventType(EventType.PROCESS_FINISHED);
            } else if (triggeredRule.getFirst().contains("Stop system")) {
                eventDto.setEventType(EventType.END);
            } else if (triggeredRule.getFirst().contains("Block process on I/O")) {
                eventDto.setEventType(EventType.PROCESS_BLOCKED);
            } else if (triggeredRule.getFirst().contains("Preempt when there is a process")) {
                eventDto.setEventType(EventType.PREEMPTED);
            } else if (triggeredRule.getFirst().contains("Handle I/O events")) {
                eventDto.setEventType(EventType.IO_RECEIVED);
            } else if (triggeredRule.getFirst().contains("Handle page fault")) {
                eventDto.setEventType(EventType.PAGING);
            } else {
                continue;
            }

            eventDto.setProcessId(triggeredRule.getSecond().stream().filter(fact -> fact.startsWith("Process")).map(pFact -> {
                        String startMarker = "Process(id=";

                        int startIndex = pFact.indexOf("Process(id=");
                        startIndex += startMarker.length();

                        int endIndex = pFact.indexOf(", ", startIndex);
                        return pFact.substring(startIndex, endIndex);
                    }
            ).findFirst().orElse(null));

            eventDtos.add(eventDto);
        }

        eventListDto.setEvents(eventDtos);
        eventListDto.setRulesFired(triggeredRules.size());

        return eventListDto;
    }
}
