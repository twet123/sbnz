package com.ftn.sbnz.listener;

import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;

public class WorkingMemoryListener implements RuleRuntimeEventListener {

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        System.out.println("Fact updated: " + event.getObject());
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent objectDeletedEvent) {

    }
}
