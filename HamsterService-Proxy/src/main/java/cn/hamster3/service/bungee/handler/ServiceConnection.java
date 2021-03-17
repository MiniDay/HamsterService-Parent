package cn.hamster3.service.bungee.handler;

import cn.hamster3.service.bungee.HamsterServicePlugin;
import cn.hamster3.service.bungee.event.MessageSentEvent;
import cn.hamster3.service.bungee.event.ServiceConnectEvent;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import cn.hamster3.service.common.entity.ServiceSenderType;
import cn.hamster3.service.common.util.ServiceLogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;

public class ServiceConnection {
    // 当连接因异常断开时, 要发送的消息将会被存在这里, 等待服务器重新连接后再发送.
    private final ArrayList<ServiceMessageInfo> waitForSendMessages = new ArrayList<>();

    private final HamsterServicePlugin plugin;

    private final Bootstrap bootstrap;
    private final NioEventLoopGroup executors;

    private final ServiceSenderInfo selfInfo;
    private final String serviceHost;
    private final int servicePort;

    private boolean enable;
    private Channel channel;

    public ServiceConnection(HamsterServicePlugin plugin) {
        this.plugin = plugin;
        Configuration config = plugin.getConfig();
        selfInfo = new ServiceSenderInfo(
                ServiceSenderType.PROXY,
                config.getString("name"),
                config.getString("nickName")
        );
        serviceHost = config.getString("serviceHost");
        servicePort = config.getInt("servicePort");
        bootstrap = new Bootstrap();
        executors = new NioEventLoopGroup(config.getInt("nioThread", 2));

        bootstrap.group(executors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ServiceInitHandler(this));
    }

    public void start() {
        enable = true;
        connect(true);
    }

    /**
     * 关闭连接
     */
    public void close() {
        enable = false;
        if (channel != null) {
            channel.close();
            channel = null;
        }
        if (executors != null) {
            executors.shutdownGracefully().awaitUninterruptibly();
        }
    }

    /**
     * 连接到服务中心
     *
     * @param shutdown 连接失败时是否关闭服务器
     */
    public void connect(boolean shutdown) {
        if (!enable) {
            return;
        }
        ChannelFuture future = bootstrap.connect(serviceHost, servicePort);
        if (shutdown) {
            future = future.awaitUninterruptibly();
            if (future.isSuccess()) {
                channel = future.channel();
                register();
                ProxyServer.getInstance().getScheduler().runAsync(
                        plugin,
                        () -> ProxyServer.getInstance().getPluginManager().callEvent(new ServiceConnectEvent())
                );
            } else {
                Throwable cause = future.cause();
                ServiceLogUtils.error(cause, "连接至服务中心失败, 准备关闭服务器!");
                ProxyServer.getInstance().getScheduler().runAsync(
                        plugin,
                        () -> ProxyServer.getInstance().getPluginManager().callEvent(new ServiceConnectEvent(cause))
                );
                ProxyServer.getInstance().stop("连接至服务中心失败!");
            }
        } else {
            future.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    channel = f.channel();
                    register();
                    ProxyServer.getInstance().getPluginManager().callEvent(new ServiceConnectEvent());
                } else {
                    ServiceLogUtils.error(f.cause(), "连接至服务中心时出现了一个错误: ");
                    ProxyServer.getInstance().getPluginManager().callEvent(new ServiceConnectEvent(f.cause()));
                    ServiceLogUtils.info("准备重新连接...");
                    reconnect();
                }
            });
        }
    }

    /**
     * 重连
     */
    public void reconnect() {
        if (!enable) {
            return;
        }
        if (channel != null && channel.isOpen() && channel.isRegistered() && channel.isActive() && channel.isWritable()) {
            ServiceLogUtils.warning("通道可用, 无需重连!");
            return;
        }
        channel = null;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        connect(false);
    }

    /**
     * 发送注册信息
     */
    private void register() {
        ServiceMessageInfo content = new ServiceMessageInfo(
                selfInfo,
                "HamsterService",
                "register"
        );
        String string = content.saveToJson().toString();
        ServiceLogUtils.info("发送了注册信息: " + string);
        channel.writeAndFlush(string);
    }

    /**
     * 发送一条服务消息
     *
     * @param info  消息内容
     * @param block 是否阻塞（即必须等待消息发送完成，该方法才会返回）
     */
    public void sendMessage(ServiceMessageInfo info, boolean block) {
        if (channel == null) {
            synchronized (waitForSendMessages) {
                waitForSendMessages.add(info);
            }
            return;
        }
        ChannelFuture future = channel.writeAndFlush(info.toString());
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                ProxyServer.getInstance().getPluginManager().callEvent(new MessageSentEvent(info));
            } else {
                ProxyServer.getInstance().getPluginManager().callEvent(new MessageSentEvent(info, f.cause()));
            }
        });
        if (block) {
            future.awaitUninterruptibly();
        }
    }

    public ArrayList<ServiceMessageInfo> getWaitForSendMessages() {
        return waitForSendMessages;
    }

    public ServiceSenderInfo getInfo() {
        return selfInfo;
    }
}
