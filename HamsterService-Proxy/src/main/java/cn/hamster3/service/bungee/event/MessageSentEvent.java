package cn.hamster3.service.bungee.event;

import cn.hamster3.service.common.entity.ServiceMessageInfo;

/**
 * 消息发送出去之后产生的事件
 */
@SuppressWarnings("unused")
public class MessageSentEvent extends MessageEvent {
    private final boolean success;
    private final Throwable cause;

    public MessageSentEvent(ServiceMessageInfo info) {
        super(info);
        success = true;
        cause = null;
    }

    public MessageSentEvent(ServiceMessageInfo messageInfo, Throwable cause) {
        super(messageInfo);
        success = false;
        this.cause = cause;
    }


    /**
     * 消息是否成功发送出去了
     *
     * @return true代表成功发送
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 若消息发送失败，则失败原因为何
     * 若发送成功，则返回null
     *
     * @return 失败原因
     */
    public Throwable getCause() {
        return cause;
    }
}
