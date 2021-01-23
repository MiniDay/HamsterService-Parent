package cn.hamster3.service.common.entity;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * 消息发送者信息
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class ServiceSenderInfo {
    private final ServiceSenderType type;
    private final String name;
    private final String nickName;

    private JsonObject jsonInfo;

    public ServiceSenderInfo(@NotNull ServiceSenderType type, @NotNull String name, @NotNull String nickName) {
        this.type = type;
        this.name = name;
        this.nickName = nickName;
    }

    public ServiceSenderInfo(@NotNull JsonObject object) {
        type = ServiceSenderType.valueOf(object.get("type").getAsString());
        name = object.get("name").getAsString();
        nickName = object.get("nickName").getAsString();
    }

    /**
     * 序列化至Json
     *
     * @return json对象
     */
    @NotNull
    public JsonObject saveToJson() {
        if (jsonInfo == null) {
            jsonInfo = new JsonObject();
            jsonInfo.addProperty("type", type.name());
            jsonInfo.addProperty("name", name);
            jsonInfo.addProperty("nickName", nickName);
        }
        return jsonInfo;
    }

    @NotNull
    public ServiceSenderType getType() {
        return type;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getNickName() {
        return nickName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceSenderInfo that = (ServiceSenderInfo) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return saveToJson().toString();
    }
}
