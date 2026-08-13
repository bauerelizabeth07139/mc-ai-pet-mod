package com.bauerelizabeth07139.mcaipet.ai.goals;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.config.ModConfig;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import com.bauerelizabeth07139.mcaipet.pet.PetData;
import com.bauerelizabeth07139.mcaipet.pet.PetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class GuardGoal {

    private final AIPetEntity pet;
    private final ServerPlayer guard;
    private final ServerLevel level;
    private Player owner;
    private int scanTick = 0;
    private int stuckTicks = 0;
    private boolean alerted = false;

    public GuardGoal(AIPetEntity pet) {
        this.pet = pet;
        this.guard = pet.getFakePlayer();
        this.level = guard.serverLevel();
    }

    public void tick() {
        PetBlackboard bb = pet.getData().getBlackboard();

        if (pet.getData().getOwnerUuid() == null) {
            Player nearest = PetManager.getInstance().findNearestRealPlayer(pet);
            if (nearest != null) {
                pet.getData().setOwnerUuid(nearest.getUUID());
                bb.setOwnerUuid(nearest.getUUID());
            }
        }

        if (owner == null) {
            owner = findOwner();
        }

        if (owner == null) {
            wander();
            return;
        }

        double distance = guard.distanceTo(owner);

        if (distance > bb.getGuardRange()) {
            moveToOwner();
        } else {
            scanTick++;
            if (scanTick % 10 == 0 || alerted) {
                scanForEnemies();
            }

            LivingEntity target = bb.getCurrentTarget();
            if (target != null && target.isAlive() && !target.isRemoved()) {
                attackTarget(target);
            } else if (alerted) {
                alerted = false;
                patrolNearOwner();
            }
        }
    }

    private Player findOwner() {
        PetBlackboard bb = pet.getData().getBlackboard();
        UUID ownerUuid = pet.getData().getOwnerUuid();
        if (ownerUuid == null) {
            ownerUuid = bb.getOwnerUuid();
        }
        if (ownerUuid == null) {
            AABB area = guard.getBoundingBox().inflate(100.0);
            UUID selfUuid = pet.getUuid();
            List<Player> players = level.getEntitiesOfClass(Player.class, area,
                p -> p.isAlive() && !p.isSpectator() && !p.getUUID().equals(selfUuid));

            for (Player player : players) {
                if (!(player instanceof ServerPlayer &&
                      PetManager.getInstance().getByUuid(player.getUUID()) != null)) {
                    pet.getData().setOwnerUuid(player.getUUID());
                    bb.setOwnerUuid(player.getUUID());
                    return player;
                }
            }

            if (!players.isEmpty()) {
                pet.getData().setOwnerUuid(players.get(0).getUUID());
                bb.setOwnerUuid(players.get(0).getUUID());
                return players.get(0);
            }
            return null;
        }

        for (Player player : level.players()) {
            if (player.getUUID().equals(ownerUuid) && !player.getUUID().equals(pet.getUuid())) {
                return player;
            }
        }
        return null;
    }

    private void moveToOwner() {
        if (owner == null) return;

        double targetX = owner.getX() + (Math.random() - 0.5) * 20;
        double targetZ = owner.getZ() + (Math.random() - 0.5) * 20;

        guard.getNavigation().moveTo(targetX, owner.getY(), targetZ, 2.0);
    }

    private void scanForEnemies() {
        if (owner == null) return;

        int guardRange = pet.getData().getBlackboard().getGuardRange();
        AABB area = new AABB(
            owner.getX() - guardRange, owner.getY() - 20, owner.getZ() - guardRange,
            owner.getX() + guardRange, owner.getY() + 20, owner.getZ() + guardRange
        );

        List<Monster> enemies = level.getEntitiesOfClass(Monster.class, area,
            e -> e.isAlive() && e.canAttack(owner) && e.distanceTo(owner) < 30);

        if (!enemies.isEmpty()) {
            alerted = true;
            Monster closest = enemies.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(owner)))
                .orElse(null);

            if (closest != null) {
                pet.getData().getBlackboard().setCurrentTarget(closest);
                pet.getData().getBlackboard().addEnemy(closest.getUUID());
                guard.sendSystemMessage(Component.literal("发现敌人: " + closest.getType().getDescription().getString()));
            }
        }
    }

    private void attackTarget(LivingEntity target) {
        double distance = guard.distanceTo(target);

        if (distance > 4.0) {
            guard.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 2.5);
        } else {
            guard.attack(target);

            if (!target.isAlive()) {
                pet.getData().getBlackboard().setCurrentTarget(null);
                alerted = false;
            }
        }
    }

    private void patrolNearOwner() {
        if (owner == null) return;

        if (pet.getData().getTicksExisted() % 200 == 0) {
            double angle = Math.random() * Math.PI * 2;
            double distance = 10 + Math.random() * 20;
            double x = owner.getX() + Math.cos(angle) * distance;
            double z = owner.getZ() + Math.sin(angle) * distance;
            guard.getNavigation().moveTo(x, owner.getY(), z, 1.5);
        }
    }

    private void wander() {
        if (pet.getData().getTicksExisted() % 100 == 0) {
            double angle = Math.random() * Math.PI * 2;
            double distance = 5 + Math.random() * 10;
            double x = guard.getX() + Math.cos(angle) * distance;
            double z = guard.getZ() + Math.sin(angle) * distance;
            guard.getNavigation().moveTo(x, guard.getY(), z, 1.0);
        }
    }
}
