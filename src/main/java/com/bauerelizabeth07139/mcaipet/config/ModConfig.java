package com.bauerelizabeth07139.mcaipet.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.*;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/mcaipet.json");

    public static int DEFAULT_PET_COUNT = 3;
    public static int MAX_PET_COUNT = 20;
    public static int GUARD_RANGE = 200;
    public static int SWEEP_RANGE = 50;
    public static double PET_SPEED = 1.0;
    public static double AI_REACH = 4.0;
    public static int BUILD_TICK_INTERVAL = 100;

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                try (Reader reader = new FileReader(CONFIG_FILE)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json != null) {
                        DEFAULT_PET_COUNT = json.has("defaultPetCount") ? json.get("defaultPetCount").getAsInt() : DEFAULT_PET_COUNT;
                        MAX_PET_COUNT = json.has("maxPetCount") ? json.get("maxPetCount").getAsInt() : MAX_PET_COUNT;
                        GUARD_RANGE = json.has("guardRange") ? json.get("guardRange").getAsInt() : GUARD_RANGE;
                        SWEEP_RANGE = json.has("sweepRange") ? json.get("sweepRange").getAsInt() : SWEEP_RANGE;
                        PET_SPEED = json.has("petSpeed") ? json.get("petSpeed").getAsDouble() : PET_SPEED;
                        AI_REACH = json.has("aiReach") ? json.get("aiReach").getAsDouble() : AI_REACH;
                        BUILD_TICK_INTERVAL = json.has("buildTickInterval") ? json.get("buildTickInterval").getAsInt() : BUILD_TICK_INTERVAL;
                    }
                }
            } else {
                save();
            }
        } catch (Exception e) {
            System.err.println("[McAiPet] 加载配置失败: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            json.addProperty("defaultPetCount", DEFAULT_PET_COUNT);
            json.addProperty("maxPetCount", MAX_PET_COUNT);
            json.addProperty("guardRange", GUARD_RANGE);
            json.addProperty("sweepRange", SWEEP_RANGE);
            json.addProperty("petSpeed", PET_SPEED);
            json.addProperty("aiReach", AI_REACH);
            json.addProperty("buildTickInterval", BUILD_TICK_INTERVAL);
            try (Writer writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            System.err.println("[McAiPet] 保存配置失败: " + e.getMessage());
        }
    }
}
