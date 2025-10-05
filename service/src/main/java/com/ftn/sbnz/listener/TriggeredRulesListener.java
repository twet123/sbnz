package com.ftn.sbnz.listener;

import lombok.Getter;
import org.apache.commons.math3.util.Pair;
import org.kie.api.event.rule.*;

import java.util.ArrayList;
import java.util.List;

public class TriggeredRulesListener extends DefaultAgendaEventListener implements RuleRuntimeEventListener {

    @Getter
    private List<Pair<String, List<String>>> firedRules = new ArrayList<>();
    private Pair<String, List<String>> lastRuleFired;

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        String ruleName = event.getMatch().getRule().getName();
        System.out.println("Rule fired: " + ruleName);
        lastRuleFired = new Pair<>(ruleName, new ArrayList<>());
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        firedRules.add(lastRuleFired);
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        Object eventObject = event.getObject();
        System.out.println("Fact updated: " + eventObject);
        lastRuleFired.getValue().add(eventObject.toString());
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent objectDeletedEvent) {

    }
}
