package cn.hamster3.service.server;

import cn.hamster3.service.server.data.ServerConfig;
import cn.hamster3.service.server.handler.ServiceCentre;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.logging.Logger;

public class ServerMain {
    private static final Logger logger = Logger.getLogger("ServerMain");
    private static NioEventLoopGroup loopGroup;
    private static boolean started;

    public static void main(String[] args) throws Exception {
        File file = new File("server.yml");
        if (!file.exists()) {
            Files.copy(
                    ServerMain.class.getResourceAsStream("/server.yml"),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            logger.info("生成默认服务器设置...");
        }
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        ServerConfig config = new Yaml().loadAs(reader, ServerConfig.class);
        reader.close();
        logger.info("服务器绑定地址: " + config.getServiceAddress());
        logger.info("服务器绑定端口: " + config.getServicePort());
        logger.info("服务器线程池数: " + config.getNioThread());
        logger.info("白名单IP列表: " + config.getAcceptList());

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
                logger.info("服务器已启动.");
            } else {
                logger.warning("服务器启动失败!");
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
}
