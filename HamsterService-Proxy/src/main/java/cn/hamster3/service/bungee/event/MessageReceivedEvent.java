package cn.hamster3.service.bungee.event;

import cn.hamster3.service.common.entity.ServiceMessageInfo;

/**
 * 从服务中心收到消息时产生的事件
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class MessageReceivedEvent extends MessageEvent {

    public MessageReceivedEvent(ServiceMessageInfo info) {
        super(info);
    }
}
