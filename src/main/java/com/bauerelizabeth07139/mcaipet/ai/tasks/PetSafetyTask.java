package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class PetSafetyTask implements PetTask {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private final ServerLevel level;
    private boolean complete = false;
    private int tickCount = 0;
    private int stuckTicks = 0;
    private BlockPos lastPos;

    public PetSafetyTask(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
        this.level = player.serverLevel();
    }

    @Override
    public int getPriority() {
        return PetTask.PRIORITY_HIGH;
    }

    @Override
    public boolean canRun() {
        return isInDanger();
    }

    @Override
    public void tick() {
        tickCount++;
        checkStuck();

        PetBlackboard bb = pet.getData().getBlackboard();
        List<Monster> monsters = level.getEntitiesOfClass(
            Monster.class,
            player.getBoundingBox().inflate(12.0),
            e -> e.isAlive() && e.canAttack(player)
        );

        if (!monsters.isEmpty()) {
            Monster closest = monsters.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);

            if (closest != null) {
                bb.setCurrentTarget(closest);

                if (player.distanceTo(closest) < 3.0) {
                    player.attack(closest);
                }

                Vec3 away = player.position().subtract(closest.position()).normalize().scale(12);
                player.getNavigation().moveTo(
                    player.getX() + away.x,
                    player.getY(),
                    player.getZ() + away.z,
                    2.0
                );
            }
        } else {
            LivingEntity target = bb.getCurrentTarget();
            if (target != null && target.isAlive() && !target.isRemoved()) {
                if (player.distanceTo(target) < 4.0) {
                    player.attack(target);
                } else {
                    player.getNavigation().moveTo(
                        target.getX(), target.getY(), target.getZ(),
                        2.0
                    );
                }
            } else {
                complete = true;
            }
        }

        if (tickCount % 20 == 0) {
            lastPos = player.blockPosition();
        }
    }

    @Override
    public boolean isComplete() {
        return complete || !isInDanger();
    }

    @Override
    public void reset() {
        complete = false;
        tickCount = 0;
        stuckTicks = 0;
        lastPos = null;
        pet.getData().getBlackboard().setCurrentTarget(null);
    }

    private boolean isInDanger() {
        AABB area = player.getBoundingBox().inflate(10.0);
        List<Monster> monsters = level.getEntitiesOfClass(
            Monster.class, area,
            e -> e.isAlive() && e.canAttack(player)
        );
        return !monsters.isEmpty() || player.getHealth() < (player.getMaxHealth() * 0.5);
    }

    private void checkStuck() {
        if (lastPos != null && tickCount % 40 == 0) {
            if (player.blockPosition().distSqr(lastPos) < 2) {
                stuckTicks++;
                if (stuckTicks > 5) {
                    player.teleportTo(
                        player.getX() + (player.getRandom().nextDouble() - 0.5) * 10,
                        player.getY(),
                        player.getZ() + (player.getRandom().nextDouble() - 0.5) * 10
                    );
                    stuckTicks = 0;
                }
            } else {
                stuckTicks = 0;
            }
        }
    }
}
