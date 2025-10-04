package com.ftn.sbnz.utils;

import com.ftn.sbnz.listener.TriggeredRulesListener;
import com.ftn.sbnz.listener.WorkingMemoryListener;
import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import java.io.InputStream;

public class DroolsUtil {

    public static KieSession getSession() {
        KieHelper kieHelper = new KieHelper();

        // adding template ruleset
        InputStream priorityBoostingTemplate = DroolsUtil.class.getResourceAsStream("/rules/template/priority-boosting.drt");
        InputStream data = DroolsUtil.class.getResourceAsStream("/rules/template/priority-boosting.xls");
        ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();
        String priorityBoostingRules = converter.compile(data, priorityBoostingTemplate, 2, 2);
        kieHelper.addContent(priorityBoostingRules, ResourceType.DRL);

        InputStream processAcceptanceTemplate = DroolsUtil.class.getResourceAsStream("/rules/template/process-acceptance.drt");
        data = DroolsUtil.class.getResourceAsStream("/rules/template/process-acceptance.xls");
        String processAcceptanceRules = converter.compile(data, processAcceptanceTemplate, 2, 2);
        kieHelper.addContent(processAcceptanceRules, ResourceType.DRL);

        // adding regular ruleset
        InputStream regularRules = DroolsUtil.class.getResourceAsStream("/rules/forward/forward.drl");
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
