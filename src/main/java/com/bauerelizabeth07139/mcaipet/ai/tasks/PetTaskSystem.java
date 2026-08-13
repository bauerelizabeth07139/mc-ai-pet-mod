package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PetTaskSystem {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private final List<PetTask> tasks = new ArrayList<>();
    private PetTask currentTask;
    private int tickCount = 0;

    public PetTaskSystem(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
        registerTasks();
    }

    private void registerTasks() {
        tasks.add(new PetFoodTask(pet));
        tasks.add(new PetSafetyTask(pet));
        tasks.add(new PetCraftingTask(pet));
        tasks.add(new PetMiningTask(pet));
        tasks.add(new PetExplorationTask(pet));
        tasks.add(new PetIdleTask(pet));
    }

    public void tick() {
        tickCount++;
        PetBlackboard bb = pet.getData().getBlackboard();

        PetTask bestTask = null;
        int bestPriority = Integer.MAX_VALUE;

        for (PetTask task : tasks) {
            if (task.canRun() && !task.isComplete()) {
                if (task.getPriority() < bestPriority) {
                    bestPriority = task.getPriority();
                    bestTask = task;
                }
            }
        }

        if (bestTask != null) {
            if (currentTask != bestTask) {
                if (currentTask != null) {
                    currentTask.reset();
                }
                currentTask = bestTask;
            }

            currentTask.tick();
        }

        if (tickCount % 20 == 0) {
            cleanInvalidTargets();
        }
    }

    private void cleanInvalidTargets() {
        PetBlackboard bb = pet.getData().getBlackboard();
        var target = bb.getCurrentTarget();
        if (target != null && (target.isRemoved() || !target.isAlive())) {
            bb.setCurrentTarget(null);
        }
    }

    public void resetAll() {
        for (PetTask task : tasks) {
            task.reset();
        }
        currentTask = null;
        pet.getData().getBlackboard().setCurrentTarget(null);
        pet.getData().getBlackboard().setTargetPosition(null);
    }
}
