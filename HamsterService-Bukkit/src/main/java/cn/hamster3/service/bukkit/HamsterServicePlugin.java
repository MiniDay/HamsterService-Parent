package cn.hamster3.service.bukkit;

import cn.hamster3.service.bukkit.api.ServiceInfoAPI;
import cn.hamster3.service.bukkit.api.ServiceMessageAPI;
import cn.hamster3.service.bukkit.handler.ServiceConnection;
import cn.hamster3.service.bukkit.listener.ServiceLogReceiveListener;
import cn.hamster3.service.bukkit.listener.ServiceLogSendListener;
import cn.hamster3.service.bukkit.listener.ServiceMainListener;
import cn.hamster3.service.common.util.ServiceLogUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class HamsterServicePlugin extends JavaPlugin {
    private static HamsterServicePlugin instance;
    private ServiceConnection connection;
    private ServiceInfoAPI serviceInfoAPI;

    public static HamsterServicePlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
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
