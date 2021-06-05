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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger("main");

    public static void main(String[] args) {
        saveDefaultFile("logSettings.xml");
        File file = saveDefaultFile("server.yml");
        ServerConfig config;
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            config = new Yaml().loadAs(reader, ServerConfig.class);
            reader.close();
        } catch (Exception e) {
            logger.error("加载配置文件时遇到了一个异常: ", e);
            return;
        }

        logger.info("服务器绑定地址: {}", config.getServiceAddress());
        logger.info("服务器绑定端口: {}", config.getServicePort());
        logger.info("服务器线程池数: {}", config.getNioThread());
        logger.info("白名单IP列表: {}", config.getAcceptList());
        ServiceCentre centre = new ServiceCentre(config);

        File playerDataFolder = new File("playerData");
        if (playerDataFolder.mkdirs()) {
            logger.info("创建玩家存档文件夹...");
        }

        logger.info("正在加载玩家存档...");
        File[] playerDataFiles = playerDataFolder.listFiles();
        if (playerDataFiles != null) {
            for (File dataFile : playerDataFiles) {
                try {
                    JsonObject object = JsonParser.parseReader(new FileReader(dataFile)).getAsJsonObject();
                    ServicePlayerInfo playerInfo = new ServicePlayerInfo(object);
                    playerInfo.setOnline(false);
                    centre.getAllPlayerInfo().add(playerInfo);
                } catch (Exception e) {
                    logger.error("在加载存档文件 " + file.getName() + " 时遇到了一个异常: ", e);
                }
            }
        }
        logger.info("玩家存档加载完成.");

        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup loopGroup = new NioEventLoopGroup(config.getNioThread());
        bootstrap
                .group(loopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(centre);
        ChannelFuture channelFuture = bootstrap.bind(config.getServiceAddress(), config.getServicePort());
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("服务器已启动.");
            } else {
                logger.error("服务器启动失败: {}", future.cause().toString());
                loopGroup.shutdownGracefully();
            }
        });

        new CommandHandler(loopGroup, centre, playerDataFolder).startScanConsole();
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
