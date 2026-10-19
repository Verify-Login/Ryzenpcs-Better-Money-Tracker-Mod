package com.example.donutpay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Animated "X paid you" cards in the top right: slide in, shimmer, countdown bar, slide out. */
public final class PayNotifier {
    private PayNotifier() {}

    private static final long LIFE = 5000, SLIDE_IN = 380, SLIDE_OUT = 450;
    private static final int MAX_VISIBLE = 4;

    // Tiers (the card itself only shows above DonutPayHighlighter.THRESHOLD = 2.9M):
    //   2.9M - 25M  -> green "PAYMENT RECEIVED"
    //   25M  - 99M  -> gold  "MEDIUM PAYMENT"
    //   99M and up  -> magenta "MEGA PAYMENT"
    private static final double MEDIUM_MIN = 25_000_000;
    private static final double MEGA_MIN = 99_000_000;
    private static final int H = 38, MARGIN = 8, GAP = 6;
    private static final int BG = 0xE8101018;

    private record Note(String payer, String amount, double raw, long start) {}

    private static final List<Note> NOTES = new ArrayList<>();

    public static synchronized void push(String payer, double amount) {
        NOTES.add(new Note(payer, "+$" + LiveTracker.formatAmount(amount), amount, System.currentTimeMillis()));
        while (NOTES.size() > MAX_VISIBLE) NOTES.remove(0);
    }

    private static float easeOutCubic(float t) { float u = 1 - t; return 1 - u * u * u; }
    private static float easeInCubic(float t)  { return t * t * t; }

    public static synchronized void render(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        int sw = ctx.getScaledWindowWidth();
        int row = 0;

        Iterator<Note> it = NOTES.iterator();
        while (it.hasNext()) {
            Note n = it.next();
            long age = now - n.start();
            if (age >= LIFE) { it.remove(); continue; }

            int accent; String title;
            if (n.raw() >= MEGA_MIN) { accent = 0xFF55FF; title = "MEGA PAYMENT"; }
            else if (n.raw() >= MEDIUM_MIN) { accent = 0xFFD23C; title = "MEDIUM PAYMENT"; }
            else { accent = 0x55FF55; title = "PAYMENT RECEIVED"; }

            Text titleT = Text.literal(title).formatted(Formatting.BOLD);
            Text nameT = Text.literal(n.payer()).formatted(Formatting.BOLD);
            Text amtT = Text.literal(n.amount()).formatted(Formatting.BOLD);

            int w = Math.max(tr.getWidth(titleT) + 24, 14 + tr.getWidth(nameT) + 18 + tr.getWidth(amtT) + 10);
            w = Math.max(w, 170);

            // slide + fade
            float slide = 0, alpha = 1;
            if (age < SLIDE_IN) {
                slide = 1 - easeOutCubic(age / (float) SLIDE_IN);
            } else if (age > LIFE - SLIDE_OUT) {
                float t = (age - (LIFE - SLIDE_OUT)) / (float) SLIDE_OUT;
                slide = easeInCubic(t);
                alpha = 1 - t;
            }
            int x = sw - w - MARGIN + Math.round(slide * (w + MARGIN * 2));
            int y = MARGIN + row * (H + GAP);
            row++;

            // glow + border + body
            Draw.roundRect(ctx, x - 2, y - 2, w + 4, H + 4, Draw.withAlpha(accent, 0.12f * alpha));
            Draw.roundRect(ctx, x - 1, y - 1, w + 2, H + 2, Draw.withAlpha(accent, 0.70f * alpha));
            Draw.roundRect(ctx, x, y, w, H, Draw.withAlpha(0x101018, 0.92f * alpha));

            // left accent bar with a lighter strip
            ctx.fill(x + 1, y + 1, x + 4, y + H - 1, Draw.withAlpha(accent, alpha));
            ctx.fill(x + 1, y + 1, x + 2, y + H - 1, Draw.withAlpha(Draw.mix(accent, 0xFFFFFF, 0.5f), alpha));

            // shimmer sweeping along the top edge
            float p = ((now - n.start()) % 1500) / 1500f;
            int sx = x + Math.round(p * (w + 30)) - 30;
            int x0 = Math.max(x + 5, sx), x1 = Math.min(x + w - 1, sx + 30);
            if (x1 > x0) ctx.fill(x0, y, x1, y + 1, Draw.withAlpha(0xFFFFFF, 0.55f * alpha));

            // text
            int white = Draw.withAlpha(0xFFFFFF, alpha);
            ctx.drawTextWithShadow(tr, titleT, x + 11, y + 6, Draw.withAlpha(accent, alpha));
            ctx.drawTextWithShadow(tr, nameT, x + 11, y + 19, white);
            ctx.drawTextWithShadow(tr, amtT, x + w - 10 - tr.getWidth(amtT), y + 19, Draw.withAlpha(accent, alpha));

            // countdown bar
            float remaining = 1f - age / (float) LIFE;
            int barX = x + 11, barW = w - 21;
            ctx.fill(barX, y + H - 5, barX + barW, y + H - 3, Draw.withAlpha(0xFFFFFF, 0.10f * alpha));
            ctx.fill(barX, y + H - 5, barX + Math.round(barW * remaining), y + H - 3, Draw.withAlpha(accent, 0.9f * alpha));
        }
    }
}
