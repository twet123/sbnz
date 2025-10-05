package com.ftn.sbnz.model.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Expires("5s")
@Data
@AllArgsConstructor
public class CpuTemperatureEvent {

    private float temperature;
}
