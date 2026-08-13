package com.bauerelizabeth07139.mcaipet.ai.tasks;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PetMiningTask implements PetTask {

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private final ServerLevel level;
    private boolean complete = false;
    private int tickCount = 0;
    private BlockPos currentTarget;
    private boolean foundCoal = false;
    private boolean foundIron = false;

    public PetMiningTask(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
        this.level = player.serverLevel();
    }

    @Override
    public int getPriority() {
        return PetTask.PRIORITY_LOW;
    }

    @Override
    public boolean canRun() {
        return needsMining();
    }

    @Override
    public void tick() {
        tickCount++;
        PetBlackboard bb = pet.getData().getBlackboard();

        if (currentTarget == null || level.getBlockState(currentTarget).isAir()) {
            if (tickCount % 5 == 0) {
                findMiningTarget();
            }
        }

        if (currentTarget != null) {
            bb.setTargetPosition(currentTarget);

            if (player.distanceToSqr(Vec3.atCenterOf(currentTarget)) < 9.0) {
                BlockState state = level.getBlockState(currentTarget);
                if (!state.isAir() && state.getDestroySpeed(level, currentTarget) >= 0) {
                    level.destroyBlock(currentTarget, true, player);

                    if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
                        foundCoal = true;
                    }
                    if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
                        foundIron = true;
                    }

                    currentTarget = null;
                }
            } else {
                player.getNavigation().moveTo(
                    currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(),
                    1.5
                );
            }
        }

        if (foundIron) {
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
        currentTarget = null;
        foundCoal = false;
        foundIron = false;
        pet.getData().getBlackboard().setTargetPosition(null);
    }

    private boolean needsMining() {
        int ironCount = countItem(Items.IRON_INGOT);
        int diamondCount = countItem(Items.DIAMOND);
        int coalCount = countItem(Items.COAL);

        return ironCount < 8 || coalCount < 10 || (ironCount < 16 && diamondCount < 3);
    }

    private void findMiningTarget() {
        BlockPos playerPos = player.blockPosition();
        int minY = Math.max(level.getMinBuildHeight(), playerPos.getY() - 20);

        for (int y = playerPos.getY(); y > minY; y--) {
            for (int x = playerPos.getX() - 12; x <= playerPos.getX() + 12; x++) {
                for (int z = playerPos.getZ() - 12; z <= playerPos.getZ() + 12; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0) {
                        if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
                            if (!foundIron) {
                                currentTarget = pos;
                                return;
                            }
                        } else if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
                            if (!foundCoal && currentTarget == null) {
                                currentTarget = pos;
                            }
                        } else if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE_STONE)) {
                            if (countItem(Items.COBBLESTONE) < 32 && currentTarget == null) {
                                currentTarget = pos;
                            }
                        }
                    }
                }
            }
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
}
