package com.example.donutpay;

import net.minecraft.client.gui.DrawContext;

/** Small drawing helpers for the HUD. */
public final class Draw {
    private Draw() {}

    /** rgb = 0xRRGGBB, alpha 0..1 -> 0xAARRGGBB */
    public static int withAlpha(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** Filled rectangle with 1px chamfered corners (looks rounded at HUD scale). */
    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, int argb) {
        ctx.fill(x + 1, y, x + w - 1, y + h, argb);
        ctx.fill(x, y + 1, x + w, y + h - 1, argb);
    }
}
