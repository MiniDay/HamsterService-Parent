package cn.hamster3.service.bungee.listener;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import cn.hamster3.service.bungee.event.MessageReceivedEvent;
import cn.hamster3.service.bungee.event.ServiceConnectEvent;
import cn.hamster3.service.common.data.ServiceLocation;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import cn.hamster3.service.common.util.ComponentUtils;
import cn.hamster3.service.common.util.ServiceLogUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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
        JsonArray playerInfoArray = new JsonArray();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            ServicePlayerInfo playerInfo = new ServicePlayerInfo(
                    player.getUniqueId(),
                    player.getName(),
                    player.getServer().getInfo().getName(),
                    ServiceInfoAPI.getLocalServerName()
            );
            playerInfoArray.add(playerInfo.saveToJson());
        }
        ServiceMessageAPI.sendServiceMessage("HamsterService", "updatePlayerInfoArray", playerInfoArray);
    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {
        ServiceMessageInfo info = event.getMessageInfo();
        if (!"HamsterService".equals(info.getTag())) {
            return;
        }
        switch (info.getAction()) {
            case "resetAllInfo": {
                JsonObject object = info.getContentAsJsonObject();
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
            case "updatePlayerInfoArray": {
                JsonArray array = info.getContentAsJsonArray();
                for (JsonElement element : array) {
                    ServicePlayerInfo playerInfo = new ServicePlayerInfo(element.getAsJsonObject());
                    serviceInfoAPI.loadPlayerInfo(playerInfo);
                }
                break;
            }
            case "updatePlayerInfo":
            case "playerPostLogin":
            case "playerDisconnect": {
                serviceInfoAPI.loadPlayerInfo(new ServicePlayerInfo(info.getContentAsJsonObject()));
                break;
            }
            case "updateServerInfo": {
                serviceInfoAPI.loadServerInfo(new ServiceSenderInfo(info.getContentAsJsonObject()));
                break;
            }
            case "removeServerInfo": {
                serviceInfoAPI.removeSenderInfo(info.getContentAsString());
                break;
            }
            case "sendPlayerMessage": {
                JsonObject object = info.getContentAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player == null) {
                    return;
                }
                player.sendMessage(ComponentUtils.parseComponentFromJson(object.get("message")));
                break;
            }
            case "broadcastMessage": {
                ProxyServer.getInstance().broadcast(ComponentUtils.parseComponentFromJson(info.getContentAsJsonObject()));
                break;
            }
            case "proxyConsoleCommand": {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(
                        ProxyServer.getInstance().getConsole(),
                        info.getContentAsString()
                );
                break;
            }
            case "dispatchProxyCommand": {
                JsonObject object = info.getContentAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player == null) {
                    return;
                }
                ProxyServer.getInstance().getPluginManager().dispatchCommand(player, object.get("command").getAsString());
                break;
            }
            case "sendPlayerToLocation": {
                JsonObject object = info.getContent().getAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player == null) {
                    return;
                }
                ServiceLocation location = new ServiceLocation(object.getAsJsonObject("location"));
                if (location.getServerName().equals(player.getServer().getInfo().getName())) {
                    return;
                }
                ServiceSenderInfo senderInfo = ServiceInfoAPI.getSenderInfo(location.getServerName());
                if (senderInfo == null) {
                    return;
                }
                player.connect(ProxyServer.getInstance().getServerInfo(location.getServerName()));
                break;
            }
            case "kickPlayer": {
                JsonObject object = info.getContentAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player != null) {
                    player.disconnect(ComponentUtils.parseComponentFromJson(object.get("reason")));
                    return;
                }
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
                event.getServer().getInfo().getName(),
                ServiceInfoAPI.getLocalServerName()
        );
        ServiceMessageAPI.sendServiceMessage("HamsterService", "updatePlayerInfo", playerInfo.saveToJson());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServicePlayerInfo playerInfo = new ServicePlayerInfo(
                player.getUniqueId(),
                player.getName(),
                null,
                ServiceInfoAPI.getLocalServerName(),
                true
        );
        ServiceMessageAPI.sendServiceMessage("HamsterService", "playerPostLogin", playerInfo.saveToJson());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServicePlayerInfo playerInfo = new ServicePlayerInfo(
                player.getUniqueId(),
                player.getName(),
                null,
                ServiceInfoAPI.getLocalServerName(),
                false
        );
        ServiceMessageAPI.sendServiceMessage("HamsterService", "playerDisconnect", playerInfo.saveToJson());
    }

}
