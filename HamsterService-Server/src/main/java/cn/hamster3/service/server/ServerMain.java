package cn.hamster3.service.server;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.server.data.ServerConfig;
import cn.hamster3.service.server.handler.ServiceCentre;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
import java.util.ArrayList;
import java.util.Scanner;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger("main");

    private static File playerDataFolder = new File("playerData");

    private static NioEventLoopGroup loopGroup;
    private static ServiceCentre centre;

    private static boolean started;

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
        centre = new ServiceCentre(config);

        playerDataFolder = new File("playerData");
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
        loopGroup = new NioEventLoopGroup(config.getNioThread());
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

        started = true;
        Scanner scanner = new Scanner(System.in);
        logger.info("命令执行器准备就绪. 输入 help 查看命令帮助.");
        while (started) {
            String command = scanner.nextLine();
            try {
                executeCommand(command);
            } catch (Exception e) {
                logger.error("执行命令 " + command + " 时遇到了一个异常: ", e);
            }
        }
    }

    public static void executeCommand(String command) throws Exception {
        String[] args = command.split(" ");
        switch (args[0].toLowerCase()) {
            case "?":
            case "help": {
                logger.info("===============================================================");
                logger.info("help              - 查看帮助.");
                logger.info("stop              - 关闭服务中心.");
                logger.info("save              - 保存所有玩家数据.");
                logger.info("command [bukkit/proxy] [命令内容] - 让所有已连接的 Bukkit/Proxy 服务器以控制台身份执行命令.");
                logger.info("===============================================================");
                break;
            }
            case "end":
            case "stop": {
                started = false;
                logger.info("准备关闭服务器...");
                loopGroup.shutdownGracefully().await();
                logger.info("服务器已关闭!");

                logger.info("正在保存玩家存档...");
                synchronized (centre.getAllPlayerInfo()) {
                    for (ServicePlayerInfo playerInfo : new ArrayList<>(centre.getAllPlayerInfo())) {
                        try {
                            OutputStreamWriter writer = new OutputStreamWriter(
                                    new FileOutputStream(
                                            new File(playerDataFolder, playerInfo.getUuid() + ".json")
                                    ),
                                    StandardCharsets.UTF_8
                            );
                            writer.write(playerInfo.saveToJson().toString());
                            writer.close();
                        } catch (Exception e) {
                            logger.error("保存玩家 " + playerInfo.getUuid() + " 的存档时遇到了一个异常: ", e);
                        }
                    }
                }
                logger.info("玩家存档保存完毕.");
                break;
            }
            case "command": {
                if (args.length < 3) {
                    logger.info("command [bukkit/proxy] [命令内容]");
                    break;
                }
                String action;
                switch (args[1].toLowerCase()) {
                    case "bukkit": {
                        action = "bukkitConsoleCommand";
                        break;
                    }
                    case "proxy": {
                        action = "proxyConsoleCommand";
                        break;
                    }
                    default: {
                        logger.info("command [bukkit/proxy] [命令内容]");
                        return;
                    }
                }
                StringBuilder execCommand = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    execCommand.append(args[i]).append(" ");
                }
                centre.broadcastServiceMessage(new ServiceMessageInfo(
                        centre.getInfo(),
                        "HamsterService",
                        action,
                        new JsonPrimitive(execCommand.toString())
                ));
                logger.info("已广播命令执行信息.");
                break;
            }
            case "save": {
                logger.info("正在保存玩家存档...");
                synchronized (centre.getAllPlayerInfo()) {
                    for (ServicePlayerInfo playerInfo : centre.getAllPlayerInfo()) {
                        try {
                            OutputStreamWriter writer = new OutputStreamWriter(
                                    new FileOutputStream(
                                            new File(playerDataFolder, playerInfo.getUuid() + ".json")
                                    ),
                                    StandardCharsets.UTF_8
                            );
                            writer.write(playerInfo.saveToJson().toString());
                            writer.close();
                        } catch (Exception e) {
                            logger.error("保存玩家 " + playerInfo.getUuid() + " 的存档时遇到了一个异常: ", e);
                        }
                    }
                }
                logger.info("玩家存档保存完毕.");
                break;
            }
            default: {
                logger.info("未知指令. 请输入 help 查看帮助.");
                break;
            }
        }
    }

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
}
