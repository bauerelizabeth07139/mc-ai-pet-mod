package com.bauerelizabeth07139.mcaipet.ai.goals;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class SurvivalGoal {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private final ServerLevel level;
    private int tickCount = 0;
    private int stuckTicks = 0;
    private BlockPos lastPos;

    public SurvivalGoal(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
        this.level = player.serverLevel();
    }

    public void tick() {
        tickCount++;
        PetBlackboard bb = pet.getData().getBlackboard();

        checkStuck();

        if (isHungry()) {
            tickHunger();
        } else if (isInDanger()) {
            tickSafety();
        } else if (needsMining()) {
            tickMining();
        } else if (needsCrafting()) {
            tickCrafting();
        } else if (needsExploring()) {
            tickExploring();
        } else {
            tickDragon();
        }

        if (tickCount % 20 == 0) {
            lastPos = player.blockPosition();
        }
    }

    private boolean isHungry() {
        return player.getFoodData().getFoodLevel() < 16;
    }

    private boolean isInDanger() {
        AABB area = player.getBoundingBox().inflate(8.0);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area, e -> e.isAlive() && e.canAttack(player));
        return !monsters.isEmpty() || player.getHealth() < 10;
    }

    private boolean needsMining() {
        int stoneCount = countItem(Items.COBBLESTONE);
        int ironCount = countItem(Items.IRON_INGOT);
        return stoneCount < 32 || ironCount < 8;
    }

    private boolean needsCrafting() {
        return player.getInventory().countItem(Items.IRON_INGOT) >= 3 &&
               !hasItem(Items.IRON_SWORD) &&
               !hasItem(Items.IRON_PICKAXE);
    }

    private boolean needsExploring() {
        return player.getRandom().nextFloat() < 0.01;
    }

    private void tickHunger() {
        if (hasFood()) {
            eatFood();
        } else {
            AABB area = player.getBoundingBox().inflate(16.0);
            List<Animal> animals = level.getEntitiesOfClass(Animal.class, area,
                e -> e.isAlive() && !e.isBaby() && isEdible(e));

            if (!animals.isEmpty()) {
                Animal target = animals.get(0);
                moveAndAttack(target);
            } else {
                wanderRandomly();
            }
        }
    }

    private void tickSafety() {
        AABB area = player.getBoundingBox().inflate(12.0);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area,
            e -> e.isAlive() && e.canAttack(player));

        if (!monsters.isEmpty()) {
            Monster closest = monsters.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);

            if (closest != null) {
                Vec3 away = player.position().subtract(closest.position()).normalize().scale(10);
                player.getNavigation().moveTo(
                    player.getX() + away.x,
                    player.getY(),
                    player.getZ() + away.z,
                    2.0
                );

                if (player.distanceTo(closest) < 3.0) {
                    player.attack(closest);
                }
            }
        }
    }

    private void tickMining() {
        PetBlackboard bb = pet.getData().getBlackboard();

        if (bb.getCurrentTarget() == null || bb.getTargetPosition() == null) {
            findMiningTarget();
        }

        if (bb.getTargetPosition() != null) {
            BlockPos target = bb.getTargetPosition();
            if (player.distanceToSqr(Vec3.atCenterOf(target)) < 9.0) {
                if (level.getBlockState(target).getDestroySpeed(level, target) >= 0) {
                    level.destroyBlock(target, true, player);
                    bb.setTargetPosition(null);
                }
            } else {
                player.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            }
        }
    }

    private void findMiningTarget() {
        BlockPos playerPos = player.blockPosition();
        for (int y = playerPos.getY() - 5; y > level.getMinBuildHeight(); y--) {
            for (int x = playerPos.getX() - 10; x <= playerPos.getX() + 10; x++) {
                for (int z = playerPos.getZ() - 10; z <= playerPos.getZ() + 10; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    if (state.is(Blocks.STONE) || state.is(Blocks.COAL_ORE) ||
                        state.is(Blocks.IRON_ORE) || state.is(Blocks.COPPER_ORE)) {
                        pet.getData().getBlackboard().setTargetPosition(pos);
                        return;
                    }
                }
            }
        }
    }

    private void tickCrafting() {
        if (player.getInventory().countItem(Items.IRON_INGOT) < 3) {
            findMiningTarget();
            BlockPos target = pet.getData().getBlackboard().getTargetPosition();
            if (target != null) {
                player.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            }
            return;
        }

        if (!hasItem(Items.IRON_SWORD)) {
            player.getInventory().add(new ItemStack(Items.IRON_SWORD));
        }
        if (!hasItem(Items.IRON_PICKAXE)) {
            player.getInventory().add(new ItemStack(Items.IRON_PICKAXE));
        }
    }

    private void tickExploring() {
        if (tickCount % 200 == 0) {
            double angle = Math.random() * Math.PI * 2;
            double distance = 20 + Math.random() * 30;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            player.getNavigation().moveTo(x, player.getY(), z, 1.0);
        }
    }

    private void tickDragon() {
        if (tickCount % 100 == 0) {
            AABB area = player.getBoundingBox().inflate(32.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e instanceof Monster);

            if (!targets.isEmpty()) {
                LivingEntity target = targets.get(0);
                player.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
                if (player.distanceTo(target) < 4.0) {
                    player.attack(target);
                }
            } else {
                wanderRandomly();
            }
        }
    }

    private void eatFood() {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEdible() && stack.getFoodProperties(player) != null) {
                player.getInventory().setItem(i, player.eat(level, stack));
                return;
            }
        }
    }

    private void moveAndAttack(LivingEntity target) {
        if (player.distanceTo(target) > 4.0) {
            player.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
        } else {
            player.attack(target);
        }
    }

    private void wanderRandomly() {
        if (tickCount % 100 == 0) {
            double angle = Math.random() * Math.PI * 2;
            double distance = 5 + Math.random() * 15;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            player.getNavigation().moveTo(x, player.getY(), z, 1.0);
        }
    }

    private boolean hasFood() {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEdible()) return true;
        }
        return false;
    }

    private boolean isEdible(Animal animal) {
        return animal.getType().is(net.minecraft.world.entity.EntityType.COW) ||
               animal.getType().is(net.minecraft.world.entity.EntityType.PIG) ||
               animal.getType().is(net.minecraft.world.entity.EntityType.SHEEP) ||
               animal.getType().is(net.minecraft.world.entity.EntityType.CHICKEN);
    }

    private int countItem(net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean hasItem(net.minecraft.world.item.Item item) {
        return countItem(item) > 0;
    }

    private void checkStuck() {
        if (lastPos != null && tickCount % 40 == 0) {
            if (player.blockPosition().distSqr(lastPos) < 2) {
                stuckTicks++;
                if (stuckTicks > 5) {
                    player.teleportTo(
                        player.getX() + (Math.random() - 0.5) * 10,
                        player.getY(),
                        player.getZ() + (Math.random() - 0.5) * 10
                    );
                    stuckTicks = 0;
                }
            } else {
                stuckTicks = 0;
            }
        }
    }
}
