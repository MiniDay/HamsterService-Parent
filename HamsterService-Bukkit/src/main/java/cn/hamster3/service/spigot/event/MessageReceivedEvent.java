package cn.hamster3.service.spigot.event;

import cn.hamster3.service.common.entity.ServiceMessageInfo;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 从服务中心收到消息时产生的事件
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class MessageReceivedEvent extends MessageEvent {
    private static final HandlerList handlers = new HandlerList();

    public MessageReceivedEvent(ServiceMessageInfo info) {
        super(info);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}
