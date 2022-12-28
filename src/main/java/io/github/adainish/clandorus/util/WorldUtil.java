package io.github.adainish.clandorus.util;

import com.pixelmonmod.pixelmon.api.util.helpers.ResourceLocationHelper;
import io.github.adainish.clandorus.Clandorus;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Optional;

public class WorldUtil {
    public static World getBasicWorld()
    {
        return Clandorus.getServer().getWorlds().iterator().next().getWorld();
    }
    public static RegistryKey<World> getDimension(String dimension) {
        return dimension.isEmpty() ? null : getDimension(ResourceLocationHelper.of(dimension));
    }

    public static RegistryKey<World> getDimension(ResourceLocation key) {
        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key);
    }

    public static Optional<ServerWorld> getWorld(RegistryKey<World> key) {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().getWorld(key));
    }

    public static Optional<ServerWorld> getWorld(String key) {
        return getWorld(getDimension(key));
    }
}
