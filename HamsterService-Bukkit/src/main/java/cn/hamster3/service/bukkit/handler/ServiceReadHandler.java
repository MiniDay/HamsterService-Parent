package cn.hamster3.service.bukkit.handler;

import cn.hamster3.service.bukkit.event.MessageReceivedEvent;
import cn.hamster3.service.common.util.ServiceLogUtils;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bukkit.Bukkit;

@SuppressWarnings("deprecation")
public class ServiceReadHandler extends SimpleChannelInboundHandler<String> {
    private static final JsonParser parser = new JsonParser();
    private final ServiceConnection connection;

    public ServiceReadHandler(ServiceConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        try {
            MessageReceivedEvent event = new MessageReceivedEvent(new ServiceMessageInfo(parser.parse(msg).getAsJsonObject()));
            ServiceMessageInfo info = event.getMessageInfo();
            if ("HamsterService".equals(info.getTag())) {
                if (executeServiceMessage(info)) {
                    return;
                }
            }
            Bukkit.getPluginManager().callEvent(event);
        } catch (Exception e) {
            ServiceLogUtils.error(e, "从服务中心接受并处理消息时出现了一个错误: %s", msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ServiceLogUtils.warning("与服务中心断开了连接.");
        connection.reconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ServiceLogUtils.warning("与服务中心的通信中出现了一个错误: ");
        cause.printStackTrace();
    }

    private boolean executeServiceMessage(ServiceMessageInfo info) {
        switch (info.getAction()) {
            case "registerSuccess": {
                for (ServiceMessageInfo serviceMessageInfo : connection.getWaitForSendMessages()) {
                    connection.sendMessage(serviceMessageInfo, false);
                }
                connection.getWaitForSendMessages().clear();
                return false;
            }
            case "registerFailed": {
                ServiceLogUtils.warning("==============================");
                for (int i = 0; i < 3; i++) {
                    ServiceLogUtils.warning("服务注册失败: " + info.getContent().getAsString());
                }
                ServiceLogUtils.warning("服务器即将自动关闭......");
                ServiceLogUtils.warning("==============================");
                Bukkit.shutdown();
                return true;
            }
        }
        return false;
    }
}
