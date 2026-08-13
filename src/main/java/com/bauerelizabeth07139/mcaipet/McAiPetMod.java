package com.bauerelizabeth07139.mcaipet;

import com.bauerelizabeth07139.mcaipet.config.ModConfig;
import com.bauerelizabeth07139.mcaipet.pet.PetManager;
import com.bauerelizabeth07139.mcaipet.commands.PetCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ServerPlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * 智能宠物Mod
 */
@Mod("mcaipet")
public class McAiPetMod {

    public McAiPetMod() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        PetManager.init();
        ModConfig.load();
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        PetManager.init(event.getServer());
        PetCommand.register(event.getCommandDispatcher());
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onServerPlayerTick(ServerPlayerEvent.Tick event) {
        PetManager.getInstance().tickPets(event.getPlayer());
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer realPlayer) {
            PetManager.getInstance().getAllPets().forEach(pet -> {
                if (pet.getData().getBlackboard().getOwnerUuid() != null &&
                    pet.getData().getBlackboard().getOwnerUuid().equals(realPlayer.getUUID())) {
                    if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                        pet.getData().getBlackboard().setCurrentTarget(attacker);
                        pet.getData().getBlackboard().addEnemy(attacker.getUUID());
                    }
                }
            });
        }
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer realPlayer) {
            PetManager.getInstance().getAllPets().forEach(pet -> {
                if (pet.getData().getBlackboard().getOwnerUuid() != null &&
                    pet.getData().getBlackboard().getOwnerUuid().equals(realPlayer.getUUID())) {
                    if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                        pet.getData().getBlackboard().setCurrentTarget(attacker);
                        pet.getData().getBlackboard().addEnemy(attacker.getUUID());
                    }
                }
            });
        }
    }
}
