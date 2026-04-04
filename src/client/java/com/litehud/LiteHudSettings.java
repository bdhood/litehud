package com.litehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class LiteHudSettings {
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
        .getConfigDir().resolve("litehud.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static LiteHudSettings instance;

    public int textColor       = 0xFFAACC44;
    public int outlineColor    = 0xFFAACC44;
    public int backgroundColor = 0x80000000;
    public int toggleKey   = GLFW.GLFW_KEY_B;
    public int settingsKey = GLFW.GLFW_KEY_H;

    public boolean showTitle  = true;
    public boolean showFps    = true;
    public boolean showTps    = true;
    public boolean showPing   = true;
    public boolean showXyz    = true;
    public boolean showFacing = true;
    public boolean showSpeed  = true;

    public static LiteHudSettings get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static LiteHudSettings load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader r = new InputStreamReader(Files.newInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
                LiteHudSettings s = GSON.fromJson(r, LiteHudSettings.class);
                if (s != null) return s;
            } catch (Exception ignored) {}
        }
        return new LiteHudSettings();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }
}
