package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.event.MessageReceivedEvent;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.util.ComponentUtils;
import cn.hamster3.service.common.util.ServiceLogUtils;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ServiceLogReceiveListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageReceived(MessageReceivedEvent event) {
        ServiceMessageInfo messageInfo = event.getMessageInfo();
        ServiceLogUtils.info("[<<<<<] 收到了一条服务消息\n%s", ComponentUtils.getGson().toJson(messageInfo.saveToJson()));
    }
}
