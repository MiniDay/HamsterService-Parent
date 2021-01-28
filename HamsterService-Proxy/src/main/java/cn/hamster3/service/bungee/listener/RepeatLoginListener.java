package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;

public class RepeatLoginListener implements Listener {
    private final boolean block;

    public RepeatLoginListener(boolean block) {
        this.block = block;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        if (ServiceInfoAPI.getPlayerInfo(uuid) != null) {
            if (block) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§c已经有一个相同名称的玩家在服务器中."));
            } else {
                ServiceMessageAPI.kickPlayer(uuid, "§c你的账户已在其他地方登录.");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPostLogin(PostLoginEvent event) {
        System.out.println(2);
    }
}
