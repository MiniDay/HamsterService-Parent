package cn.hamster3.service.bukkit.listener;

import cn.hamster3.service.bukkit.event.MessageReceivedEvent;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.util.ComponentUtils;
import cn.hamster3.service.common.util.ServiceLogUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServiceLogReceiveListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMessageSent(MessageReceivedEvent event) {
        ServiceMessageInfo messageInfo = event.getMessageInfo();
        ServiceLogUtils.info("[<<<<<] 收到了一条服务消息\n%s", ComponentUtils.getGson().toJson(messageInfo.saveToJson()));
    }
}
