package cn.hamster3.service.bukkit.connection;

import cn.hamster3.service.bukkit.api.ServiceMessageAPI;
import cn.hamster3.service.bukkit.event.MessageReceivedEvent;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.util.ServiceLogUtils;
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
    public void channelInactive(ChannelHandlerContext context) {
        ServiceLogUtils.warning("与服务中心的连接已断开.");
        connection.reconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        ServiceLogUtils.warning("与服务中心通信时出现了一个错误: ");
        cause.printStackTrace();
    }

    private boolean executeServiceMessage(ServiceMessageInfo info) {
        switch (info.getAction()) {
            case "registerSuccess": {
                synchronized (connection.getWaitForSendMessages()) {
                    for (ServiceMessageInfo serviceMessageInfo : connection.getWaitForSendMessages()) {
                        connection.sendMessage(serviceMessageInfo, false);
                    }
                    connection.getWaitForSendMessages().clear();
                }
                for (String s : ServiceMessageAPI.SUBSCRIBED_TAG) {
                    ServiceMessageAPI.subscribeTag(s);
                }
                return false;
            }
            case "registerFailed": {
                ServiceLogUtils.warning("==============================");
                ServiceLogUtils.warning("服务注册失败: " + info.getContent().getAsString());
                ServiceLogUtils.warning("服务器即将自动关闭......");
                ServiceLogUtils.warning("==============================");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("由于 HamsterService 注册失败, 服务器被关闭了.")));
                Bukkit.shutdown();
                return true;
            }
        }
        return false;
    }
}
