package cn.hamster3.service.spigot.api;

import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.spigot.handler.ServiceConnection;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

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

}
