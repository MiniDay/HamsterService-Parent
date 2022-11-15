package cn.hamster3.service.common.data;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class ServiceLocation {
    @NotNull
    private String serverName;
    @NotNull
    private String worldName;
    private double x;
    private double y;
    private double z;

    private float yaw;
    private float pitch;

    public ServiceLocation(@NotNull String serverName, @NotNull String worldName, double x, double y, double z) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ServiceLocation(@NotNull String serverName, @NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public ServiceLocation(@NotNull JsonObject object) {
        serverName = object.get("serverName").getAsString();
        worldName = object.get("worldName").getAsString();

        x = object.get("x").getAsDouble();
        y = object.get("y").getAsDouble();
        z = object.get("z").getAsDouble();

        yaw = object.get("yaw").getAsFloat();
        pitch = object.get("pitch").getAsFloat();
    }

    @NotNull
    public JsonObject saveToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("serverName", serverName);
        object.addProperty("worldName", worldName);

        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);

        object.addProperty("yaw", yaw);
        object.addProperty("pitch", pitch);
        return object;
    }

    @NotNull
    public String getServerName() {
        return serverName;
    }

    public void setServerName(@NotNull String serverName) {
        this.serverName = serverName;
    }

    @NotNull
    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(@NotNull String worldName) {
        this.worldName = worldName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getBlockX() {
        return (int) x;
    }

    public int getBlockY() {
        return (int) y;
    }

    public int getBlockZ() {
        return (int) z;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceLocation that = (ServiceLocation) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0 && Float.compare(that.yaw, yaw) == 0 && Float.compare(that.pitch, pitch) == 0 && serverName.equals(that.serverName) && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName, worldName, x, y, z, yaw, pitch);
    }
}
