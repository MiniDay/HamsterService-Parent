package cn.hamster3.service.server;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.server.command.CommandHandler;
import cn.hamster3.service.server.connection.ServiceCentre;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger("main");
    private static final File PLAYER_DATA_FOLDER = new File("playerData");

    public static void main(String[] args) {
        saveDefaultFile("logSettings.xml");

        ServerConfig config = loadConfig();
        if (config == null) {
            return;
        }

        logger.info("服务器绑定地址: {}", config.getServiceAddress());
        logger.info("服务器绑定端口: {}", config.getServicePort());
        logger.info("服务器线程池数: {}", config.getNioThread());
        logger.info("白名单IP列表: {}", config.getAcceptList());
        ServiceCentre centre = new ServiceCentre(config);

        loadPlayerData(centre);

        NioEventLoopGroup loopGroup = new NioEventLoopGroup(config.getNioThread());
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(loopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(centre);
        ChannelFuture channelFuture = bootstrap.bind(config.getServiceAddress(), config.getServicePort());
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("服务器已启动. 输入 stop 来关闭该程序.");
                logger.warn("请勿直接点 X 关闭! 否则无法保存玩家存档, 将导致许多插件功能异常!");
                logger.warn("请勿直接点 X 关闭! 否则无法保存玩家存档, 将导致许多插件功能异常!");
                logger.warn("请勿直接点 X 关闭! 否则无法保存玩家存档, 将导致许多插件功能异常!");
                logger.info("若要关闭该程序，请在本控制台使用 stop 命令。");
            } else {
                logger.error("服务器启动失败: {}", future.cause().toString());
                loopGroup.shutdownGracefully();
            }
        });

        new CommandHandler(loopGroup, centre, PLAYER_DATA_FOLDER).startScanConsole();
    }

    @SuppressWarnings("ConstantConditions")
    private static File saveDefaultFile(String name) {
        File file = new File(name);
        if (file.exists()) {
            return file;
        }
        try {
            Files.copy(ServerMain.class.getResourceAsStream("/" + name), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("在保存默认配置文件 {} 时遇到了一个错误: {}", name, e);
        }
        return file;
    }

    private static ServerConfig loadConfig() {
        File configFile = saveDefaultFile("server.yml");
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);
            ServerConfig config = new Yaml().loadAs(reader, ServerConfig.class);
            reader.close();
            return config;
        } catch (Exception e) {
            logger.error("加载配置文件时遇到了一个异常: ", e);
            return null;
        }
    }

    private static void loadPlayerData(ServiceCentre centre) {
        if (PLAYER_DATA_FOLDER.mkdirs()) {
            logger.info("创建玩家存档文件夹...");
        }

        logger.info("正在加载玩家存档...");
        File[] playerDataFiles = PLAYER_DATA_FOLDER.listFiles();
        if (playerDataFiles != null) {
            for (File dataFile : playerDataFiles) {
                try {
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);
                    JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                    reader.close();
                    ServicePlayerInfo playerInfo = new ServicePlayerInfo(object);
                    playerInfo.setOnline(false);
                    centre.getAllPlayerInfo().add(playerInfo);
                } catch (Exception e) {
                    logger.error("加载存档文件 " + dataFile.getName() + " 时遇到了一个异常: ", e);
                }
            }
        }
        logger.info("玩家存档加载完成.");
    }

    @SuppressWarnings("unused")
    public static class ServerConfig {
        private String serviceAddress;
        private int servicePort;
        private int nioThread;
        private List<String> acceptList;

        public String getServiceAddress() {
            return serviceAddress;
        }

        public void setServiceAddress(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        public int getServicePort() {
            return servicePort;
        }

        public void setServicePort(int servicePort) {
            this.servicePort = servicePort;
        }

        public int getNioThread() {
            return nioThread;
        }

        public void setNioThread(int nioThread) {
            this.nioThread = nioThread;
        }

        public List<String> getAcceptList() {
            return acceptList;
        }

        public void setAcceptList(List<String> acceptList) {
            this.acceptList = acceptList;
        }
    }
}
