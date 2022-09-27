package cn.hamster3.service.bukkit.data;

import cn.hamster3.service.bukkit.api.ServiceInfoAPI;
import cn.hamster3.service.common.data.ServiceLocation;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BukkitLocation extends ServiceLocation {

    public BukkitLocation(@NotNull String serverName, @NotNull String worldName, double x, double y, double z) {
        super(serverName, worldName, x, y, z);
    }

    public BukkitLocation(@NotNull String serverName, @NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        super(serverName, worldName, x, y, z, yaw, pitch);
    }

    public BukkitLocation(@NotNull JsonObject object) {
        super(object);
    }

    public BukkitLocation(@NotNull Entity player) {
        this(player.getLocation());
    }

    public BukkitLocation(@NotNull Block block) {
        this(block.getLocation());
    }

    @SuppressWarnings("ConstantConditions")
    public BukkitLocation(@NotNull Location location) {
        super(
                ServiceInfoAPI.getLocalServerName(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public BukkitLocation(@NotNull ServiceLocation location) {
        super(
                location.getServerName(),
                location.getWorldName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    @NotNull
    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(getWorldName()), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    @NotNull
    public BukkitBlockPos toServiceBlockPos() {
        return new BukkitBlockPos(getServerName(), getWorldName(), getBlockX(), getBlockY(), getBlockZ());
    }
}
