package com.ftn.sbnz.service.dtos;

import com.ftn.sbnz.model.enums.InstructionType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessDto {

    private String id;
    private int instructions;
    private List<Integer> ioInstructions;
    private int memoryRequirement;
    private int priority;
    private int safeMemoryLimit;

    public List<InstructionType> getInstructionTypes() {
        List<InstructionType> instructionTypes = new ArrayList<>();

        for (int i = 0; i < instructions; i++) {
            if (ioInstructions.contains(i + 1)) {
                instructionTypes.add(InstructionType.IO);
            } else {
                instructionTypes.add(InstructionType.REGULAR);
            }
        }

        return instructionTypes;
    }
}
