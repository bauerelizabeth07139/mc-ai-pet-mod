package com.bauerelizabeth07139.mcaipet.pet;

import com.bauerelizabeth07139.mcaipet.ai.PetAI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RemovalReason;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class AIPetEntity {
    private final ServerPlayer fakePlayer;
    private final PetData data;
    private final PetAI ai;
    private String skinName;
    private UUID skinOwnerUuid;

    public AIPetEntity(ServerPlayer fakePlayer, String name) {
        this.fakePlayer = fakePlayer;
        this.data = new PetData(fakePlayer.getUUID(), name);
        this.ai = new PetAI(this);
        this.skinName = name;
        this.skinOwnerUuid = null;
    }

    public ServerPlayer getFakePlayer() {
        return fakePlayer;
    }

    public Player getPlayer() {
        return fakePlayer;
    }

    public PetData getData() {
        return data;
    }

    public PetAI getAI() {
        return ai;
    }

    public UUID getUuid() {
        return fakePlayer.getUUID();
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public UUID getSkinOwnerUuid() {
        return skinOwnerUuid;
    }

    public void setSkinOwnerUuid(UUID skinOwnerUuid) {
        this.skinOwnerUuid = skinOwnerUuid;
    }

    public void inheritSkinFromPlayer(Player realPlayer) {
        if (realPlayer != null) {
            this.skinOwnerUuid = realPlayer.getUUID();
            this.skinName = realPlayer.getName().getString();

                if (fakePlayer instanceof ServerPlayer serverFake) {
                var prop = serverFake.getGameProfile();
                if (prop != null) {
                    var newProp = new com.mojang.authlib.GameProfile(
                        prop.getId(),
                        realPlayer.getName().getString()
                    );
                    fakePlayer.setGameProfile(newProp);
                }
            }
        }
    }

    public void tick() {
        data.tick();
        ai.tick();
        syncSkinWithOwner();
    }

    private void syncSkinWithOwner() {
        if (skinOwnerUuid != null && fakePlayer.serverLevel() != null) {
            Player owner = fakePlayer.serverLevel().getPlayerByUUID(skinOwnerUuid);
            if (owner != null && owner.isAlive()) {
                if (fakePlayer instanceof ServerPlayer serverFake) {
                    if (!owner.getName().getString().equals(
                        serverFake.getGameProfile().getName())) {
                        var prop = serverFake.getGameProfile();
                        if (prop != null) {
                            var newProp = new com.mojang.authlib.GameProfile(
                                prop.getId(),
                                owner.getName().getString()
                            );
                            fakePlayer.setGameProfile(newProp);
                        }
                    }
                }
            }
        }
    }

    public boolean isRemoved() {
        return fakePlayer.isRemoved();
    }

    public void remove(ServerLevel level) {
        if (!fakePlayer.isRemoved()) {
            fakePlayer.remove(RemovalReason.DISCARDED);
        }
    }
}
