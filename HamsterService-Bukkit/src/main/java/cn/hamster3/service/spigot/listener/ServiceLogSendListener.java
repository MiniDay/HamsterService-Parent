package cn.hamster3.service.spigot.listener;

import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.spigot.event.MessageSentEvent;
import cn.hamster3.service.spigot.util.ServiceLogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServiceLogSendListener implements Listener {
    private final Gson gson;

    public ServiceLogSendListener() {
        gson = new GsonBuilder()
                .setLenient()// json宽松
                .enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
                .serializeNulls() //智能null
                .setPrettyPrinting()// 调教格式
                .create();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMessageSent(MessageSentEvent event) {
        ServiceMessageInfo messageInfo = event.getMessageInfo();
        ServiceLogUtils.info("[>>>>>] 发送了一条服务消息\n%s", gson.toJson(messageInfo.saveToJson()));
    }
}
