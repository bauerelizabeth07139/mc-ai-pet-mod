package com.bauerelizabeth07139.mcaipet.ai.goals;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.config.ModConfig;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class SweepGoal {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private final ServerLevel level;
    private int scanTick = 0;
    private int stuckTicks = 0;

    public SweepGoal(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
        this.level = player.serverLevel();
    }

    public void tick() {
        scanTick++;
        PetBlackboard bb = pet.getData().getBlackboard();

        if (scanTick % 10 == 0) {
            scanAndTarget();
        }

        LivingEntity target = bb.getCurrentTarget();
        if (target != null && target.isAlive() && !target.isRemoved()) {
            attackTarget(target);
        } else {
            findNewTarget();
        }
    }

    private void scanAndTarget() {
        int scanRange = pet.getData().getBlackboard().getSweepRange();
        AABB area = player.getBoundingBox().inflate(scanRange);

        List<LivingEntity> allEntities = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e.isAlive() && !e.isRemoved() && !(e instanceof Player) && !(e instanceof IronGolem));

        List<LivingEntity> hostile = new java.util.ArrayList<>();
        List<LivingEntity> neutral = new java.util.ArrayList<>();

        for (LivingEntity entity : allEntities) {
            if (entity == player || entity.getUUID().equals(player.getUUID())) continue;

            if (entity instanceof Monster) {
                hostile.add(entity);
            } else if (entity instanceof Animal || entity instanceof Villager) {
            } else if (isNeutral(entity)) {
                neutral.add(entity);
            }
        }

        LivingEntity target = null;
        if (!hostile.isEmpty()) {
            target = hostile.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);
        } else if (!neutral.isEmpty()) {
            target = neutral.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);
        }

        if (target != null) {
            pet.getData().getBlackboard().setCurrentTarget(target);
        }
    }

    private void findNewTarget() {
        int scanRange = pet.getData().getBlackboard().getSweepRange();
        AABB area = player.getBoundingBox().inflate(scanRange);

        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area,
            e -> e.isAlive() && e.distanceTo(player) < scanRange);

        if (!monsters.isEmpty()) {
            Monster closest = monsters.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);

            if (closest != null) {
                pet.getData().getBlackboard().setCurrentTarget(closest);
            }
        } else {
            if (pet.getData().getTicksExisted() % 100 == 0) {
                double angle = Math.random() * Math.PI * 2;
                double distance = 10 + Math.random() * 20;
                double x = player.getX() + Math.cos(angle) * distance;
                double z = player.getZ() + Math.sin(angle) * distance;
                player.getNavigation().moveTo(x, player.getY(), z, 1.5);
            }
        }
    }

    private void attackTarget(LivingEntity target) {
        double distance = player.distanceTo(target);

        if (distance > 3.5) {
            player.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 2.0);
        } else {
            player.attack(target);

            if (!target.isAlive()) {
                pet.getData().getBlackboard().setCurrentTarget(null);
            }
        }
    }

    private boolean isNeutral(LivingEntity entity) {
        return !(entity instanceof Animal) && !(entity instanceof Villager);
    }
}
