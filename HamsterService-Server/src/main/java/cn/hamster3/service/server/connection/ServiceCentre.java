package cn.hamster3.service.server.connection;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import cn.hamster3.service.common.entity.ServiceSenderType;
import cn.hamster3.service.server.ServerMain;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.UUID;

/**
 * 服务中心
 * <p>
 * 管理与所有子服连接对象的类
 */
public class ServiceCentre extends ChannelInitializer<NioSocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger("ServiceCentre");

    private final HashSet<ServiceConnection> registeredHandlers;
    private final HashSet<ServicePlayerInfo> playerInfo;

    private final ServiceSenderInfo info;
    private final ServerMain.ServerConfig config;
    private boolean safeMode;

    public ServiceCentre(ServerMain.ServerConfig config) {
        this.config = config;

        info = new ServiceSenderInfo(
                ServiceSenderType.SERVICE_CENTRE,
                "ServiceCentre",
                "服务中心"
        );
        safeMode = false;
        registeredHandlers = new HashSet<>();
        playerInfo = new HashSet<>();
        logger.info("服务中心初始化完成.");
    }

    @Override
    protected void initChannel(NioSocketChannel channel) {
        logger.info("远程地址 {} 请求建立连接...", channel.remoteAddress().toString());
        channel.pipeline()
                .addLast(new LengthFieldPrepender(8))
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8))
                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                .addLast(new StringEncoder(StandardCharsets.UTF_8));

        String hostAddress = channel.remoteAddress().getAddress().getHostAddress();
        if (!config.getAcceptList().contains(hostAddress)) {
            ServiceMessageInfo messageInfo = new ServiceMessageInfo(
                    info,
                    "HamsterService",
                    "registerFailed",
                    new JsonPrimitive("ip不在白名单列表中!")
            );
            channel.writeAndFlush(messageInfo.saveToJson().toString());
            try {
                channel.disconnect().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.warn("{} 不在白名单列表中, 已断开连接!", hostAddress);
            return;
        }
        channel.pipeline().addLast(new ServiceConnection(this, channel));
    }

    public void registered(ServiceConnection handler) {
        synchronized (registeredHandlers) {
            registeredHandlers.add(handler);
        }
        logger.info("服务器 {} 已注册.", handler.getInfo().getName());

        broadcastServiceMessage(
                new ServiceMessageInfo(
                        info,
                        "HamsterService",
                        "updateServerInfo",
                        handler.getInfo().saveToJson()
                )
        );
        handler.sendServiceMessage("setSafeMode", new JsonPrimitive(safeMode));
    }

    public void closed(ServiceConnection handler) {
        ServiceSenderInfo info = handler.getInfo();
        if (info == null) {
            return;
        }
        broadcastServiceMessage(
                new ServiceMessageInfo(
                        this.info,
                        "HamsterService",
                        "removeServerInfo",
                        new JsonPrimitive(handler.getInfo().getName())
                )
        );

        synchronized (registeredHandlers) {
            registeredHandlers.remove(handler);
        }

        logger.info("与服务器 {} 的连接已关闭.", handler.getInfo().getName());
    }

    public ServiceSenderInfo getInfo() {
        return info;
    }

    public void broadcastMessage(ServiceMessageInfo messageInfo) {
        String s = messageInfo.saveToJson().toString();

        if (messageInfo.getToServer() != null) {
            ServiceConnection server = getServiceSenderByServerName(messageInfo.getToServer());
            if (server == null) {
                return;
            }
            server.getChannel().writeAndFlush(s);
            return;
        }

        synchronized (registeredHandlers) {
            for (ServiceConnection handler : registeredHandlers) {
                if (handler.isSubscribedTags(messageInfo.getTag())) {
                    handler.getChannel().writeAndFlush(s);
                }
            }
        }
    }

    public void broadcastServiceMessage(ServiceMessageInfo messageInfo) {
        String s = messageInfo.saveToJson().toString();

        if (messageInfo.getToServer() != null) {
            ServiceConnection server = getServiceSenderByServerName(messageInfo.getToServer());
            if (server == null) {
                return;
            }
            server.getChannel().writeAndFlush(s);
            return;
        }

        synchronized (registeredHandlers) {
            for (ServiceConnection handler : registeredHandlers) {
                handler.getChannel().writeAndFlush(s);
            }
        }
    }

    public ServiceConnection getServiceSenderByServerName(String serverName) {
        synchronized (registeredHandlers) {
            for (ServiceConnection sender : registeredHandlers) {
                if (serverName.equals(sender.getInfo().getName())) {
                    return sender;
                }
            }
        }
        return null;
    }

    public ServicePlayerInfo getPlayerInfo(UUID uuid) {
        synchronized (playerInfo) {
            for (ServicePlayerInfo playerInfo : playerInfo) {
                if (uuid.equals(playerInfo.getUuid())) {
                    return playerInfo;
                }
            }
        }
        return null;
    }

    public HashSet<ServiceConnection> getRegisteredHandlers() {
        return registeredHandlers;
    }

    public void updatePlayerInfo(ServicePlayerInfo info) {
        synchronized (playerInfo) {
            playerInfo.remove(info);
            playerInfo.add(info);
        }
    }

    public HashSet<ServicePlayerInfo> getAllPlayerInfo() {
        return playerInfo;
    }

    public boolean isSafeMode() {
        return safeMode;
    }

    public void setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
    }
}
