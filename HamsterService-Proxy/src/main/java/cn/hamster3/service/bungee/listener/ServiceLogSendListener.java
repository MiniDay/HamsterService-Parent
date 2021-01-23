package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.event.MessageSentEvent;
import cn.hamster3.service.bungee.util.ServiceLogUtils;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSent(MessageSentEvent event) {
        ServiceMessageInfo messageInfo = event.getMessageInfo();
        ServiceLogUtils.info("[>>>>>] 发送了一条服务消息\n%s", gson.toJson(messageInfo.saveToJson()));
    }
}
