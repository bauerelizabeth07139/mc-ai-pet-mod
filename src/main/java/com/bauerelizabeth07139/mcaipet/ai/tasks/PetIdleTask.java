package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.server.level.ServerPlayer;

public class PetIdleTask implements PetTask {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private boolean complete = false;
    private int tickCount = 0;
    private double targetX;
    private double targetZ;
    private boolean hasTarget = false;

    public PetIdleTask(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
    }

    @Override
    public int getPriority() {
        return PetTask.PRIORITY_IDLE;
    }

    @Override
    public boolean canRun() {
        return true;
    }

    @Override
    public void tick() {
        tickCount++;

        if (!hasTarget || player.distanceToSqr(targetX, player.getY(), targetZ) < 2.0) {
            pickNewTarget();
        }

        if (hasTarget) {
            player.getNavigation().moveTo(targetX, player.getY(), targetZ, 0.8);
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void reset() {
        complete = false;
        tickCount = 0;
        hasTarget = false;
    }

    private void pickNewTarget() {
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        double distance = 5 + player.getRandom().nextDouble() * 10;
        targetX = player.getX() + Math.cos(angle) * distance;
        targetZ = player.getZ() + Math.sin(angle) * distance;
        hasTarget = true;
    }
}
