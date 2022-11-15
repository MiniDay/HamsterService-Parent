package cn.hamster3.service.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 服务连接事件
 */

@SuppressWarnings("unused")
public class ServiceConnectEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final boolean success;
    private final Throwable cause;

    public ServiceConnectEvent() {
        super(true);
        success = true;
        cause = null;
    }

    public ServiceConnectEvent(Throwable cause) {
        super(true);
        success = false;
        this.cause = cause;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}
