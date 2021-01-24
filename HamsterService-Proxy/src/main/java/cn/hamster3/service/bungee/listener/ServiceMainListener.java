package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import cn.hamster3.service.bungee.event.MessageReceivedEvent;
import cn.hamster3.service.bungee.event.ServiceConnectEvent;
import cn.hamster3.service.bungee.util.ProxyServiceUtils;
import cn.hamster3.service.bungee.util.ServiceLogUtils;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
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
        JsonElement content = info.getContent();
        switch (info.getAction()) {
            case "resetAllInfo": {
                JsonObject object = content.getAsJsonObject();
                HashSet<ServicePlayerInfo> playerInfo = new HashSet<>();
                for (JsonElement element : object.getAsJsonArray("playerInfo")) {
                    playerInfo.add(new ServicePlayerInfo(element.getAsJsonObject()));
                }
                serviceInfoAPI.resetAllPlayerInfo(playerInfo);
                HashSet<ServiceSenderInfo> senderInfo = new HashSet<>();
                for (JsonElement element : object.getAsJsonArray("senderInfo")) {
                    senderInfo.add(new ServiceSenderInfo(element.getAsJsonObject()));
                }
                serviceInfoAPI.resetAllServerInfo(senderInfo);
                break;
            }
            case "updatePlayerInfo": {
                serviceInfoAPI.loadPlayerInfo(new ServicePlayerInfo(content.getAsJsonObject()));
                break;
            }
            case "removePlayerInfo": {
                UUID uuid = UUID.fromString(content.getAsString());
                serviceInfoAPI.removePlayerInfo(uuid);
                break;
            }
            case "updateServerInfo": {
                serviceInfoAPI.loadServerInfo(new ServiceSenderInfo(content.getAsJsonObject()));
                break;
            }
            case "removeServerInfo": {
                serviceInfoAPI.removeSenderInfo(content.getAsString());
                break;
            }
            case "sendPlayerMessage": {
                JsonObject object = content.getAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player == null) {
                    return;
                }
                player.sendMessage(ProxyServiceUtils.parseComponentFromJson(object.get("message")));
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
