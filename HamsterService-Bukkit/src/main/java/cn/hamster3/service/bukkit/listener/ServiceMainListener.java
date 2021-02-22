package cn.hamster3.service.bukkit.listener;

import cn.hamster3.service.bukkit.HamsterServicePlugin;
import cn.hamster3.service.bukkit.api.ServiceInfoAPI;
import cn.hamster3.service.bukkit.api.ServiceMessageAPI;
import cn.hamster3.service.bukkit.data.BukkitLocation;
import cn.hamster3.service.bukkit.event.MessageReceivedEvent;
import cn.hamster3.service.bukkit.event.ServiceConnectEvent;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import cn.hamster3.service.common.util.ServiceLogUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ServiceMainListener implements Listener {
    private final ServiceInfoAPI serviceInfoAPI;
    private final HashMap<UUID, Location> playerToLocations;

    public ServiceMainListener(ServiceInfoAPI serviceInfoAPI) {
        this.serviceInfoAPI = serviceInfoAPI;
        playerToLocations = new HashMap<>();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();


        UUID uuid = player.getUniqueId();
        Location location = playerToLocations.remove(uuid);
        if (location == null) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(
                HamsterServicePlugin.getInstance(),
                () -> player.teleport(location, PlayerTeleportEvent.TeleportCause.UNKNOWN),
                1
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onServiceConnect(ServiceConnectEvent event) {
        if (!event.isSuccess()) {
            return;
        }
        ServiceLogUtils.info("连接至服务中心成功...");
    }

    @EventHandler(ignoreCancelled = true)
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
            case "updatePlayerInfo": {
                serviceInfoAPI.loadPlayerInfo(new ServicePlayerInfo(info.getContentAsJsonObject()));
                break;
            }
            case "playerDisconnect": {
                UUID uuid = UUID.fromString(info.getContentAsString());
                ServicePlayerInfo playerInfo = ServiceInfoAPI.getPlayerInfo(uuid);
                if (playerInfo != null) {
                    playerInfo.setOnline(false);
                }
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
            case "bukkitConsoleCommand": {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), info.getContentAsString());
                break;
            }
            case "dispatchBukkitCommand": {
                JsonObject object = info.getContentAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    return;
                }
                Bukkit.dispatchCommand(player, object.get("command").getAsString());
                break;
            }
            case "sendPlayerToPlayer": {
                JsonObject object = info.getContentAsJsonObject();
                Player player = Bukkit.getPlayer(UUID.fromString(object.get("toPlayer").getAsString()));
                if (player != null) {
                    UUID uuid = UUID.fromString(object.get("sendPlayer").getAsString());
                    ServiceMessageAPI.sendPlayerToLocation(uuid, new BukkitLocation(player));
                }
                break;
            }
            case "sendPlayerToLocation": {
                JsonObject object = info.getContentAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                BukkitLocation location = new BukkitLocation(object.getAsJsonObject("location"));
                if (!ServiceInfoAPI.getLocalServerName().equals(location.getServerName())) {
                    return;
                }
                Player player = Bukkit.getPlayer(uuid);
                Location bukkitLocation = location.toBukkitLocation();
                if (player == null) {
                    playerToLocations.put(uuid, bukkitLocation);
                    return;
                }
                Bukkit.getScheduler().runTaskLater(
                        HamsterServicePlugin.getInstance(),
                        () -> player.teleport(bukkitLocation, PlayerTeleportEvent.TeleportCause.UNKNOWN),
                        1
                );
                break;
            }
        }
    }

}
