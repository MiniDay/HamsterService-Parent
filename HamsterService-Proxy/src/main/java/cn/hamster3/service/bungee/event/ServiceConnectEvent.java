package cn.hamster3.service.bungee.event;


import net.md_5.bungee.api.plugin.Event;

/**
 * 服务连接事件
 */

@SuppressWarnings({"unused", "RedundantSuppression"})
public class ServiceConnectEvent extends Event {
    private final boolean success;
    private final Throwable cause;

    public ServiceConnectEvent() {
        success = true;
        cause = null;
    }

    public ServiceConnectEvent(Throwable cause) {
        success = true;
        this.cause = cause;
    }


    /**
     * 是否成功连接到服务中心
     *
     * @return true代表成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 如果连接失败了，则返回失败原因
     * <p>
     * 如果连接成功了，则返回null
     *
     * @return 失败原因
     */
    public Throwable getCause() {
        return cause;
    }
}
