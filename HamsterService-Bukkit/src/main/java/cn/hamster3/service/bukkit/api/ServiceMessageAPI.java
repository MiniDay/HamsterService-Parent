package cn.hamster3.service.bukkit.api;

import cn.hamster3.service.bukkit.HamsterServicePlugin;
import cn.hamster3.service.bukkit.data.BukkitLocation;
import cn.hamster3.service.bukkit.handler.ServiceConnection;
import cn.hamster3.service.common.data.ServiceLocation;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * 服务消息API
 * <p>
 * 通过这个类的静态方法可以快速简单地向其他服务器发送消息
 * <p>
 * 请注意：该类无需实例化，仅调用静态方法即可
 * <p>
 * 由于与服务中心的连接仅在HamsterService的onEnable阶段才会启用
 * <p>
 * 因此任何在onLoad()里调用该类的方法都可能是无效的
 */
@SuppressWarnings("unused")
public abstract class ServiceMessageAPI {
    private static ServiceConnection connection;

    /**
     * 订阅某个标签的消息
     *
     * @param tag 标签
     */
    public static void subscribeTag(String tag) {
        sendMessage("HamsterService", "subscribeTag", tag);
    }

    /**
     * 取消订阅某个标签的消息
     *
     * @param tag 标签
     */
    public static void unsubscribeTag(String tag) {
        sendMessage("HamsterService", "unsubscribeTag", tag);
    }

    /**
     * 初始化ServiceAPI
     *
     * @param connection Service连接对象
     */
    public static void init(ServiceConnection connection) {
        if (ServiceMessageAPI.connection != null) {
            throw new IllegalStateException("不允许重复初始化 ServiceMessageAPI !");
        }
        ServiceMessageAPI.connection = connection;
    }

    /**
     * 发送一条消息
     *
     * @param tag    消息标签
     * @param action 执行动作
     */
    public static void sendMessage(String tag, String action) {
        sendMessage(new ServiceMessageInfo(connection.getInfo(), tag, action, null), false);
    }

    /**
     * 发送一条有附加参数的消息
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     */
    public static void sendMessage(String tag, String action, String content) {
        sendMessage(tag, action, new JsonPrimitive(content));
    }

    /**
     * 发送一条有附加参数的消息，使用 String.format() 替换附加参数
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     * @param args    替换参数
     * @see String#format(String, Object...)
     */
    public static void sendMessage(String tag, String action, String content, Object... args) {
        sendMessage(tag, action, new JsonPrimitive(String.format(content, args)));
    }

    /**
     * 发送一条有附加参数的消息
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     */
    public static void sendMessage(String tag, String action, JsonElement content) {
        sendMessage(
                new ServiceMessageInfo(
                        connection.getInfo(),
                        tag,
                        action,
                        content
                )
                , false
        );
    }

    /**
     * 自定义消息信息并发送
     *
     * @param info  消息内容
     * @param block 是否阻塞（即必须等待消息发送完成，该方法才会返回）
     */
    public static void sendMessage(ServiceMessageInfo info, boolean block) {
        connection.sendMessage(info, block);
    }

    /**
     * 给玩家发送一条消息
     *
     * @param uuid    玩家
     * @param message 消息
     */
    public static void sendPlayerMessage(UUID uuid, String message) {
        JsonObject object = new JsonObject();
        object.addProperty("text", message);
        sendPlayerMessage(uuid, object);
    }

    /**
     * 给玩家发送一条消息
     *
     * @param uuid    玩家
     * @param message 消息
     */
    public static void sendPlayerMessage(UUID uuid, JsonElement message) {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.add("message", message);
        sendMessage("HamsterService", "sendPlayerMessage", object);
    }

    /**
     * 给服务器的在线玩家广播一条消息
     *
     * @param message 消息
     * @since 2.1.0
     */
    public static void broadcastMessage(String message) {
        JsonObject object = new JsonObject();
        object.addProperty("text", message);
        broadcastMessage(object);
    }

    /**
     * 给服务器的在线玩家广播一条消息
     *
     * @param message 消息
     * @since 2.1.0
     */
    public static void broadcastMessage(JsonElement message) {
        sendMessage("HamsterService", "broadcastMessage", message);
    }

    /**
     * 把玩家传送到另一个玩家身边
     * <p>
     * 支持跨服传送
     *
     * @param sendPlayer 被传送的玩家
     * @param toPlayer   传送的目标玩家
     * @since 2.1.0
     */
    public static void sendPlayerToPlayer(UUID sendPlayer, UUID toPlayer) {
        Player player = Bukkit.getPlayer(toPlayer);
        if (player != null) {
            sendPlayerToLocation(sendPlayer, new BukkitLocation(player));
            return;
        }

        // 如果被传送玩家不在线
        if (ServiceInfoAPI.getPlayerInfo(sendPlayer) == null) {
            return;
        }
        // 如果目标玩家不在线
        if (ServiceInfoAPI.getPlayerInfo(toPlayer) == null) {
            return;
        }

        JsonObject object = new JsonObject();
        object.addProperty("sendPlayer", sendPlayer.toString());
        object.addProperty("toPlayer", toPlayer.toString());
        ServiceMessageAPI.sendMessage("HamsterService", "sendPlayerToPlayer", object);
    }

    /**
     * 把玩家传送到一个位置
     * <p>
     * 支持跨服传送
     *
     * @param uuid     玩家的uuid
     * @param location 坐标
     * @since 2.1.0
     */
    public static void sendPlayerToLocation(UUID uuid, ServiceLocation location) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && ServiceInfoAPI.getLocalServerName().equals(location.getServerName())) {
            Bukkit.getScheduler().runTaskLater(
                    HamsterServicePlugin.getInstance(),
                    () -> player.teleport(
                            new BukkitLocation(location).toBukkitLocation(),
                            PlayerTeleportEvent.TeleportCause.UNKNOWN
                    ),
                    1
            );
            return;
        }
        // 如果玩家不在线
        if (ServiceInfoAPI.getPlayerInfo(uuid) == null) {
            return;
        }
        // 如果目标服务器不在线
        if (ServiceInfoAPI.getSenderInfo(location.getServerName()) == null) {
            return;
        }
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.add("location", location.saveToJson());
        ServiceMessageAPI.sendMessage("HamsterService", "sendPlayerToLocation", object);
    }

}
