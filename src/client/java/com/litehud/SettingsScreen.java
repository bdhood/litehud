package com.litehud;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SettingsScreen extends Screen {
    private final Screen parent;

    // Left column — colors
    private EditBox textColorField;
    private EditBox outlineColorField;
    private EditBox bgColorField;
    private int pendingTextColor;
    private int pendingOutlineColor;
    private int pendingBgColor;

    // Left column — keys
    private Button toggleKeyButton;
    private Button settingsKeyButton;
    private boolean capturingToggleKey   = false;
    private boolean capturingSettingsKey = false;
    private int pendingToggleKey;
    private int pendingSettingsKey;

    // Right column — line visibility
    private static final String[] LINE_NAMES = {"Title", "FPS", "TPS", "Ping", "XYZ", "Facing", "Speed", "Mob Count"};
    private final boolean[] pendingVisible = new boolean[LINE_NAMES.length];
    private final Button[]  visibleButtons = new Button[LINE_NAMES.length];

    public SettingsScreen(Screen parent) {
        super(Component.literal("LiteHud " + version() + " Settings"));
        this.parent = parent;
        LiteHudSettings s = LiteHudSettings.get();
        this.pendingTextColor    = s.textColor;
        this.pendingOutlineColor = s.outlineColor;
        this.pendingBgColor      = s.backgroundColor;
        this.pendingToggleKey    = s.toggleKey;
        this.pendingSettingsKey  = s.settingsKey;
        pendingVisible[0] = s.showTitle;
        pendingVisible[1] = s.showFps;
        pendingVisible[2] = s.showTps;
        pendingVisible[3] = s.showPing;
        pendingVisible[4] = s.showXyz;
        pendingVisible[5] = s.showFacing;
        pendingVisible[6] = s.showSpeed;
        pendingVisible[7] = s.showMobCount;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;
        int row = 38;

        // lx = left edge of left column content (fields, labels, key buttons)
        // rx = left edge of right column buttons
        // Content spans lx to rx+85, centered on cx
        int lx = cx - 122;
        int rx = cx + 38;

        int f1 = cy - 82;
        int f2 = f1 + row;
        int f3 = f2 + row;
        int f4 = f3 + row;
        int f5 = f4 + row;

        capturingToggleKey   = false;
        capturingSettingsKey = false;

        textColorField    = colorField(lx, f1, pendingTextColor);
        outlineColorField = colorField(lx, f2, pendingOutlineColor);
        bgColorField      = colorField(lx, f3, pendingBgColor);
        addRenderableWidget(textColorField);
        addRenderableWidget(outlineColorField);
        addRenderableWidget(bgColorField);

        toggleKeyButton = Button.builder(keyLabel(pendingToggleKey), btn -> {
            capturingToggleKey = true;
            toggleKeyButton.setMessage(Component.literal("Press any key..."));
        }).bounds(lx, f4, 120, 20).build();
        addRenderableWidget(toggleKeyButton);

        settingsKeyButton = Button.builder(keyLabel(pendingSettingsKey), btn -> {
            capturingSettingsKey = true;
            settingsKeyButton.setMessage(Component.literal("Press any key..."));
        }).bounds(lx, f5, 120, 20).build();
        addRenderableWidget(settingsKeyButton);

        // Save/Cancel centered on cx
        addRenderableWidget(Button.builder(Component.literal("Save"), btn -> save())
            .bounds(cx - 62, f5 + 28, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> onClose())
            .bounds(cx + 4,  f5 + 28, 58, 20).build());

        // Right column — visibility toggles
        for (int i = 0; i < LINE_NAMES.length; i++) {
            final int idx = i;
            visibleButtons[i] = Button.builder(visibleLabel(i), btn -> {
                pendingVisible[idx] = !pendingVisible[idx];
                visibleButtons[idx].setMessage(visibleLabel(idx));
            }).bounds(rx, f1 + i * 22, 85, 20).build();
            addRenderableWidget(visibleButtons[i]);
        }
    }

    private static String version() {
        return FabricLoader.getInstance()
            .getModContainer("litehud")
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("?");
    }

    private EditBox colorField(int lx, int y, int initialColor) {
        EditBox field = new EditBox(font, lx, y, 120, 20, Component.empty());
        field.setMaxLength(8);
        field.setValue(String.format("%08X", initialColor));
        return field;
    }

    private Component keyLabel(int key) {
        String name = GLFW.glfwGetKeyName(key, 0);
        return Component.literal(name != null ? name.toUpperCase() : "Key " + key);
    }

    private Component visibleLabel(int idx) {
        return Component.literal(LINE_NAMES[idx] + ": " + (pendingVisible[idx] ? "ON" : "OFF"));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (capturingToggleKey) {
            if (event.key() != GLFW.GLFW_KEY_ESCAPE) pendingToggleKey = event.key();
            capturingToggleKey = false;
            toggleKeyButton.setMessage(keyLabel(pendingToggleKey));
            return true;
        }
        if (capturingSettingsKey) {
            if (event.key() != GLFW.GLFW_KEY_ESCAPE) pendingSettingsKey = event.key();
            capturingSettingsKey = false;
            settingsKeyButton.setMessage(keyLabel(pendingSettingsKey));
            return true;
        }
        return super.keyPressed(event);
    }

    private void save() {
        pendingTextColor    = parseColor(textColorField,    pendingTextColor);
        pendingOutlineColor = parseColor(outlineColorField, pendingOutlineColor);
        pendingBgColor      = parseColor(bgColorField,      pendingBgColor);

        LiteHudSettings s = LiteHudSettings.get();
        s.textColor       = pendingTextColor;
        s.outlineColor    = pendingOutlineColor;
        s.backgroundColor = pendingBgColor;
        s.toggleKey   = pendingToggleKey;
        s.settingsKey = pendingSettingsKey;
        s.showTitle    = pendingVisible[0];
        s.showFps      = pendingVisible[1];
        s.showTps      = pendingVisible[2];
        s.showPing     = pendingVisible[3];
        s.showXyz      = pendingVisible[4];
        s.showFacing   = pendingVisible[5];
        s.showSpeed    = pendingVisible[6];
        s.showMobCount     = pendingVisible[7];
        s.save();
        onClose();
    }

    private int parseColor(EditBox field, int fallback) {
        try {
            return (int) Long.parseLong(field.getValue(), 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int cx = width / 2;
        int cy = height / 2;
        int row = 38;

        int lx = cx - 122;
        int rx = cx + 38;

        int f1 = cy - 82;
        int f2 = f1 + row;
        int f3 = f2 + row;
        int f4 = f3 + row;
        int f5 = f4 + row;

        // Panel — equal 20px padding on each side of content (lx to rx+85)
        graphics.fill(lx - 20, cy - 120, rx + 105, f5 + 54, 0xC0000000);

        // Title — centered on cx
        graphics.text(font, title, cx - font.width(title) / 2, cy - 110, 0xFFFFFFFF, true);

        // Left column labels
        drawColorRow(graphics, lx, f1 - 11, f1, "Text Color (AARRGGBB):", textColorField);
        drawColorRow(graphics, lx, f2 - 11, f2, "Outline Color (AARRGGBB):", outlineColorField);
        drawColorRow(graphics, lx, f3 - 11, f3, "Background Color (AARRGGBB):", bgColorField);
        graphics.text(font, Component.literal("Toggle Key:"),   lx, f4 - 11, 0xFFAAAAAA, false);
        graphics.text(font, Component.literal("Settings Key:"), lx, f5 - 11, 0xFFAAAAAA, false);

        // Right column header
        graphics.text(font, Component.literal("Show Lines:"), rx, f1 - 11, 0xFFAAAAAA, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawColorRow(GuiGraphicsExtractor graphics, int lx, int labelY, int fieldY, String label, EditBox field) {
        graphics.text(font, Component.literal(label), lx, labelY, 0xFFAAAAAA, false);
        try {
            int color = (int) Long.parseLong(field.getValue(), 16);
            int sx = lx + 124; // swatch x: 120px field + 4px gap
            graphics.fill(sx,      fieldY,      sx + 20, fieldY + 20, color);
            graphics.fill(sx,      fieldY,      sx + 20, fieldY + 1,  0xFFFFFFFF);
            graphics.fill(sx,      fieldY + 19, sx + 20, fieldY + 20, 0xFFFFFFFF);
            graphics.fill(sx,      fieldY,      sx + 1,  fieldY + 20, 0xFFFFFFFF);
            graphics.fill(sx + 19, fieldY,      sx + 20, fieldY + 20, 0xFFFFFFFF);
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
