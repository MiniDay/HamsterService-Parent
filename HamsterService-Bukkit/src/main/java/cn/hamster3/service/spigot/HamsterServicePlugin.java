package cn.hamster3.service.spigot;

import cn.hamster3.service.spigot.api.ServiceInfoAPI;
import cn.hamster3.service.spigot.api.ServiceMessageAPI;
import cn.hamster3.service.spigot.handler.ServiceConnection;
import cn.hamster3.service.spigot.listener.ServiceLogReceiveListener;
import cn.hamster3.service.spigot.listener.ServiceLogSendListener;
import cn.hamster3.service.spigot.listener.ServiceMainListener;
import cn.hamster3.service.spigot.util.ServiceLogUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HamsterServicePlugin extends JavaPlugin {
    private ServiceConnection connection;
    private ServiceInfoAPI serviceInfoAPI;

    @Override
    public void onLoad() {
        ServiceLogUtils.setLogger(getLogger());
        saveDefaultConfig();
        reloadConfig();
        connection = new ServiceConnection(this);
        serviceInfoAPI = new ServiceInfoAPI(connection);
        ServiceMessageAPI.init(connection);
    }

    @Override
    public void onEnable() {
        connection.start();
        FileConfiguration config = getConfig();
        if (config.getBoolean("logSend")) {
            Bukkit.getPluginManager().registerEvents(new ServiceLogSendListener(), this);
        }
        if (config.getBoolean("logReceive")) {
            Bukkit.getPluginManager().registerEvents(new ServiceLogReceiveListener(), this);
        }
        Bukkit.getPluginManager().registerEvents(new ServiceMainListener(serviceInfoAPI), this);
    }

    @Override
    public void onDisable() {
        connection.close();
    }
}
