package com.ftn.sbnz.model.events;

import lombok.Data;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Data
public class IOEvent {

    private long processId;
}
