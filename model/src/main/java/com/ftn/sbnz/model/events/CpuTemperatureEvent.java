package com.ftn.sbnz.model.events;

import lombok.Data;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Expires("30m")
@Data
public class CpuTemperatureEvent {

    private float temperature;
}
