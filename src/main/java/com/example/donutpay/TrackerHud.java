package com.example.donutpay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** The LIVE session total: a boxed, color-coded pill at the top middle of the screen. */
public final class TrackerHud {
    private TrackerHud() {}

    private static final int BG = 0xE0101018;

    public static void render(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        double net = LiveTracker.get();

        int accent = net < 0 ? 0xFF5555 : net > 0 ? 0x55FF55 : 0xAAAAAA;
        String arrow = net < 0 ? "\u25BC " : net > 0 ? "\u25B2 " : "";
        Text number = Text.literal(arrow + LiveTracker.format(net)).formatted(Formatting.BOLD);
        Text label = Text.literal("LIVE").formatted(Formatting.BOLD);

        int numW = tr.getWidth(number);
        int labelW = tr.getWidth(label);
        int padX = 9, dot = 5, gap = 7;
        int w = padX + dot + 5 + labelW + gap + 1 + gap + numW + padX;
        int h = 20;
        int x = (ctx.getScaledWindowWidth() - w) / 2;
        int y = 6;

        float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 450.0);

        // soft glow, border, then dark body
        Draw.roundRect(ctx, x - 2, y - 2, w + 4, h + 4, Draw.withAlpha(accent, 0.10f + 0.10f * pulse));
        Draw.roundRect(ctx, x - 1, y - 1, w + 2, h + 2, Draw.withAlpha(accent, 0.55f + 0.25f * pulse));
        Draw.roundRect(ctx, x, y, w, h, BG);
        // subtle top highlight
        ctx.fill(x + 2, y + 1, x + w - 2, y + 2, Draw.withAlpha(0xFFFFFF, 0.07f));

        int cy = y + h / 2;
        int ty = y + (h - tr.fontHeight) / 2 + 1;

        // pulsing "live" dot
        int dx = x + padX;
        ctx.fill(dx - 1, cy - 3, dx + dot + 1, cy + 3, Draw.withAlpha(0x55FF55, 0.18f + 0.2f * pulse));
        ctx.fill(dx, cy - 2, dx + dot, cy + 3, Draw.withAlpha(0x55FF55, 0.65f + 0.35f * pulse));

        ctx.drawTextWithShadow(tr, label, dx + dot + 5, ty, 0xFF55FF55);

        // divider
        int divX = dx + dot + 5 + labelW + gap;
        ctx.fill(divX, y + 4, divX + 1, y + h - 4, Draw.withAlpha(0xFFFFFF, 0.18f));

        ctx.drawTextWithShadow(tr, number, divX + 1 + gap, ty, 0xFF000000 | accent);
    }
}
