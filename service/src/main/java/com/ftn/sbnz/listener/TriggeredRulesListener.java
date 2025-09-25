package com.ftn.sbnz.listener;

import org.kie.api.event.rule.*;

public class TriggeredRulesListener implements AgendaEventListener {

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        System.out.println("Rule triggered: " + event.getMatch().getRule().getName());
    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) { /* No-op */ }

    @Override
    public void matchCancelled(MatchCancelledEvent event) { /* No-op */ }

    @Override
    public void matchCreated(MatchCreatedEvent event) { /* No-op */ }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) { /* No-op */ }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) { /* No-op */ }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) { /* No-op */ }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) { /* No-op */ }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) { /* No-op */ }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) { /* No-op */ }
}
