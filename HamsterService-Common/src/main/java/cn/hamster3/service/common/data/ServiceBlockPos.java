package cn.hamster3.service.common.data;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class ServiceBlockPos {
    @NotNull
    private String serverName;
    @NotNull
    private String worldName;
    private int x;
    private int y;
    private int z;

    public ServiceBlockPos(@NotNull String serverName, @NotNull String worldName, int x, int y, int z) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ServiceBlockPos(@NotNull JsonObject object) {
        serverName = object.get("serverName").getAsString();
        worldName = object.get("worldName").getAsString();

        x = object.get("x").getAsInt();
        y = object.get("y").getAsInt();
        z = object.get("z").getAsInt();
    }

    @NotNull
    public JsonObject saveToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("serverName", serverName);
        object.addProperty("worldName", worldName);

        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceBlockPos that = (ServiceBlockPos) o;
        return x == that.x && y == that.y && z == that.z && serverName.equals(that.serverName) && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName, worldName, x, y, z);
    }
}
