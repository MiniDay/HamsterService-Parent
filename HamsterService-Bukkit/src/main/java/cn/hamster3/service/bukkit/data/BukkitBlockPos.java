package cn.hamster3.service.bukkit.data;

import cn.hamster3.service.bukkit.api.ServiceInfoAPI;
import cn.hamster3.service.common.data.ServiceBlockPos;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BukkitBlockPos extends ServiceBlockPos {
    public BukkitBlockPos(@NotNull String serverName, @NotNull String worldName, int x, int y, int z) {
        super(serverName, worldName, x, y, z);
    }

    public BukkitBlockPos(@NotNull JsonObject object) {
        super(object);
    }

    public BukkitBlockPos(@NotNull Entity player) {
        this(player.getLocation());
    }

    public BukkitBlockPos(@NotNull Block block) {
        this(block.getLocation());
    }

    @SuppressWarnings("ConstantConditions")
    public BukkitBlockPos(@NotNull Location location) {
        super(
                ServiceInfoAPI.getLocalServerName(),
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public BukkitBlockPos(@NotNull ServiceBlockPos location) {
        super(
                ServiceInfoAPI.getLocalServerName(),
                location.getWorldName(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    @NotNull
    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(getWorldName()), getX(), getY(), getZ(), 0, 0);
    }

    @NotNull
    public BukkitLocation toServiceLocation() {
        return new BukkitLocation(getServerName(), getWorldName(), getX(), getY(), getZ());
    }
}
