package com.bauerelizabeth07139.mcaipet.ai.goals;

import com.bauerelizabeth07139.mcaipet.ai.tasks.PetTaskSystem;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;

public class SurvivalGoal {

    private final AIPetEntity pet;
    private final PetTaskSystem taskSystem;

    public SurvivalGoal(AIPetEntity pet) {
        this.pet = pet;
        this.taskSystem = new PetTaskSystem(pet);
    }

    public void tick() {
        taskSystem.tick();
    }
}
