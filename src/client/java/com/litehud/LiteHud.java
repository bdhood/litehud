package com.litehud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class LiteHud implements ClientModInitializer {
	private static final int SAMPLE_SIZE = 10;
	private static final long[] tickTimes = new long[SAMPLE_SIZE];
	private static int tickCount = 0;
	private static long lastTickNanos = 0;
	private static volatile double tps = 20.0;
	private static boolean visible = true;
	private static boolean lastKeyState = false;
	private static boolean lastSettingsKeyState = false;
	private static double lastPlayerX = Double.NaN;
	private static double lastPlayerY = Double.NaN;
	private static double lastPlayerZ = Double.NaN;
	private static volatile double speedBps = 0.0;

	private static final int OX = 6;  // horizontal offset from screen edge
	private static final int OY = 6;  // vertical offset from screen edge

	private static final Style TEXT_STYLE = Style.EMPTY.withFont(
		new FontDescription.Resource(Identifier.fromNamespaceAndPath("litehud", "roboto_mono"))
	);

	private static Component line(String text) {
		return Component.literal(text).withStyle(TEXT_STYLE);
	}

	@Override
	public void onInitializeClient() {
		String version = FabricLoader.getInstance()
			.getModContainer("litehud")
			.map(c -> c.getMetadata().getVersion().getFriendlyString())
			.orElse("?");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean keyDown = GLFW.glfwGetKey(
				client.getWindow().handle(), LiteHudSettings.get().toggleKey
			) == GLFW.GLFW_PRESS;
			if (keyDown && !lastKeyState && client.screen == null) {
				visible = !visible;
			}
			lastKeyState = keyDown;

			boolean settingsKeyDown = GLFW.glfwGetKey(
				client.getWindow().handle(), LiteHudSettings.get().settingsKey
			) == GLFW.GLFW_PRESS;
			if (settingsKeyDown && !lastSettingsKeyState && client.screen == null) {
				client.setScreen(new SettingsScreen(null));
			}
			lastSettingsKeyState = settingsKeyDown;

			long now = System.nanoTime();
			if (lastTickNanos != 0) {
				tickTimes[(tickCount & Integer.MAX_VALUE) % SAMPLE_SIZE] = now - lastTickNanos;
				tickCount++;
				if (tickCount >= SAMPLE_SIZE) {
					long sum = 0;
					for (long t : tickTimes) sum += t;
					double avgMs = sum / (double) SAMPLE_SIZE / 1_000_000.0;
					tps = Math.min(20.0, 1000.0 / avgMs);
				}
			}
			lastTickNanos = now;

			if (client.player != null) {
				double px = client.player.getX();
				double py = client.player.getY();
				double pz = client.player.getZ();
				if (!Double.isNaN(lastPlayerX)) {
					double dx = px - lastPlayerX;
					double dy = py - lastPlayerY;
					double dz = pz - lastPlayerZ;
					speedBps = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
				}
				lastPlayerX = px;
				lastPlayerY = py;
				lastPlayerZ = pz;
			} else {
				lastPlayerX = Double.NaN;
				speedBps = 0.0;
			}
		});

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("litehud", "coords"), (gui, deltaTracker) -> {
			if (!visible) return;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;
			var font = mc.font;

			// Build visible lines
			LiteHudSettings s = LiteHudSettings.get();
			float yaw = Mth.wrapDegrees(mc.player.getYRot());
			float absYaw = Math.abs(yaw);
			String cardinal = absYaw < 45f ? "South" : absYaw > 135f ? "North" : yaw > 0 ? "West " : "East ";
			java.util.List<String> lines = new java.util.ArrayList<>();
			if (s.showTitle)  lines.add("LiteHud " + version);
			if (s.showFps)    lines.add(String.format("FPS: %d", mc.getFps()));
			if (s.showTps)    lines.add(String.format("TPS: %.1f", tps));
			if (s.showPing && mc.getCurrentServer() != null && mc.getConnection() != null) {
				var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
				if (info != null) lines.add(String.format("Ping: %dms", info.getLatency()));
			}
			if (s.showXyz)    lines.add(String.format("XYZ: %.1f / %5.1f / %.1f",
				mc.player.getX(), mc.player.getY(), mc.player.getZ()));
			if (s.showFacing) lines.add(String.format("Facing: %s %6.1f / %3.1f",
				cardinal, yaw, mc.player.getXRot()));
			if (s.showSpeed)  lines.add(String.format("Speed: %.2f b/s", speedBps));
			if (s.showMobCount && mc.level != null) {
				int mobCount = 0;
				for (var e : mc.level.entitiesForRendering())
					if (e instanceof net.minecraft.world.entity.Mob) mobCount++;
				lines.add(String.format("Mob Count: %d", mobCount));
			}
			if (lines.isEmpty()) return;

			// Measure and draw background box
			int pad = 3;
			int maxW = 0;
			for (String l : lines) maxW = Math.max(maxW, font.width(line(l)));
			maxW += 20; // buffer so small value changes don't resize the box
			int boxX1 = OX;
			int boxY1 = OY;
			int boxX2 = OX + pad + 2 + maxW + pad;
			int boxY2 = OY + pad + (lines.size() - 1) * 9 + 9 + pad;
			gui.fill(boxX1,     boxY1,     boxX2,     boxY2,     s.backgroundColor); // background
			gui.fill(boxX1,     boxY1,     boxX2,     boxY1 + 1, s.outlineColor); // top
			gui.fill(boxX1,     boxY2 - 1, boxX2,     boxY2,     s.outlineColor); // bottom
			gui.fill(boxX1,     boxY1,     boxX1 + 1, boxY2,     s.outlineColor); // left
			gui.fill(boxX2 - 1, boxY1,     boxX2,     boxY2,     s.outlineColor); // right

			// Draw text
			int tx = OX + 2 + pad;
			int ty = OY + pad;
			for (int i = 0; i < lines.size(); i++) {
				gui.text(font, line(lines.get(i)), tx, ty + 9 * i, s.textColor, false);
			}
		});
	}
}
