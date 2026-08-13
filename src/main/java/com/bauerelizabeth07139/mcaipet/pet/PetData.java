package com.bauerelizabeth07139.mcaipet.pet;

import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

public class PetData {

    public enum Mode {
        IDLE,
        SURVIVAL,
        BUILDING,
        GUARD,
        SWEEP
    }

    private final UUID uuid;
    private String name;
    private String petName;
    private Mode mode;
    private float health;
    private int ticksExisted;
    private boolean active;
    private UUID ownerUuid;
    private final PetBlackboard blackboard;

    public PetData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.petName = name;
        this.mode = Mode.IDLE;
        this.health = 20.0f;
        this.ticksExisted = 0;
        this.active = true;
        this.ownerUuid = null;
        this.blackboard = new PetBlackboard();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    public void tick() {
        ticksExisted++;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public PetBlackboard getBlackboard() {
        return blackboard;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("UUID", uuid);
        tag.putString("Name", name);
        tag.putString("PetName", petName);
        tag.putString("Mode", mode.name());
        tag.putFloat("Health", health);
        tag.putInt("TicksExisted", ticksExisted);
        tag.putBoolean("Active", active);
        if (ownerUuid != null) {
            tag.putUUID("OwnerUUID", ownerUuid);
        }
        tag.put("Blackboard", blackboard.saveNBT());
        return tag;
    }

    public static PetData loadNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("UUID");
        String name = tag.getString("Name");
        PetData data = new PetData(uuid, name);
        data.petName = tag.getString("PetName");
        data.mode = Mode.valueOf(tag.getString("Mode"));
        data.health = tag.getFloat("Health");
        data.ticksExisted = tag.getInt("TicksExisted");
        data.active = tag.getBoolean("Active");
        if (tag.hasUUID("OwnerUUID")) {
            data.ownerUuid = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("Blackboard")) {
            data.blackboard.loadNBT(tag.getCompound("Blackboard"));
        }
        return data;
    }
}
