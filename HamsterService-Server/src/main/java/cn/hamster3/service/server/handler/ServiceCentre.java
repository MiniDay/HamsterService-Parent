package cn.hamster3.service.server.handler;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import cn.hamster3.service.common.entity.ServiceSenderType;
import cn.hamster3.service.server.data.ServerConfig;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 服务中心
 */
public class ServiceCentre extends ChannelInitializer<NioSocketChannel> {
    private static final Logger logger = Logger.getLogger("ServiceCentre");

    private final HashSet<ServiceConnection> registeredHandlers;
    private final HashSet<ServicePlayerInfo> playerInfo;

    private final ServiceSenderInfo info;
    private final ServerConfig config;

    public ServiceCentre(ServerConfig config) {
        this.config = config;
        registeredHandlers = new HashSet<>();
        info = new ServiceSenderInfo(
                ServiceSenderType.SERVICE_CENTRE,
                "ServiceCentre",
                "服务中心"
        );
        playerInfo = new HashSet<>();
        logger.info("服务中心初始化完成.");
    }

    @Override
    protected void initChannel(NioSocketChannel channel) {
        logger.info("远程地址 " + channel.remoteAddress().toString() + " 请求建立连接...");
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
            logger.warning(hostAddress + " 不在白名单列表中, 已断开连接!");
            return;
        }
        channel.pipeline().addLast(new ServiceConnection(this, channel));
    }

    public void registered(ServiceConnection handler) {
        registeredHandlers.add(handler);
        logger.info("服务器 " + handler.getInfo().getName() + " 已注册!");

        broadcastServiceMessage(
                new ServiceMessageInfo(
                        info,
                        "HamsterService",
                        "updateServerInfo",
                        handler.getInfo().saveToJson()
                )
        );
    }

    public void closed(ServiceConnection handler) {
        registeredHandlers.remove(handler);
        logger.info("服务器 " + handler.getInfo().getName() + " 已断开链接!");

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
    }

    public ServiceSenderInfo getInfo() {
        return info;
    }

    public void broadcastMessage(ServiceMessageInfo messageInfo) {
        String s = messageInfo.saveToJson().toString();
        for (ServiceConnection handler : registeredHandlers) {
            if (handler.isSubscribedTags(messageInfo.getTag())) {
                handler.getChannel().writeAndFlush(s);
            }
        }
    }

    public void broadcastServiceMessage(ServiceMessageInfo messageInfo) {
        String s = messageInfo.saveToJson().toString();
        for (ServiceConnection handler : registeredHandlers) {
            handler.getChannel().writeAndFlush(s);
        }
    }

    public ServiceConnection getServiceSenderByServerName(String serverName) {
        for (ServiceConnection sender : registeredHandlers) {
            if (serverName.equals(sender.getInfo().getName())) {
                return sender;
            }
        }
        return null;
    }

    public ServicePlayerInfo getPlayerInfo(UUID uuid) {
        for (ServicePlayerInfo playerInfo : playerInfo) {
            if (uuid.equals(playerInfo.getUuid())) {
                return playerInfo;
            }
        }
        return null;
    }

    public HashSet<ServiceConnection> getRegisteredHandlers() {
        return registeredHandlers;
    }

    public HashSet<ServicePlayerInfo> getAllPlayerInfo() {
        return playerInfo;
    }
}
