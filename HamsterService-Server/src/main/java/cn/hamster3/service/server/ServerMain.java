package cn.hamster3.service.server;

import cn.hamster3.service.server.data.ServerConfig;
import cn.hamster3.service.server.handler.ServiceCentre;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
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
import java.util.Scanner;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger("main");
    private static NioEventLoopGroup loopGroup;
    private static boolean started;

    public static void main(String[] args) throws Exception {
        saveDefaultFile("logSettings.xml");
        File file = saveDefaultFile("server.yml");
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        ServerConfig config = new Yaml().loadAs(reader, ServerConfig.class);
        reader.close();
        logger.info("服务器绑定地址: {}", config.getServiceAddress());
        logger.info("服务器绑定端口: {}", config.getServicePort());
        logger.info("服务器线程池数: {}", config.getNioThread());
        logger.info("白名单IP列表: {}", config.getAcceptList());

        ServerBootstrap bootstrap = new ServerBootstrap();
        loopGroup = new NioEventLoopGroup(config.getNioThread());
        bootstrap
                .group(loopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ServiceCentre(config));
        bootstrap.bind(config.getServiceAddress(), config.getServicePort()).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("服务器已启动. 输入 stop 关闭服务器.");
            } else {
                logger.warn("服务器启动失败!");
                future.cause().printStackTrace();
            }
        });
        started = true;
        Scanner scanner = new Scanner(System.in);
        logger.info("命令执行器准备就绪.");
        while (started) {
            executeCommand(scanner.nextLine());
        }
    }

    public static void executeCommand(String command) throws Exception {
        if (command.equalsIgnoreCase("stop")) {
            logger.info("准备关闭服务器...");
            loopGroup.shutdownGracefully().await();
            logger.info("服务器已关闭!");
            started = false;
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
