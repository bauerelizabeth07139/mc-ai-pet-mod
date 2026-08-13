package com.bauerelizabeth07139.mcaipet.ai;

import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PetBlackboard {

    private LivingEntity currentTarget;
    private final List<String> taskQueue = new ArrayList<>();
    private final List<ItemRequirement> itemRequirements = new ArrayList<>();
    private BlockPos targetPosition;
    private final List<UUID> enemyUuids = new ArrayList<>();
    private UUID ownerUuid;
    private int guardRange = 200;
    private int sweepRange = 50;
    private int scanRange = 50;
    private ServerLevel level;
    private int taskTimer;
    private int lastScanTick;
    private String buildingTemplate;
    private final List<ItemRequirement> buildingMaterials = new ArrayList<>();

    public LivingEntity getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(LivingEntity target) {
        this.currentTarget = target;
    }

    public List<String> getTaskQueue() {
        return taskQueue;
    }

    public void addTask(String task) {
        this.taskQueue.add(task);
    }

    public void clearTasks() {
        this.taskQueue.clear();
    }

    public List<ItemRequirement> getItemRequirements() {
        return itemRequirements;
    }

    public void addItemRequirement(String itemId, int count) {
        itemRequirements.add(new ItemRequirement(itemId, count));
    }

    public BlockPos getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(BlockPos pos) {
        this.targetPosition = pos;
    }

    public List<UUID> getEnemyUuids() {
        return enemyUuids;
    }

    public void addEnemy(UUID uuid) {
        if (!enemyUuids.contains(uuid)) {
            enemyUuids.add(uuid);
        }
    }

    public void clearEnemies() {
        enemyUuids.clear();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    public int getGuardRange() {
        return guardRange;
    }

    public void setGuardRange(int guardRange) {
        this.guardRange = guardRange;
    }

    public int getSweepRange() {
        return sweepRange;
    }

    public void setSweepRange(int sweepRange) {
        this.sweepRange = sweepRange;
    }

    public int getScanRange() {
        return scanRange;
    }

    public void setScanRange(int range) {
        this.scanRange = range;
    }

    public void setLevel(ServerLevel level) {
        this.level = level;
    }

    public int getTaskTimer() {
        return taskTimer;
    }

    public void setTaskTimer(int timer) {
        this.taskTimer = timer;
    }

    public void decrementTaskTimer() {
        if (taskTimer > 0) taskTimer--;
    }

    public int getLastScanTick() {
        return lastScanTick;
    }

    public void setLastScanTick(int tick) {
        this.lastScanTick = tick;
    }

    public String getBuildingTemplate() {
        return buildingTemplate;
    }

    public void setBuildingTemplate(String template) {
        this.buildingTemplate = template;
    }

    public List<ItemRequirement> getBuildingMaterials() {
        return buildingMaterials;
    }

    public void clearBuildingMaterials() {
        buildingMaterials.clear();
    }

    public void reset() {
        currentTarget = null;
        taskQueue.clear();
        itemRequirements.clear();
        targetPosition = null;
        enemyUuids.clear();
        taskTimer = 0;
        buildingTemplate = null;
        buildingMaterials.clear();
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        if (currentTarget != null) {
            tag.putUUID("CurrentTarget", currentTarget.getUUID());
        }
        ListTag taskList = new ListTag();
        for (String task : taskQueue) {
            CompoundTag taskTag = new CompoundTag();
            taskTag.putString("Task", task);
            taskList.add(taskTag);
        }
        tag.put("TaskQueue", taskList);
        tag.putInt("TaskTimer", taskTimer);
        tag.putInt("ScanRange", scanRange);
        tag.putInt("GuardRange", guardRange);
        tag.putInt("SweepRange", sweepRange);
        tag.putInt("LastScanTick", lastScanTick);
        if (targetPosition != null) {
            tag.putLong("TargetPosition", targetPosition.asLong());
        }
        ListTag enemyList = new ListTag();
        for (UUID uuid : enemyUuids) {
            CompoundTag enemyTag = new CompoundTag();
            enemyTag.putUUID("UUID", uuid);
            enemyList.add(enemyTag);
        }
        tag.put("Enemies", enemyList);
        if (ownerUuid != null) {
            tag.putUUID("OwnerUUID", ownerUuid);
        }
        if (buildingTemplate != null) {
            tag.putString("BuildingTemplate", buildingTemplate);
        }
        ListTag materialList = new ListTag();
        for (ItemRequirement req : buildingMaterials) {
            CompoundTag matTag = new CompoundTag();
            matTag.putString("ItemId", req.itemId);
            matTag.putInt("Count", req.count);
            materialList.add(matTag);
        }
        tag.put("BuildingMaterials", materialList);
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.hasUUID("CurrentTarget")) {
        }
        ListTag taskList = tag.getList("TaskQueue", Tag.TAG_STRING);
        if (tag.hasUUID("CurrentTarget")) {
            try {
                UUID targetUuid = tag.getUUID("CurrentTarget");
                if (level != null) {
                    Entity entity = level.getEntity(targetUuid);
                    if (entity instanceof LivingEntity living) {
                        this.currentTarget = living;
                    }
                }
            } catch (Exception e) {
                currentTarget = null;
            }
        }
        taskQueue.clear();
        for (int i = 0; i < taskList.size(); i++) {
            taskQueue.add(taskList.getString(i));
        }
        taskTimer = tag.getInt("TaskTimer");
        scanRange = tag.getInt("ScanRange");
        guardRange = tag.getInt("GuardRange");
        sweepRange = tag.getInt("SweepRange");
        lastScanTick = tag.getInt("LastScanTick");
        if (tag.contains("TargetPosition")) {
            targetPosition = BlockPos.of(tag.getLong("TargetPosition"));
        }
        ListTag enemyList = tag.getList("Enemies", Tag.TAG_COMPOUND);
        enemyUuids.clear();
        for (int i = 0; i < enemyList.size(); i++) {
            enemyUuids.add(enemyList.getCompound(i).getUUID("UUID"));
        }
        if (tag.hasUUID("OwnerUUID")) {
            ownerUuid = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("BuildingTemplate")) {
            buildingTemplate = tag.getString("BuildingTemplate");
        }
        ListTag materialList = tag.getList("BuildingMaterials", Tag.TAG_COMPOUND);
        buildingMaterials.clear();
        for (int i = 0; i < materialList.size(); i++) {
            CompoundTag matTag = materialList.getCompound(i);
            buildingMaterials.add(new ItemRequirement(matTag.getString("ItemId"), matTag.getInt("Count")));
        }
    }

    public static class ItemRequirement {
        public String itemId;
        public int count;

        public ItemRequirement(String itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
    }
}
