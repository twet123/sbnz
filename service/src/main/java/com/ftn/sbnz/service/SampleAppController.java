package com.ftn.sbnz.service;

import com.ftn.sbnz.service.dtos.CompleteSystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SampleAppController {
    private static final Logger log = LoggerFactory.getLogger(SampleAppController.class);

    private final SampleAppService sampleService;

    @Autowired
    public SampleAppController(SampleAppService sampleService) {
        this.sampleService = sampleService;
    }

    @PostMapping("/schedule")
    public CompleteSystemState schedule(@RequestBody CompleteSystemState systemState) {
        return sampleService.runSystem(systemState);
    }

    @GetMapping("/test")
    public String test() {
        sampleService.test();
        return "Hello World!";
    }

}
