package com.bauerelizabeth07139.mcaipet.pet;

import com.bauerelizabeth07139.mcaipet.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.player.FakePlayerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PetManager {

    private static PetManager instance;
    private final Map<UUID, AIPetEntity> pets = new ConcurrentHashMap<>();
    private ServerLevel serverLevel;
    private boolean initialized = false;

    private PetManager() {}

    public static PetManager getInstance() {
        if (instance == null) {
            instance = new PetManager();
        }
        return instance;
    }

    public static void init() {
        getInstance();
    }

    public static void init(net.minecraft.server.MinecraftServer server) {
        PetManager instance = getInstance();
        instance.serverLevel = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        instance.loadData();
    }

    public void spawnPets(int count) {
        if (serverLevel == null) return;
        for (int i = 0; i < count; i++) {
            spawnOne(i);
        }
    }

    private void spawnOne(int index) {
        try {
            UUID uuid = UUID.randomUUID();
            String name = "AI Pet_" + index;

            ServerPlayer fakePlayer = FakePlayerFactory.get(serverLevel, name);
            if (fakePlayer == null) {
                System.err.println("[McAiPet] 无法创建智能宠物: " + name);
                return;
            }

            fakePlayer.setPos(serverLevel.getSharedSpawnPos().getX(), serverLevel.getSharedSpawnPos().getY(), serverLevel.getSharedSpawnPos().getZ());
            serverLevel.addFreshEntity(fakePlayer);

            AIPetEntity petEntity = new AIPetEntity(fakePlayer, name);
            pets.put(uuid, petEntity);

            petEntity.getData().getBlackboard().setSweepRange(ModConfig.SWEEP_RANGE);
            petEntity.getData().getBlackboard().setGuardRange(ModConfig.GUARD_RANGE);

        } catch (Exception e) {
            System.err.println("[McAiPet] 生成智能宠物失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void dismissAll() {
        for (AIPetEntity pet : pets.values()) {
            pet.remove(serverLevel);
        }
        pets.clear();
        saveData();
    }

    public int getPetCount() {
        return pets.size();
    }

    public Collection<AIPetEntity> getAllPets() {
        return pets.values();
    }

    public AIPetEntity getByUuid(UUID uuid) {
        return pets.get(uuid);
    }

    public AIPetEntity getByName(String name) {
        for (AIPetEntity pet : pets.values()) {
            if (pet.getData().getName().equals(name)) {
                return pet;
            }
        }
        return null;
    }

    public void tick() {
        if (serverLevel == null) return;

        Iterator<Map.Entry<UUID, AIPetEntity>> iterator = pets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, AIPetEntity> entry = iterator.next();
            AIPetEntity pet = entry.getValue();

            if (pet.isRemoved() || !pet.getFakePlayer().isAlive()) {
                iterator.remove();
                continue;
            }

            pet.tick();
        }
    }

    public void tickPets(Player realPlayer) {
        tick();

        for (AIPetEntity pet : pets.values()) {
            if (pet.getData().getMode() == PetData.Mode.GUARD) {
                if (pet.getData().getBlackboard().getOwnerUuid() == null) {
                    Player nearest = findNearestRealPlayer(pet);
                    if (nearest != null) {
                        pet.getData().getBlackboard().setOwnerUuid(nearest.getUUID());
                    }
                }
            }
        }
    }

    public Player findNearestRealPlayer(AIPetEntity pet) {
        if (serverLevel == null || pet == null) return null;

        UUID petUuid = pet.getUuid();
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : serverLevel.players()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            if (player.getUUID().equals(petUuid)) continue;
            if (player instanceof ServerPlayer && getByUuid(player.getUUID()) != null) continue;

            double dist = player.distanceTo(pet.getFakePlayer());
            if (dist > 100.0) continue;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    private void saveData() {
        try {
            File file = new File("config/mcaipet_virtual_players.dat");
            file.getParentFile().mkdirs();
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
                CompoundTag tag = new CompoundTag();
                ListTag list = new ListTag();
                for (AIPetEntity pet : pets.values()) {
                    list.add(pet.getData().saveNBT());
                }
                tag.put("Pets", list);
                net.minecraft.nbt.NbtIo.write(tag, out);
            }
        } catch (Exception e) {
            System.err.println("[McAiPet] 保存智能宠物数据失败: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            File file = new File("config/mcaipet_virtual_players.dat");
            if (!file.exists()) return;

            try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
                CompoundTag tag = net.minecraft.nbt.NbtIo.read(in);
                if (tag.contains("Pets")) {
                    ListTag list = tag.getList("Pets", Tag.TAG_COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        PetData data = PetData.loadNBT(list.getCompound(i));
                        if (data.isActive()) {
                            spawnOneWithData(data);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[McAiPet] 加载智能宠物数据失败: " + e.getMessage());
        }
    }

    private void spawnOneWithData(PetData data) {
        try {
            ServerPlayer fakePlayer = FakePlayerFactory.get(serverLevel, data.getName());
            if (fakePlayer == null) return;

            fakePlayer.setPos(serverLevel.getSharedSpawnPos().getX(), serverLevel.getSharedSpawnPos().getY(), serverLevel.getSharedSpawnPos().getZ());
            serverLevel.addFreshEntity(fakePlayer);

            AIPetEntity petEntity = new AIPetEntity(fakePlayer, data.getName());
            petEntity.getData().setMode(data.getMode());
            petEntity.getData().setHealth(data.getHealth());
            petEntity.getData().setActive(data.isActive());
            petEntity.getData().setPetName(data.getPetName());
            petEntity.getData().setOwnerUuid(data.getOwnerUuid());
            petEntity.getData().getBlackboard().setLevel(serverLevel);
            if (data.getBlackboard() != null) {
                petEntity.getData().getBlackboard().loadNBT(data.getBlackboard().saveNBT());
            }

            pets.put(data.getUuid(), petEntity);
        } catch (Exception e) {
            System.err.println("[McAiPet] 恢复智能宠物失败: " + e.getMessage());
        }
    }

    public void setServerLevel(ServerLevel level) {
        this.serverLevel = level;
    }
}
