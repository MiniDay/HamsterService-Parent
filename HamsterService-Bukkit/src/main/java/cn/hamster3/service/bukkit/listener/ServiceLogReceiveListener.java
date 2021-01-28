package cn.hamster3.service.bukkit.listener;

import cn.hamster3.service.bukkit.event.MessageReceivedEvent;
import cn.hamster3.service.common.util.ServiceLogUtils;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServiceLogReceiveListener implements Listener {
    private final Gson gson;

    public ServiceLogReceiveListener() {
        gson = new GsonBuilder()
                .setLenient()// json宽松
                .enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
                .serializeNulls() //智能null
                .setPrettyPrinting()// 调教格式
                .create();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMessageSent(MessageReceivedEvent event) {
        ServiceMessageInfo messageInfo = event.getMessageInfo();
        ServiceLogUtils.info("[<<<<<] 收到了一条服务消息\n%s", gson.toJson(messageInfo.saveToJson()));
    }
}
