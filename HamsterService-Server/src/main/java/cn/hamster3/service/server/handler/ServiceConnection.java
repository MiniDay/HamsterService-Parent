package cn.hamster3.service.server.handler;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import com.google.gson.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;


public class ServiceConnection extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger("ServiceConnection");

    private final NioSocketChannel channel;
    private final ServiceCentre centre;
    private final Gson gson;
    private final HashSet<String> subscribedTags;
    private ServiceSenderInfo info;

    public ServiceConnection(ServiceCentre centre, NioSocketChannel channel) {
        this.centre = centre;
        this.channel = channel;

        gson = new GsonBuilder()
                .setLenient()// json宽松
                .enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
                .serializeNulls() //智能null
                .setPrettyPrinting()// 调教格式
                .create();
        subscribedTags = new HashSet<>();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, String msg) {
        try {
            ServiceMessageInfo messageInfo = new ServiceMessageInfo(JsonParser.parseString(msg).getAsJsonObject());
            if (info != null) {
                logger.info("从服务器 {} 上收到一条消息:", info.getName());
                logger.info(gson.toJson(messageInfo));
            }
            if ("HamsterService".equals(messageInfo.getTag())) {
                executeServiceMessage(messageInfo);
                return;
            }
            if (info == null) {
                centre.closed(this);
                JsonObject object = new JsonObject();
                object.addProperty("action", "disconnect");
                object.addProperty("message", "请先注册后再使用消息服务!");
                ServiceMessageInfo notRegisterCloseMessage = new ServiceMessageInfo(
                        centre.getInfo(),
                        "HamsterService",
                        "disconnect",
                        object
                );
                channel.writeAndFlush(notRegisterCloseMessage.saveToJson().toString());
                channel.close();
                return;
            }
            centre.broadcastMessage(messageInfo);
        } catch (Exception e) {
            logger.warn("处理消息 {} 时出现错误: ", msg);
            e.printStackTrace();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        context.close();
        logger.warn("服务器 {} 断开了与我们的连接.", info.getName());
        centre.closed(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("与服务器 {} 通信时出现了一个错误: ", info.getName(), cause);
    }

    private void executeServiceMessage(ServiceMessageInfo messageInfo) {
        switch (messageInfo.getAction()) {
            case "register": {
                if (centre.getServiceSenderByServerName(messageInfo.getSenderInfo().getName()) != null) {
                    sendServiceMessage("registerFailed", new JsonPrimitive("已经有一个服务器使用了相同的name!"));
                    return;
                } else {
                    sendServiceMessage("registerSuccess", null);
                }
                info = messageInfo.getSenderInfo();
                centre.registered(this);
                break;
            }
            case "serverEnabled": {
                JsonArray playerInfoJson = new JsonArray();
                for (ServicePlayerInfo playerInfo : centre.getAllPlayerInfo()) {
                    playerInfoJson.add(playerInfo.saveToJson());
                }

                JsonArray senderInfoJson = new JsonArray();
                for (ServiceConnection connection : centre.getRegisteredHandlers()) {
                    senderInfoJson.add(connection.getInfo().saveToJson());
                }

                JsonObject response = new JsonObject();
                response.add("playerInfo", playerInfoJson);
                response.add("senderInfo", senderInfoJson);
                sendServiceMessage("resetAllInfo", response);
                break;
            }
            case "subscribeTag": {
                subscribedTags.add(messageInfo.getContent().getAsString());
                break;
            }
            case "unsubscribeTag": {
                subscribedTags.remove(messageInfo.getContent().getAsString());
                break;
            }
            case "updatePlayerInfo": {
                ServicePlayerInfo playerInfo = new ServicePlayerInfo(messageInfo.getContent().getAsJsonObject());
                centre.getAllPlayerInfo().remove(playerInfo);
                centre.getAllPlayerInfo().add(playerInfo);
                centre.broadcastServiceMessage(messageInfo);
                break;
            }
            case "removePlayerInfo": {
                UUID uuid = UUID.fromString(messageInfo.getContent().getAsString());
                ServicePlayerInfo info = centre.getPlayerInfo(uuid);
                centre.getAllPlayerInfo().remove(info);
                centre.broadcastServiceMessage(messageInfo);
                break;
            }
            default: {
                centre.broadcastServiceMessage(messageInfo);
                break;
            }
        }
    }

    private void sendServiceMessage(String action, JsonElement content) {
        ServiceMessageInfo messageInfo = new ServiceMessageInfo(
                centre.getInfo(),
                "HamsterService",
                action,
                content
        );
        channel.writeAndFlush(messageInfo.toString());
    }

    public NioSocketChannel getChannel() {
        return channel;
    }

    public ServiceSenderInfo getInfo() {
        return info;
    }

    public boolean isSubscribedTags(String tag) {
        return subscribedTags.contains(tag);
    }

}
