package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ReplaceOnlinePlayersListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing response = event.getResponse();
        response.getPlayers().setOnline(ServiceInfoAPI.getOnlinePlayers().size());
    }
}
