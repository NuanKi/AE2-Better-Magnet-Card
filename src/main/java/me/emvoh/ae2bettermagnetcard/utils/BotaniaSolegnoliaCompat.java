package me.emvoh.ae2bettermagnetcard.utils;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;

public final class BotaniaSolegnoliaCompat {
    private static final boolean BOTANIA_LOADED = Loader.isModLoaded("botania");
    private static final Method HAS_SOLEGNOLIA_AROUND = resolve();

    private BotaniaSolegnoliaCompat() {}

    private static Method resolve() {
        if (!BOTANIA_LOADED) return null;

        try {
            Class<?> c = Class.forName("vazkii.botania.common.block.subtile.functional.SubTileSolegnolia");
            return c.getMethod("hasSolegnoliaAround", Entity.class);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean hasSolegnoliaAround(Entity e) {
        if (HAS_SOLEGNOLIA_AROUND == null) return false;

        try {
            return (boolean) HAS_SOLEGNOLIA_AROUND.invoke(null, e);
        } catch (Throwable t) {
            return false;
        }
    }
}