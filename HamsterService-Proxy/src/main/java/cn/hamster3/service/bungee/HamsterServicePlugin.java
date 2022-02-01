package cn.hamster3.service.bungee;

import cn.hamster3.service.bungee.api.ServiceInfoAPI;
import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import cn.hamster3.service.bungee.command.ServiceCommand;
import cn.hamster3.service.bungee.connection.ServiceConnection;
import cn.hamster3.service.bungee.listener.*;
import cn.hamster3.service.common.util.ServiceLogUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class HamsterServicePlugin extends Plugin implements Listener {
    private ServiceConnection connection;
    private ServiceInfoAPI serviceInfoAPI;
    private Configuration config;

    @Override
    public void onLoad() {
        ServiceLogUtils.setLogger(getLogger());
        saveDefaultConfig();
        connection = new ServiceConnection(this);
        serviceInfoAPI = new ServiceInfoAPI(connection);
        ServiceMessageAPI.init(connection);
    }

    @Override
    public void onEnable() {
        Configuration config = getConfig();
        if (config.getBoolean("logSend")) {
            ProxyServer.getInstance().getPluginManager().registerListener(this, new ServiceLogSendListener());
        }
        if (config.getBoolean("logReceive")) {
            ProxyServer.getInstance().getPluginManager().registerListener(this, new ServiceLogReceiveListener());
        }
        if (config.getBoolean("repeatLoginProtect.enable", true)) {
            boolean block = config.getBoolean("repeatLoginProtect.block", true);
            ProxyServer.getInstance().getPluginManager().registerListener(this, new RepeatLoginListener(block));
            ServiceLogUtils.info("已启用重复登录检测器. block = " + block);
        }
        if (config.getBoolean("repeatPlayerNameProtect.enable", true)) {
            String message = config.getString("repeatPlayerNameProtect.message", "§c该名称已被其他玩家占用!");
            ProxyServer.getInstance().getPluginManager().registerListener(this, new RepeatPlayerNameListener(message));
            ServiceLogUtils.info("已启用重复名称检测器.");
        }
        if (config.getBoolean("replaceOnlinePlayers", false)) {
            ProxyServer.getInstance().getPluginManager().registerListener(this, new ReplaceOnlinePlayersListener());
            ServiceLogUtils.info("已启用在线玩家数替换器.");
        }

        ProxyServer.getInstance().getPluginManager().registerListener(this, new SafeModeListener(
                config.getBoolean("safeMode.kickAll"),
                config.getString("safeMode.message")
        ));
        ServiceLogUtils.info("已启用安全模式监听器.");
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ServiceMainListener(serviceInfoAPI));
        connection.start();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, ServiceCommand.INSTANCE);
    }

    @Override
    public void onDisable() {
        connection.close();
    }

    public Configuration getConfig() {
        if (config != null) {
            return config;
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            config = saveDefaultConfig();
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
            ServiceLogUtils.warning("未找到配置文件, 准备创建默认配置文件...");
            config = saveDefaultConfig();
        }
        return config;
    }

    private Configuration saveDefaultConfig() {
        try {
            if (getDataFolder().mkdir()) {
                ServiceLogUtils.info("创建插件文件夹...");
            }
            File file = new File(getDataFolder(), "config.yml");
            InputStream in = getResourceAsStream("config.yml");
            Files.copy(in, file.toPath());
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (Exception ignored) {
        }
        return null;
    }
}
