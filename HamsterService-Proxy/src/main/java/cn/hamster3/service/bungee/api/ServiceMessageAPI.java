package cn.hamster3.service.bungee.api;

import cn.hamster3.service.bungee.connection.ServiceConnection;
import cn.hamster3.service.common.data.ServiceLocation;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.util.ComponentUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
        sendServiceMessage("HamsterService", "subscribeTag", tag);
    }

    /**
     * 取消订阅某个标签的消息
     *
     * @param tag 标签
     */
    public static void unsubscribeTag(String tag) {
        sendServiceMessage("HamsterService", "unsubscribeTag", tag);
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
     * 发送一条服务消息
     *
     * @param tag    消息标签
     * @param action 执行动作
     */
    public static void sendServiceMessage(String tag, String action) {
        sendServiceMessage(new ServiceMessageInfo(connection.getInfo(), tag, action, null), false);
    }

    /**
     * 发送一条有附加参数的服务消息
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     */
    public static void sendServiceMessage(String tag, String action, String content) {
        sendServiceMessage(tag, action, new JsonPrimitive(content));
    }

    /**
     * 发送一条有附加参数的服务消息，使用 String.format() 替换附加参数
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     * @param args    替换参数
     * @see String#format(String, Object...)
     */
    public static void sendServiceMessage(String tag, String action, String content, Object... args) {
        sendServiceMessage(tag, action, new JsonPrimitive(String.format(content, args)));
    }

    /**
     * 发送一条有附加参数的消息
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     */
    public static void sendServiceMessage(String tag, String action, JsonElement content) {
        sendServiceMessage(
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
     * 自定义服务消息信息并发送
     *
     * @param info  消息内容
     * @param block 是否阻塞（即必须等待消息发送完成，该方法才会返回）
     */
    public static void sendServiceMessage(ServiceMessageInfo info, boolean block) {
        connection.sendMessage(info, block);
    }

    /**
     * 强制玩家执行一个 bukkit 命令
     *
     * @param uuid    玩家的uuid
     * @param command 命令内容
     */
    public static void dispatchBukkitCommand(UUID uuid, String command) {
        sendServiceMessage("HamsterService", "dispatchBukkitCommand", command);
    }

    /**
     * 强制玩家执行一个代理端（指BungeeCord等）命令
     *
     * @param uuid    玩家的uuid
     * @param command 命令内容
     */
    public static void dispatchProxyCommand(UUID uuid, String command) {
        sendServiceMessage("HamsterService", "dispatchProxyCommand", command);
    }

    /**
     * 给玩家发送一条消息
     *
     * @param uuid    玩家
     * @param message 消息
     */
    public static void sendPlayerMessage(UUID uuid, String message) {
        sendPlayerMessage(uuid, new JsonPrimitive(message));
    }

    /**
     * 给玩家发送一条消息
     *
     * @param uuid    玩家
     * @param message 消息
     */
    public static void sendPlayerMessage(UUID uuid, JsonElement message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(ComponentUtils.parseComponentFromJson(message));
            return;
        }
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.add("message", message);
        sendServiceMessage("HamsterService", "sendPlayerMessage", object);
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
        sendServiceMessage("HamsterService", "broadcastMessage", message);
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
        ServicePlayerInfo sendPlayerInfo = ServiceInfoAPI.getPlayerInfo(sendPlayer);
        // 如果被传送玩家不在线
        if (sendPlayerInfo == null || !sendPlayerInfo.isOnline()) {
            return;
        }

        ServicePlayerInfo toPlayerInfo = ServiceInfoAPI.getPlayerInfo(toPlayer);
        // 如果目标玩家不在线
        if (toPlayerInfo == null || !toPlayerInfo.isOnline()) {
            return;
        }

        JsonObject object = new JsonObject();
        object.addProperty("sendPlayer", sendPlayer.toString());
        object.addProperty("toPlayer", toPlayer.toString());
        sendServiceMessage("HamsterService", "sendPlayerToPlayer", object);
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
        ServicePlayerInfo playerInfo = ServiceInfoAPI.getPlayerInfo(uuid);

        // 如果玩家不在线
        if (playerInfo == null || !playerInfo.isOnline()) {
            return;
        }
        // 如果目标服务器不在线
        if (ServiceInfoAPI.getSenderInfo(location.getServerName()) == null) {
            return;
        }

        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.add("location", location.saveToJson());
        sendServiceMessage("HamsterService", "sendPlayerToLocation", object);
    }

    public static void kickPlayer(UUID uuid, String reason) {
        kickPlayer(uuid, new JsonPrimitive(reason));
    }

    public static void kickPlayer(UUID uuid, JsonElement reason) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if (player != null) {
            player.disconnect(ComponentUtils.parseComponentFromJson(reason));
            return;
        }
        JsonObject object = new JsonObject();
        object.addProperty("uuid", uuid.toString());
        object.add("reason", reason);
        sendServiceMessage("HamsterService", "kickPlayer", object);
    }

    /**
     * 开启/关闭 安全模式
     * <p>
     * 在安全模式开启时玩家将无法连接至服务器
     * <p>
     * 且根据 config 的配置不同，有可能会踢出全部在线玩家
     *
     * @param enable 是否启用
     */
    public static void setSafeMode(boolean enable) {
        sendServiceMessage("HamsterService", "setSafeMode", new JsonPrimitive("enable"));
    }

    /**
     * 发送一条服务消息
     *
     * @param tag    消息标签
     * @param action 执行动作
     * @deprecated 你应该使用 sendServiceMessage
     * <p>
     * 因为这个方法名有歧义
     */
    @Deprecated
    public static void sendMessage(String tag, String action) {
        sendServiceMessage(tag, action);
    }

    /**
     * 发送一条有附加参数的服务消息
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     * @deprecated 你应该使用 sendServiceMessage
     * <p>
     * 因为这个方法名有歧义
     */
    @Deprecated
    public static void sendMessage(String tag, String action, String content) {
        sendServiceMessage(tag, action, content);
    }

    /**
     * 发送一条有附加参数的服务消息，使用 String.format() 替换附加参数
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     * @param args    替换参数
     * @see String#format(String, Object...)
     * @deprecated 你应该使用 sendServiceMessage
     * <p>
     * 因为这个方法名有歧义
     */
    @Deprecated
    public static void sendMessage(String tag, String action, String content, Object... args) {
        sendServiceMessage(tag, action, content, args);
    }

    /**
     * 发送一条有附加参数的服务消息
     *
     * @param tag     消息标签
     * @param action  执行动作
     * @param content 附加参数
     * @deprecated 你应该使用 sendServiceMessage
     * <p>
     * 因为这个方法名有歧义
     */
    @Deprecated
    public static void sendMessage(String tag, String action, JsonElement content) {
        sendServiceMessage(tag, action, content);
    }

    /**
     * 自定义服务消息信息并发送
     *
     * @param info  消息内容
     * @param block 是否阻塞（即必须等待消息发送完成，该方法才会返回）
     * @deprecated 你应该使用 sendServiceMessage
     * <p>
     * 因为这个方法名有歧义
     */
    @Deprecated
    public static void sendMessage(ServiceMessageInfo info, boolean block) {
        connection.sendMessage(info, block);
    }

}
