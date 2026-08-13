package com.bauerelizabeth07139.mcaipet.pet;

import com.bauerelizabeth07139.mcaipet.ai.PetAI;
import com.bauerelizabeth07139.mcaipet.ai.PetBlackboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.player.FakePlayerFactory;

import java.util.UUID;

public class AIPetEntity {
    private final ServerPlayer fakePlayer;
    private final PetData data;
    private final PetAI ai;

    public AIPetEntity(ServerPlayer fakePlayer, String name) {
        this.fakePlayer = fakePlayer;
        this.data = new PetData(fakePlayer.getUUID(), name);
        this.ai = new PetAI(this);
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

    public void tick() {
        data.tick();
        ai.tick();
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
