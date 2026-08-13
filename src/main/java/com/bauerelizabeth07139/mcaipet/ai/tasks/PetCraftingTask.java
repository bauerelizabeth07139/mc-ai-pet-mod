package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PetCraftingTask implements PetTask {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private boolean complete = false;
    private int tickCount = 0;
    private CraftingPhase phase = CraftingPhase.CHECKING;
    private boolean swordCrafted = false;
    private boolean pickaxeCrafted = false;

    private enum CraftingPhase {
        CHECKING,
        GATHERING_IRON,
        CRAFTING_SWORD,
        CRAFTING_PICKAXE,
        DONE
    }

    public PetCraftingTask(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
    }

    @Override
    public int getPriority() {
        return PetTask.PRIORITY_NORMAL;
    }

    @Override
    public boolean canRun() {
        return needsBasicTools();
    }

    @Override
    public void tick() {
        tickCount++;

        if (!hasBasicTools()) {
            craftBasicTools();
        }

        if (hasBasicTools()) {
            complete = true;
        }
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public void reset() {
        complete = false;
        tickCount = 0;
        phase = CraftingPhase.CHECKING;
        swordCrafted = false;
        pickaxeCrafted = false;
    }

    private boolean needsBasicTools() {
        int ironCount = countItem(Items.IRON_INGOT);
        return ironCount >= 3 && !hasItem(Items.IRON_SWORD) && !hasItem(Items.IRON_PICKAXE);
    }

    private boolean hasBasicTools() {
        return hasItem(Items.IRON_SWORD) || hasItem(Items.IRON_PICKAXE);
    }

    private void craftBasicTools() {
        int ironCount = countItem(Items.IRON_INGOT);

        if (!swordCrafted && ironCount >= 2) {
            removeItems(Items.IRON_INGOT, 2);
            player.getInventory().add(new ItemStack(Items.IRON_SWORD));
            swordCrafted = true;
        } else if (!pickaxeCrafted && ironCount >= 3) {
            removeItems(Items.IRON_INGOT, 3);
            player.getInventory().add(new ItemStack(Items.IRON_PICKAXE));
            pickaxeCrafted = true;
        }
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
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    private void removeItems(net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(stack.getCount(), remaining);
                ItemStack newStack = stack.copy();
                newStack.shrink(toRemove);
                player.getInventory().setItem(i, newStack.isEmpty() ? ItemStack.EMPTY : newStack);
                remaining -= toRemove;
            }
        }
    }
}
