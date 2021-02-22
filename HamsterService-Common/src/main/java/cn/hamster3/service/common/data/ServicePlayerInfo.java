package cn.hamster3.service.common.data;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 玩家信息
 */
@SuppressWarnings("unused")
public class ServicePlayerInfo {
    /**
     * 玩家的uuid
     */
    private final UUID uuid;
    /**
     * 玩家的名称
     */
    private final String playerName;
    /**
     * 玩家是否在线
     */
    private boolean online;
    /**
     * 玩家所在的 bukkit 服务器名称
     */
    private final String bukkitServer;
    /**
     * 玩家所在的 代理 服务器名称
     */
    private final String proxyServer;

    public ServicePlayerInfo(@NotNull UUID uuid, @NotNull String playerName, @Nullable String bukkitServer, @Nullable String proxyServer) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.bukkitServer = bukkitServer;
        this.proxyServer = proxyServer;
        online = true;
    }

    public ServicePlayerInfo(JsonObject object) {
        uuid = UUID.fromString(object.get("uuid").getAsString());
        playerName = object.get("playerName").getAsString();
        online = object.get("online").getAsBoolean();
        bukkitServer = object.get("bukkitServer").getAsString();
        proxyServer = object.get("proxyServer").getAsString();
    }

    public JsonObject saveToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.addProperty("playerName", playerName);
        object.addProperty("online", online);
        object.addProperty("bukkitServer", bukkitServer);
        object.addProperty("proxyServer", proxyServer);
        return object;
    }

    /**
     * 获取玩家的UUID
     *
     * @return 玩家的UUID
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    /**
     * 获取玩家的名称
     *
     * @return 玩家名称
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * 获取玩家所在的子服
     *
     * @return 子服名称
     */
    public String getBukkitServer() {
        return bukkitServer;
    }

    /**
     * 获取玩家所在的代理节点
     *
     * @return 代理节点名称
     */
    public String getProxyServer() {
        return proxyServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServicePlayerInfo that = (ServicePlayerInfo) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return saveToJson().toString();
    }
}
