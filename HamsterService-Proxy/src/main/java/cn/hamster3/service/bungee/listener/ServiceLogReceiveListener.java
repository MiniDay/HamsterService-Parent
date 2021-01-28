package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.event.MessageReceivedEvent;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.util.ServiceLogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageReceived(MessageReceivedEvent event) {
        ServiceMessageInfo messageInfo = event.getMessageInfo();
        ServiceLogUtils.info("[<<<<<] 收到了一条服务消息\n%s", gson.toJson(messageInfo.saveToJson()));
    }
}
