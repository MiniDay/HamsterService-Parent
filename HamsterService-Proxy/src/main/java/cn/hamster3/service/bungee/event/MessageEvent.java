package cn.hamster3.service.bungee.event;

import cn.hamster3.service.common.entity.ServiceMessageInfo;
import net.md_5.bungee.api.plugin.Event;

/**
 * 服务消息事件的基类
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class MessageEvent extends Event {
    private final ServiceMessageInfo messageInfo;

    public MessageEvent(ServiceMessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    public ServiceMessageInfo getMessageInfo() {
        return messageInfo;
    }

}
