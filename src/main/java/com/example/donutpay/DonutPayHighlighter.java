package com.example.donutpay;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutPayHighlighter implements ClientModInitializer {

    // ---------- Highlight settings ----------
    // Pays OVER this amount trigger the notification card and the orange text fade.
    // (The LIVE total still counts every pay.)
    public static final double THRESHOLD = 2_900_000;

    // Adjust these if DonutSMP's message formats differ.
    // Matches e.g. "Steve paid you $3.5M", "Steve paid you $3,500,000", "$500K"
    private static final Pattern PAY_PATTERN = Pattern.compile(
            "(?i)\\b(\\w{3,16})\\b\\s+(?:has\\s+)?paid\\s+you\\s+\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*([kmbt])?");

    // PLACEHOLDER: money you send. Matches e.g. "You paid Steve $2.5M"
    private static final Pattern SENT_PATTERN = Pattern.compile(
            "(?i)\\byou\\s+(?:have\\s+)?paid\\s+\\w{3,16}\\s+\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*([kmbt])?");

    // Gradient runs from START_COLOR (left) to END_COLOR (right).
    private static final int START_COLOR = 0x000000;
    private static final int END_COLOR = 0xFF8C00;

    @Override
    public void onInitializeClient() {
        // Read-only: inspects chat the client already received. Never sends anything.
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (overlay) return message; // ignore action bar

            String raw = message.getString();

            Matcher sent = SENT_PATTERN.matcher(raw);
            if (sent.find()) {
                LiveTracker.subtract(parseAmount(sent.group(1), sent.group(2)));
                return message;
            }

            Matcher m = PAY_PATTERN.matcher(raw);
            if (!m.find()) return message;

            String payer = m.group(1);
            double amount = parseAmount(m.group(2), m.group(3));
            LiveTracker.add(amount);

            if (amount <= THRESHOLD) return message;
            PayNotifier.push(payer, amount);
            return recolor(raw);
        });

        // LIVE total: boxed pill at the top middle.
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("ryzenpcsmoneytracker", "live"),
                (context, tickCounter) -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.options.hudHidden || mc.player == null) return;
                    TrackerHud.render(context, mc);
                });

        // Animated pay notifications: top right.
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("ryzenpcsmoneytracker", "paynotify"),
                (context, tickCounter) -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.options.hudHidden || mc.player == null) return;
                    PayNotifier.render(context, mc);
                });

        // /dpreset clears the LIVE total (client-side only, never sent to the server).
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("dpreset").executes(ctx -> {
                    LiveTracker.reset();
                    ctx.getSource().sendFeedback(Text.literal("LIVE total reset."));
                    return Command.SINGLE_SUCCESS;
                })));
    }

    private static double parseAmount(String number, String suffix) {
        double value;
        try {
            value = Double.parseDouble(number.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
        if (suffix == null) return value;
        return switch (suffix.toLowerCase()) {
            case "k" -> value * 1_000;
            case "m" -> value * 1_000_000;
            case "b" -> value * 1_000_000_000;
            case "t" -> value * 1_000_000_000_000.0;
            default -> value;
        };
    }

    // Fades each character from START_COLOR to END_COLOR across the message.
    private static Text recolor(String raw) {
        MutableText result = Text.empty();
        int n = raw.length();
        for (int i = 0; i < n; i++) {
            float t = n > 1 ? (float) i / (n - 1) : 1f;
            int rgb = lerp(START_COLOR, END_COLOR, t);
            result.append(Text.literal(String.valueOf(raw.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
        }
        return result;
    }

    private static int lerp(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
