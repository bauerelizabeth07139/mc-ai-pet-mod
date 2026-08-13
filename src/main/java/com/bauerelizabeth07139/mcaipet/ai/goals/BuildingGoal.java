package com.bauerelizabeth07139.mcaipet.ai.goals;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;


public class BuildingGoal {

    private enum BuildPhase {
        SELECTING_TEMPLATE,
        GATHERING_MATERIALS,
        BUILDING,
        COMPLETE
    }

    private final AIPetEntity pet;
    private final ServerPlayer player;
    private final ServerLevel level;
    private BuildPhase phase = BuildPhase.SELECTING_TEMPLATE;
    private String currentTemplate;
    private int buildIndex = 0;
    private List<BlockPos> buildPlan = new ArrayList<>();
    private BlockPos buildOrigin;
    private int gatherTick = 0;

    private static final Map<String, List<BlockInfo>> TEMPLATES = new HashMap<>();

    static {
        TEMPLATES.put("small_house", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(1, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(0, 0, 1, Blocks.OAK_PLANKS),
            new BlockInfo(0, 1, 0, Blocks.OAK_PLANKS),
            new BlockInfo(0, 1, 1, Blocks.OAK_PLANKS),
            new BlockInfo(1, 1, 0, Blocks.OAK_PLANKS),
            new BlockInfo(1, 1, 1, Blocks.GLASS),
            new BlockInfo(0, 2, 0, Blocks.OAK_STAIRS),
            new BlockInfo(1, 2, 0, Blocks.OAK_STAIRS),
            new BlockInfo(0, 2, 1, Blocks.OAK_STAIRS)
        ));

        TEMPLATES.put("tower", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 1, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 2, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 3, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 4, 0, Blocks.COBBLESTONE),
            new BlockInfo(1, 4, 0, Blocks.COBBLESTONE),
            new BlockInfo(-1, 4, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 4, 1, Blocks.COBBLESTONE),
            new BlockInfo(0, 4, -1, Blocks.COBBLESTONE),
            new BlockInfo(0, 5, 0, Blocks.TORCH)
        ));

        TEMPLATES.put("wall", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(1, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(2, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(3, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(4, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 1, 0, Blocks.COBBLESTONE),
            new BlockInfo(2, 1, 0, Blocks.COBBLESTONE),
            new BlockInfo(4, 1, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 2, 0, Blocks.COBBLESTONE),
            new BlockInfo(4, 2, 0, Blocks.COBBLESTONE)
        ));

        TEMPLATES.put("bridge", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(1, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(2, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(3, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(4, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(-1, 0, 0, Blocks.OAK_FENCE),
            new BlockInfo(5, 0, 0, Blocks.OAK_FENCE)
        ));

        TEMPLATES.put("farm", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.FARMLAND),
            new BlockInfo(1, 0, 0, Blocks.FARMLAND),
            new BlockInfo(2, 0, 0, Blocks.FARMLAND),
            new BlockInfo(0, 0, 1, Blocks.FARMLAND),
            new BlockInfo(1, 0, 1, Blocks.FARMLAND),
            new BlockInfo(2, 0, 1, Blocks.FARMLAND),
            new BlockInfo(0, 1, 0, Blocks.OAK_FENCE),
            new BlockInfo(2, 1, 0, Blocks.OAK_FENCE),
            new BlockInfo(0, 1, 1, Blocks.OAK_FENCE),
            new BlockInfo(2, 1, 1, Blocks.OAK_FENCE)
        ));

        TEMPLATES.put("lamp_post", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 1, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 2, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 3, 0, Blocks.COBBLESTONE),
            new BlockInfo(0, 4, 0, Blocks.TORCH)
        ));

        TEMPLATES.put("storage", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(1, 0, 0, Blocks.CHEST),
            new BlockInfo(0, 1, 0, Blocks.OAK_PLANKS),
            new BlockInfo(1, 1, 0, Blocks.CHEST),
            new BlockInfo(0, 2, 0, Blocks.OAK_STAIRS),
            new BlockInfo(1, 2, 0, Blocks.OAK_STAIRS)
        ));

        TEMPLATES.put("stairs", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.COBBLESTONE_STAIRS),
            new BlockInfo(0, 1, 0, Blocks.COBBLESTONE_STAIRS),
            new BlockInfo(0, 2, 0, Blocks.COBBLESTONE_STAIRS),
            new BlockInfo(0, 3, 0, Blocks.COBBLESTONE_STAIRS),
            new BlockInfo(0, 4, 0, Blocks.COBBLESTONE_STAIRS)
        ));

        TEMPLATES.put("platform", Arrays.asList(
            new BlockInfo(0, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(1, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(-1, 0, 0, Blocks.OAK_PLANKS),
            new BlockInfo(0, 0, 1, Blocks.OAK_PLANKS),
            new BlockInfo(0, 0, -1, Blocks.OAK_PLANKS),
            new BlockInfo(1, 0, 1, Blocks.OAK_PLANKS),
            new BlockInfo(-1, 0, -1, Blocks.OAK_PLANKS),
            new BlockInfo(1, 0, -1, Blocks.OAK_PLANKS),
            new BlockInfo(-1, 0, 1, Blocks.OAK_PLANKS)
        ));
    }

    public BuildingGoal(AIPetEntity pet) {
        this.pet = pet;
        this.player = pet.getFakePlayer();
        this.level = player.serverLevel();
        PetBlackboard bb = pet.getData().getBlackboard();
        if (bb.getBuildingTemplate() == null || bb.getBuildingTemplate().isEmpty()) {
            bb.setBuildingTemplate(selectRandomTemplate());
        }
    }

    public void tick() {
        PetBlackboard bb = pet.getData().getBlackboard();
        currentTemplate = bb.getBuildingTemplate();

        switch (phase) {
            case SELECTING_TEMPLATE:
                tickSelectingTemplate();
                break;
            case GATHERING_MATERIALS:
                tickGatheringMaterials();
                break;
            case BUILDING:
                tickBuilding();
                break;
            case COMPLETE:
                tickComplete();
                break;
        }
    }

    private void tickSelectingTemplate() {
        if (buildOrigin == null) {
            buildOrigin = player.blockPosition().offset(3, 0, 3);
            bb().setTargetPosition(buildOrigin);
        }

        prepareBuildPlan();
        phase = BuildPhase.GATHERING_MATERIALS;
    }

    private void tickGatheringMaterials() {
        gatherTick++;

        if (hasAllMaterials()) {
            phase = BuildPhase.BUILDING;
            buildIndex = 0;
            return;
        }

        gatherMissingMaterials();
    }

    private void tickBuilding() {
        if (buildIndex >= buildPlan.size()) {
            phase = BuildPhase.COMPLETE;
            player.sendSystemMessage(Component.literal("建筑完成!"));
            return;
        }

        BlockInfo info = buildPlan.get(buildIndex);
        BlockPos pos = buildOrigin.offset(info.x, info.y, info.z);

        if (player.distanceToSqr(Vec3.atCenterOf(pos)) > 25.0) {
            player.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.5);
            return;
        }

        BlockState currentState = level.getBlockState(pos);
        if (currentState.isAir() || currentState.is(Blocks.GRASS) || currentState.is(Blocks.TALL_GRASS)) {
            level.setBlockAndUpdate(pos, info.block.defaultBlockState());
        }

        buildIndex++;
    }

    private void tickComplete() {
        if (tickCount() % 200 == 0) {
            bb().setBuildingTemplate(selectRandomTemplate());
            phase = BuildPhase.SELECTING_TEMPLATE;
            buildOrigin = null;
            buildPlan.clear();
            buildIndex = 0;
            bb().getBuildingMaterials().clear();
        }
    }

    private void prepareBuildPlan() {
        buildPlan.clear();
        List<BlockInfo> template = TEMPLATES.getOrDefault(currentTemplate, Collections.emptyList());
        buildPlan.addAll(template);

        Map<net.minecraft.world.level.block.Block, Integer> materialCounts = new HashMap<>();
        for (BlockInfo info : buildPlan) {
            materialCounts.merge(info.block, 1, Integer::sum);
        }

        PetBlackboard bb = bb();
        bb.clearBuildingMaterials();
        for (Map.Entry<net.minecraft.world.level.block.Block, Integer> entry : materialCounts.entrySet()) {
            bb.addItemRequirement(entry.getKey().toString(), entry.getValue());
        }
    }

    private boolean hasAllMaterials() {
        PetBlackboard bb = bb();
        for (PetBlackboard.ItemRequirement req : bb.getBuildingMaterials()) {
            if (req.itemId.contains("planks") && countItem(Items.OAK_PLANKS) < req.count) return false;
            if (req.itemId.contains("cobblestone") && countItem(Items.COBBLESTONE) < req.count) return false;
            if (req.itemId.contains("glass") && countItem(Items.GLASS) < req.count) return false;
            if (req.itemId.contains("farmland") && countItem(Items.DIRT) < req.count) return false;
        }
        return true;
    }

    private void gatherMissingMaterials() {
        PetBlackboard bb = bb();
        BlockPos gatherPos = bb.getTargetPosition();

        if (gatherPos == null || player.distanceToSqr(Vec3.atCenterOf(gatherPos)) > 100.0) {
            findGatherTarget();
            return;
        }

        if (level.getBlockState(gatherPos).getDestroySpeed(level, gatherPos) >= 0) {
            level.destroyBlock(gatherPos, true, player);
            bb.setTargetPosition(null);
        }
    }

    private void findGatherTarget() {
        BlockPos playerPos = player.blockPosition();
        for (int y = playerPos.getY() - 5; y > level.getMinBuildHeight(); y--) {
            for (int x = playerPos.getX() - 8; x <= playerPos.getX() + 8; x++) {
                for (int z = playerPos.getZ() - 8; z <= playerPos.getZ() + 8; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    if (state.is(Blocks.STONE) || state.is(Blocks.DIRT) ||
                        state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SAND)) {
                        bb().setTargetPosition(pos);
                        return;
                    }
                }
            }
        }
    }

    private String selectRandomTemplate() {
        List<String> keys = new ArrayList<>(TEMPLATES.keySet());
        return keys.get(player.getRandom().nextInt(keys.size()));
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

    private int tickCount() {
        return pet.getData().getTicksExisted();
    }

    private PetBlackboard bb() {
        return pet.getData().getBlackboard();
    }

    private static class BlockInfo {
        int x, y, z;
        net.minecraft.world.level.block.Block block;

        BlockInfo(int x, int y, int z, net.minecraft.world.level.block.Block block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
        }
    }
}
