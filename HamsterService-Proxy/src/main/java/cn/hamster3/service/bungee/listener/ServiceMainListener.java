package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import cn.hamster3.service.bungee.event.MessageReceivedEvent;
import cn.hamster3.service.bungee.event.ServiceConnectEvent;
import cn.hamster3.service.bungee.util.ServiceLogUtils;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

public class ServiceMainListener implements Listener {
    private final ServiceInfoAPI serviceInfoAPI;

    public ServiceMainListener(ServiceInfoAPI serviceInfoAPI) {
        this.serviceInfoAPI = serviceInfoAPI;
    }

    @EventHandler
    public void onServiceConnect(ServiceConnectEvent event) {
        if (!event.isSuccess()) {
            return;
        }
        ServiceLogUtils.info("连接至服务中心成功...");
    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {
        ServiceMessageInfo info = event.getMessageInfo();
        if (!"HamsterService".equals(info.getTag())) {
            return;
        }
        switch (info.getAction()) {
            case "resetInfos": {
                JsonObject object = info.getContent().getAsJsonObject();
                HashSet<ServicePlayerInfo> playerInfos = new HashSet<>();
                for (JsonElement element : object.getAsJsonArray("playerInfos")) {
                    playerInfos.add(new ServicePlayerInfo(element.getAsJsonObject()));
                }
                serviceInfoAPI.resetAllPlayerInfo(playerInfos);
                HashSet<ServiceSenderInfo> senderInfos = new HashSet<>();
                for (JsonElement element : object.getAsJsonArray("senderInfos")) {
                    senderInfos.add(new ServiceSenderInfo(element.getAsJsonObject()));
                }
                serviceInfoAPI.resetAllServerInfo(senderInfos);
                break;
            }
            case "updatePlayerInfo": {
                serviceInfoAPI.loadPlayerInfo(new ServicePlayerInfo(info.getContent().getAsJsonObject()));
                break;
            }
            case "removePlayerInfo": {
                UUID uuid = UUID.fromString(info.getContent().getAsString());
                serviceInfoAPI.removePlayerInfo(uuid);
                break;
            }
            case "updateServerInfo": {
                serviceInfoAPI.loadServerInfo(new ServiceSenderInfo(info.getContent().getAsJsonObject()));
                break;
            }
            case "removeServerInfo": {
                serviceInfoAPI.removeSenderInfo(info.getContent().getAsString());
                break;
            }
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServicePlayerInfo playerInfo = new ServicePlayerInfo(
                player.getUniqueId(),
                player.getName(),
                event.getServer().getInfo().getName()
        );
        ServiceMessageAPI.sendMessage("HamsterService", "updatePlayerInfo", playerInfo.saveToJson());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServiceMessageAPI.sendMessage("HamsterService", "removePlayerInfo", player.getUniqueId().toString());
    }
}
