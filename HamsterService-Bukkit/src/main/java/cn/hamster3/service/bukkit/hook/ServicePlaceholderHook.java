package cn.hamster3.service.bukkit.hook;

import cn.hamster3.service.bukkit.HamsterServicePlugin;
import cn.hamster3.service.bukkit.api.ServiceInfoAPI;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ServicePlaceholderHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "HamsterService";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hamster3";
    }

    @Override
    public @NotNull String getVersion() {
        return HamsterServicePlugin.getInstance().getDescription().getVersion();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        ServiceSenderInfo localSenderInfo = ServiceInfoAPI.getLocalSenderInfo();
        switch (params) {
            case "server_name":
                return localSenderInfo.getName();
            case "server_nick_name":
                return localSenderInfo.getNickName();
        }
        ServicePlayerInfo info = ServiceInfoAPI.getPlayerInfo(player.getUniqueId());
        if (info == null) {
            return null;
        }
        switch (params) {
            case "bc_server_name":
                return info.getProxyServer();
            case "bc_server_nick_name":
                return ServiceInfoAPI.getSenderInfo(info.getProxyServer()).getNickName(); // 不可能为 null
        }
        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }
}
