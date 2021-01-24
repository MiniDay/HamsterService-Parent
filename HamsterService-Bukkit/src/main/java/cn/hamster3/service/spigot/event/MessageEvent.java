package cn.hamster3.service.spigot.event;

import cn.hamster3.service.common.entity.ServiceMessageInfo;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 服务消息事件的基类
 */
@SuppressWarnings("unused")
public class MessageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ServiceMessageInfo messageInfo;

    public MessageEvent(ServiceMessageInfo messageInfo) {
        super(true);
        this.messageInfo = messageInfo;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * 获取这次事件相关的消息信息
     *
     * @return 消息信息
     */
    public ServiceMessageInfo getMessageInfo() {
        return messageInfo;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}
