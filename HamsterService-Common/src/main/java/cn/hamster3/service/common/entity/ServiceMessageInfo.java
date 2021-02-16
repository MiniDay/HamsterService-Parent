package cn.hamster3.service.common.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 消息内容
 */
@SuppressWarnings("unused")
public class ServiceMessageInfo {
    /**
     * 消息发送者
     */
    private final ServiceSenderInfo senderInfo;
    /**
     * 接受该消息的目标服务器
     * <p>
     * 一旦设定该值，则此条消息将会由 HamsterService-Server 过滤
     * <p>
     * 仅服务器名称匹配的子端才能接收到这条消息
     * <p>
     * 若不设定（值为null），则该消息会广播给所有子端
     */
    private final String toServer;
    /**
     * 消息标签
     * <p>
     * 一般用这个来判断消息由哪个插件发出
     */
    private String tag;
    /**
     * 消息动作
     * <p>
     * 一般用这个来判断插件应该如何处理这条消息
     */
    private String action;
    /**
     * 消息内容
     * <p>
     * 这里是消息的附加参数
     */
    private JsonElement content;

    public ServiceMessageInfo(@NotNull ServiceSenderInfo senderInfo, @NotNull String tag, @NotNull String action) {
        this(senderInfo, tag, action, null);
    }

    public ServiceMessageInfo(@NotNull ServiceSenderInfo senderInfo, @NotNull String tag, @NotNull String action, @Nullable JsonElement content) {
        this(senderInfo, null, tag, action, content);
    }

    public ServiceMessageInfo(@NotNull ServiceSenderInfo senderInfo, @Nullable String toServer, @NotNull String tag, @NotNull String action, @Nullable JsonElement content) {
        this.senderInfo = senderInfo;
        this.toServer = toServer;
        this.tag = tag;
        this.action = action;
        this.content = content;
    }

    public ServiceMessageInfo(@NotNull JsonObject object) {
        senderInfo = new ServiceSenderInfo(object.getAsJsonObject("senderInfo"));
        if (object.has("toServer")) {
            toServer = object.get("toServer").getAsString();
        } else {
            toServer = null;
        }
        tag = object.get("tag").getAsString();
        action = object.get("action").getAsString();
        content = object.get("content");
    }

    /**
     * 序列化至Json
     *
     * @return json对象
     */
    @NotNull
    public JsonObject saveToJson() {
        JsonObject object = new JsonObject();
        object.add("senderInfo", senderInfo.saveToJson());
        if (toServer != null) {
            object.addProperty("toServer", toServer);
        }
        object.addProperty("tag", tag);
        object.addProperty("action", action);
        object.add("content", content);
        return object;
    }

    /**
     * 获取消息发送者
     *
     * @return 发送者
     */
    @NotNull
    public ServiceSenderInfo getSenderInfo() {
        return senderInfo;
    }

    /**
     * 获取定向发送的接受者
     * <p>
     * 如果返回为 null 则代表广播消息
     *
     * @return 定向发送的接受者
     */
    public String getToServer() {
        return toServer;
    }

    /**
     * 获取消息标签
     *
     * @return 消息标签
     */
    @NotNull
    public String getTag() {
        return tag;
    }

    /**
     * 设置消息标签
     *
     * @param tag 消息标签
     */
    public void setTag(@NotNull String tag) {
        this.tag = tag;
    }

    /**
     * 获取消息动作
     *
     * @return 消息动作
     */
    @NotNull
    public String getAction() {
        return action;
    }

    /**
     * 设置消息动作
     *
     * @param action 消息动作
     */
    public void setAction(@NotNull String action) {
        this.action = action;
    }

    /**
     * 获取消息内容
     *
     * @return 消息内容
     */
    public JsonElement getContent() {
        return content;
    }

    /**
     * 设置消息内容
     *
     * @param content 消息内容
     */
    public void setContent(@Nullable JsonElement content) {
        this.content = content;
    }

    /**
     * 以字符串形式获取消息内容
     *
     * @return 消息内容
     */
    public String getContentAsString() {
        return content.getAsString();
    }

    /**
     * 以 JsonObject 对象获取消息内容
     *
     * @return 消息内容
     */
    public JsonObject getContentAsJsonObject() {
        return content.getAsJsonObject();
    }

    /**
     * 以 JsonArray 对象获取消息内容
     *
     * @return 消息内容
     */
    public JsonArray getContentAsJsonArray() {
        return content.getAsJsonArray();
    }

    @Override
    public String toString() {
        return saveToJson().toString();
    }
}
