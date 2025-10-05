package com.ftn.sbnz.model.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Data
@Expires("20s")
@AllArgsConstructor
public class CpuOverheatEvent {
}
