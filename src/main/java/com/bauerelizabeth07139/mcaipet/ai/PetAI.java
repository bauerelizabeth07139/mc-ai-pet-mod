package com.bauerelizabeth07139.mcaipet.ai;

import com.bauerelizabeth07139.mcaipet.pet.PetData;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import com.bauerelizabeth07139.mcaipet.ai.goals.BuildingGoal;
import com.bauerelizabeth07139.mcaipet.ai.goals.GuardGoal;
import com.bauerelizabeth07139.mcaipet.ai.goals.SweepGoal;
import com.bauerelizabeth07139.mcaipet.ai.tasks.PetTaskSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class PetAI {

    private final AIPetEntity pet;
    private final BuildingGoal buildingGoal;
    private final GuardGoal guardGoal;
    private final SweepGoal sweepGoal;
    private final PetTaskSystem taskSystem;
    private int tickCount = 0;

    public PetAI(AIPetEntity pet) {
        this.pet = pet;
        this.buildingGoal = new BuildingGoal(pet);
        this.guardGoal = new GuardGoal(pet);
        this.sweepGoal = new SweepGoal(pet);
        this.taskSystem = new PetTaskSystem(pet);
    }

    public void tick() {
        tickCount++;
        PetData data = pet.getData();
        ServerPlayer player = pet.getFakePlayer();

        data.setHealth(player.getHealth());

        switch (data.getMode()) {
            case IDLE:
                tickIdle();
                break;
            case SURVIVAL:
                taskSystem.tick();
                break;
            case BUILDING:
                buildingGoal.tick();
                break;
            case GUARD:
                if (data.getBlackboard().getOwnerUuid() == null) {
                    Player nearest = findNearestRealPlayer();
                    if (nearest != null) {
                        data.getBlackboard().setOwnerUuid(nearest.getUUID());
                        data.getBlackboard().setGuardRange(200);
                    }
                }
                guardGoal.tick();
                break;
            case SWEEP:
                data.getBlackboard().setSweepRange(50);
                sweepGoal.tick();
                break;
        }

        if (tickCount % 20 == 0) {
            cleanInvalidTargets();
        }
    }

    private void tickIdle() {
        ServerPlayer player = pet.getFakePlayer();
        if (tickCount % 100 == 0) {
            double angle = Math.random() * Math.PI * 2;
            double distance = 5 + Math.random() * 10;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            player.getNavigation().moveTo(x, player.getY(), z, 1.0);
        }
    }

    private Player findNearestRealPlayer() {
        if (pet.getFakePlayer().serverLevel() == null) return null;

        ServerLevel level = pet.getFakePlayer().serverLevel();
        UUID petUuid = pet.getUuid();
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            if (player.getUUID().equals(petUuid)) continue;
            if (player instanceof net.minecraft.server.level.ServerPlayer &&
                PetManager.getInstance().getByUuid(player.getUUID()) != null) continue;

            double dist = player.distanceTo(pet.getFakePlayer());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    private void cleanInvalidTargets() {
        PetBlackboard bb = pet.getData().getBlackboard();
        LivingEntity target = bb.getCurrentTarget();
        if (target != null && (target.isRemoved() || !target.isAlive())) {
            bb.setCurrentTarget(null);
        }
    }

    public AIPetEntity getPet() {
        return pet;
    }
}
