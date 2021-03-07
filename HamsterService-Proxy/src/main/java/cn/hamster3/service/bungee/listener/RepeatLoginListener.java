package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class RepeatLoginListener implements Listener {
    private final boolean block;

    public RepeatLoginListener(boolean block) {
        this.block = block;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        ServicePlayerInfo info = ServiceInfoAPI.getPlayerInfo(event.getConnection().getName());
        if (info != null && info.isOnline()) {
            if (block) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§c已经有一个相同名称的玩家在服务器中."));
            } else {
                ServiceMessageAPI.kickPlayer(info.getUuid(), "§c你的账户已在其他地方登录.");
            }
        }
    }
}
