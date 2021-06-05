package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.event.MessageReceivedEvent;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.util.ServiceLogUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class SafeModeListener implements Listener {
    private final boolean kickAll;
    private final String message;
    private boolean safeMode;

    public SafeModeListener(boolean kickAll, String message) {
        this.kickAll = kickAll;
        this.message = message;
        safeMode = false;
    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {
        ServiceMessageInfo info = event.getMessageInfo();
        if (!"HamsterService".equals(info.getTag())) {
            return;
        }
        if ("setSafeMode".equals(info.getAction())) {
            safeMode = info.getContent().getAsBoolean();
            if (safeMode) {
                ServiceLogUtils.info("已开启安全模式.");
            } else {
                ServiceLogUtils.info("已关闭安全模式.");
            }
            if (kickAll && safeMode) {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    if (player.hasPermission("service.admin")) {
                        continue;
                    }
                    player.disconnect(new TextComponent(message));
                }
            }
        }
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        if (!safeMode) {
            return;
        }
        event.setCancelReason(new TextComponent(message));
        event.setCancelled(true);
    }
}
