package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.server.level.ServerPlayer;

public interface PetTask {

    int PRIORITY_CRITICAL = 0;
    int PRIORITY_HIGH = 1;
    int PRIORITY_NORMAL = 2;
    int PRIORITY_LOW = 3;
    int PRIORITY_IDLE = 4;

    int getPriority();

    boolean canRun();

    void tick();

    boolean isComplete();

    void reset();
}
