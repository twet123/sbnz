package com.ftn.sbnz.service;

import com.ftn.sbnz.service.dtos.SystemStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class SampleAppController {

    private static final Logger log = LoggerFactory.getLogger(SampleAppController.class);

    private final SampleAppService sampleService;

    @Autowired
    public SampleAppController(SampleAppService sampleService) {
        this.sampleService = sampleService;
    }

    @PostMapping("/schedule")
    public SystemStateDto schedule(@RequestBody SystemStateDto systemState) {
        log.info("Received system state: {}", systemState);
        return systemState;
    }
}
