package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class RepeatPlayerNameListener implements Listener {
    private final String message;

    public RepeatPlayerNameListener(String message) {
        this.message = message;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String name = event.getConnection().getName();
        ServicePlayerInfo info = ServiceInfoAPI.getPlayerInfo(name);
        if (info == null) {
            return;
        }
        if (info.getPlayerName().equals(name)) {
            return;
        }
        event.setCancelled(true);
        event.setCancelReason(new TextComponent(message));
    }
}
