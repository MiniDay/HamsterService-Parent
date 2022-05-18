package cn.hamster3.service.server;

import cn.hamster3.service.server.command.CommandHandler;
import cn.hamster3.service.server.config.ServerConfig;
import cn.hamster3.service.server.connection.ServiceCentre;
import cn.hamster3.service.server.util.ServiceUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;

public class Bootstrap {
    public static final Logger LOGGER = LoggerFactory.getLogger("HamsterService");
    private static final File PLAYER_DATA_FOLDER = new File("playerData");

    public static void main(String[] args) {
        ServiceUtils.saveDefaultFile("logSettings.xml");
        ServiceUtils.saveDefaultFile("datasource.properties");

        ServerConfig config = loadConfig();
        if (config == null) {
            return;
        }

        HikariConfig hikariConfig = new HikariConfig("datasource.properties");
        HikariDataSource datasource = new HikariDataSource(hikariConfig);

        try {
            ServiceUtils.initDatabase(datasource);
        } catch (SQLException e) {
            LOGGER.error("初始化数据库连接池时遇到了一个异常:", e);
            return;
        }

        if (PLAYER_DATA_FOLDER.exists()) {
            LOGGER.info("将本地玩家存档转移至数据库中...");
            try {
                ServiceUtils.uploadDataToSQL(datasource, PLAYER_DATA_FOLDER);
            } catch (SQLException e) {
                LOGGER.error("将本地存档转移至数据库时遇到了一个异常。", e);
                return;
            }
            LOGGER.info("本地玩家存档转移至数据库完成，请重启该程序。");
            if (PLAYER_DATA_FOLDER.renameTo(new File("playerData_backup"))) {
                LOGGER.info("已重命名本地玩家存档文件夹。");
            }
            return;
        }

        LOGGER.info("服务器绑定地址: {}", config.getServiceAddress());
        LOGGER.info("服务器绑定端口: {}", config.getServicePort());
        LOGGER.info("服务器线程池数: {}", config.getNioThread());
        LOGGER.info("白名单IP列表: {}", config.getAcceptList());
        ServiceCentre centre = new ServiceCentre(datasource, config);

        try {
            ServiceUtils.loadPlayerData(centre);
        } catch (SQLException e) {
            LOGGER.error("从数据库中加载玩家存档时遇到了一个异常。", e);
            return;
        }

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
                LOGGER.info("服务器已启动. 输入 stop 来关闭该程序.");
                LOGGER.warn("请勿直接点 X 关闭! 否则无法保存玩家存档, 将导致许多插件功能异常!");
                LOGGER.warn("请勿直接点 X 关闭! 否则无法保存玩家存档, 将导致许多插件功能异常!");
                LOGGER.warn("请勿直接点 X 关闭! 否则无法保存玩家存档, 将导致许多插件功能异常!");
                LOGGER.info("若要关闭该程序，请在本控制台使用 stop 命令。");
            } else {
                LOGGER.error("服务器启动失败: {}", future.cause().toString());
                loopGroup.shutdownGracefully();
            }
        });

        new CommandHandler(loopGroup, centre).startScanConsole();
    }

    private static ServerConfig loadConfig() {
        File configFile = ServiceUtils.saveDefaultFile("server.yml");
        try {
            InputStreamReader reader = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8);
            ServerConfig config = new Yaml().loadAs(reader, ServerConfig.class);
            reader.close();
            return config;
        } catch (Exception e) {
            LOGGER.error("加载配置文件时遇到了一个异常: ", e);
            return null;
        }
    }
}
