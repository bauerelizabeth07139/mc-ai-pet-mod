package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class PetFoodTask implements PetTask {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private boolean complete = false;
    private int tickCount = 0;

    public PetFoodTask(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
    }

    @Override
    public int getPriority() {
        return PetTask.PRIORITY_CRITICAL;
    }

    @Override
    public boolean canRun() {
        return player.getFoodData().getFoodLevel() < 6;
    }

    @Override
    public void tick() {
        tickCount++;
        if (hasFood()) {
            eatFood();
            complete = isFed();
        } else {
            findAndHuntFood();
            if (player.getFoodData().getFoodLevel() >= 6) {
                complete = true;
            }
        }
    }

    @Override
    public boolean isComplete() {
        return complete || player.getFoodData().getFoodLevel() >= 10;
    }

    @Override
    public void reset() {
        complete = false;
        tickCount = 0;
    }

    private boolean hasFood() {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEdible() && stack.getFoodProperties(player) != null) {
                return true;
            }
        }
        return false;
    }

    private void eatFood() {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEdible() && stack.getFoodProperties(player) != null) {
                player.getInventory().setItem(i, player.eat(player.serverLevel(), stack));
                return;
            }
        }
    }

    private boolean isFed() {
        return player.getFoodData().getFoodLevel() >= 10;
    }

    private void findAndHuntFood() {
        if (tickCount % 20 != 0) return;

        ServerLevel level = player.serverLevel();
        List<Animal> animals = level.getEntitiesOfClass(
            Animal.class,
            player.getBoundingBox().inflate(16.0),
            e -> e.isAlive() && !e.isBaby() && isEdible(e)
        );

        if (!animals.isEmpty()) {
            Animal target = animals.stream()
                .min(Comparator.comparingDouble(e -> e.distanceTo(player)))
                .orElse(null);

            if (target != null) {
                PetBlackboard bb = pet.getData().getBlackboard();
                bb.setCurrentTarget(target);

                if (player.distanceTo(target) > 4.0) {
                    player.getNavigation().moveTo(
                        target.getX(), target.getY(), target.getZ(),
                        1.5
                    );
                } else {
                    player.attack(target);
                }
            }
        } else {
            wanderRandomly();
        }
    }

    private void wanderRandomly() {
        if (tickCount % 100 != 0) return;

        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        double distance = 5 + player.getRandom().nextDouble() * 10;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        player.getNavigation().moveTo(x, player.getY(), z, 1.0);
    }

    private boolean isEdible(Animal animal) {
        return animal.getType().is(EntityType.COW) ||
               animal.getType().is(EntityType.PIG) ||
               animal.getType().is(EntityType.SHEEP) ||
               animal.getType().is(EntityType.CHICKEN) ||
               animal.getType().is(EntityType.RABBIT);
    }
}
