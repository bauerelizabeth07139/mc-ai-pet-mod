package com.bauerelizabeth07139.mcaipet.commands;

import com.bauerelizabeth07139.mcaipet.pet.PetData;
import com.bauerelizabeth07139.mcaipet.pet.AIPetEntity;
import com.bauerelizabeth07139.mcaipet.pet.PetManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.EntityArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class PetCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> aipet = dispatcher.register(Commands.literal("aipet")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("spawn")
                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50))
                    .executes(context -> spawn(context.getSource(), IntegerArgumentType.getInteger(context, "count")))))
            .then(Commands.literal("mode")
                .then(Commands.argument("mode", StringArgumentType.word())
                    .executes(context -> setMode(context.getSource(), StringArgumentType.getString(context, "mode")))))
            .then(Commands.literal("stop")
                .executes(context -> stop(context.getSource())))
            .then(Commands.literal("dismiss")
                .executes(context -> dismiss(context.getSource())))
            .then(Commands.literal("count")
                .executes(context -> count(context.getSource())))
            .then(Commands.literal("follow")
                .then(Commands.argument("player", EntityArgumentType.player())
                    .executes(context -> follow(context.getSource(), EntityArgumentType.getPlayer(context, "player")))))
        );
        dispatcher.register(Commands.literal("pet")
            .requires(source -> source.hasPermission(2))
            .redirect(aipet)
        );
    }

    private static int spawn(CommandSourceStack source, int count) throws CommandSyntaxException {
        PetManager manager = PetManager.getInstance();
        manager.spawnPets(count);
        source.sendSuccess(() -> Component.literal("已生成 " + count + " 个AI宠物！当前总数: " + manager.getPetCount())
            .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int setMode(CommandSourceStack source, String modeStr) throws CommandSyntaxException {
        PetManager manager = PetManager.getInstance();
        PetData.Mode mode;

        try {
            mode = PetData.Mode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("无效的模式: " + modeStr +
                "。可用模式: survival, building, guard, sweep").withStyle(ChatFormatting.RED));
            return 0;
        }

        int count = 0;
        for (AIPetEntity pet : manager.getAllPets()) {
            pet.getData().setMode(mode);
            pet.getData().getBlackboard().reset();
            count++;
        }

        MutableComponent message = Component.literal("已将 " + count + " 个AI宠物的模式设置为: ")
            .withStyle(ChatFormatting.YELLOW);
        message.append(Component.literal(mode.name()).withStyle(ChatFormatting.AQUA));
        source.sendSuccess(() -> message, true);
        return count;
    }

    private static int stop(CommandSourceStack source) {
        PetManager manager = PetManager.getInstance();
        int count = 0;
        for (AIPetEntity pet : manager.getAllPets()) {
            pet.getData().setMode(PetData.Mode.IDLE);
            pet.getData().getBlackboard().reset();
            count++;
        }

        source.sendSuccess(() -> Component.literal("已停止 " + count + " 个AI宠物的AI")
            .withStyle(ChatFormatting.YELLOW), true);
        return count;
    }

    private static int dismiss(CommandSourceStack source) {
        PetManager manager = PetManager.getInstance();
        int count = manager.getPetCount();
        manager.dismissAll();

        source.sendSuccess(() -> Component.literal("已移除 " + count + " 个AI宠物")
            .withStyle(ChatFormatting.RED), true);
        return count;
    }

    private static int count(CommandSourceStack source) {
        PetManager manager = PetManager.getInstance();
        int count = manager.getPetCount();

        MutableComponent message = Component.literal("当前AI宠物数量: ")
            .withStyle(ChatFormatting.WHITE);
        message.append(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.GREEN));

        if (count > 0) {
            message.append(Component.literal("\n列表:").withStyle(ChatFormatting.GRAY));
            for (AIPetEntity pet : manager.getAllPets()) {
                message.append(Component.literal("\n- " + pet.getData().getName() +
                    " (" + pet.getData().getMode() + ")")
                    .withStyle(ChatFormatting.GRAY));
            }
        }

        source.sendSuccess(() -> message, false);
        return count;
    }

    private static int follow(CommandSourceStack source, Player targetPlayer) throws CommandSyntaxException {
        PetManager manager = PetManager.getInstance();
        Player sender = source.getPlayerOrException();
        int count = 0;

        for (AIPetEntity pet : manager.getAllPets()) {
            if (pet.getData().getMode() == PetData.Mode.GUARD) {
                pet.getData().getBlackboard().setOwnerUuid(targetPlayer.getUUID());
                pet.getData().getBlackboard().setGuardRange(200);
                count++;
            }
        }

        if (count > 0) {
            source.sendSuccess(() -> Component.literal("已将 " + count + " 个AI宠物设置为跟随 " + targetPlayer.getName().getString())
                .withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendSuccess(() -> Component.literal("没有找到护卫模式的AI宠物").withStyle(ChatFormatting.YELLOW), true);
        }

        return count;
    }
}
