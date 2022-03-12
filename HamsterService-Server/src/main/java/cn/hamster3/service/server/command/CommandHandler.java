package cn.hamster3.service.server.command;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceMessageInfo;
import cn.hamster3.service.server.connection.ServiceCentre;
import com.google.gson.JsonPrimitive;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger("command");

    private final NioEventLoopGroup loopGroup;
    private final ServiceCentre centre;
    private final File playerDataFolder;

    private boolean started;

    public CommandHandler(NioEventLoopGroup loopGroup, ServiceCentre centre, File playerDataFolder) {
        this.loopGroup = loopGroup;
        this.centre = centre;
        this.playerDataFolder = playerDataFolder;
    }

    public void startScanConsole() {
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

    @SuppressWarnings("SpellCheckingInspection")
    public void executeCommand(String command) throws Exception {
        String[] args = command.split(" ");
        switch (args[0].toLowerCase()) {
            case "?":
            case "help": {
                help();
                break;
            }
            case "end":
            case "stop": {
                stop();
                break;
            }
            case "command": {
                command(args);
                break;
            }
            case "safemode": {
                safeMode(args);
                break;
            }
            case "save": {
                save();
                break;
            }
            default: {
                logger.info("未知指令. 请输入 help 查看帮助.");
                break;
            }
        }
    }

    public void help() {
        logger.info("===============================================================");
        logger.info("help                            - 查看帮助.");
        logger.info("save                            - 保存所有玩家数据.");
        logger.info("stop                            - 关闭HamsterService-Server.");
        logger.info("safeMode [on/off]               - 开启/关闭安全模式.");
        logger.info("command [bukkit/proxy] [命令]    - 让所有已连接的 Bukkit/BC 服务器以控制台身份执行命令.");
        logger.info("===============================================================");
    }

    public void stop() throws Exception {
        started = false;
        logger.info("准备关闭服务器...");
        loopGroup.shutdownGracefully().await();
        logger.info("服务器已关闭!");

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
    }

    public void command(String[] args) {
        if (args.length < 3) {
            logger.info("command [bukkit/proxy] [命令内容]");
            return;
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
    }

    public void safeMode(String[] args) {
        boolean mode = !centre.isSafeMode();
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "on":
                case "enable": {
                    mode = true;
                    break;
                }
                case "off":
                case "disable": {
                    mode = false;
                    break;
                }
                default: {
                    logger.info("safeMode [on/off]");
                    return;
                }
            }
        }
        centre.broadcastServiceMessage(new ServiceMessageInfo(
                centre.getInfo(),
                "HamsterService",
                "setSafeMode",
                new JsonPrimitive(mode)
        ));
        if (mode) {
            logger.info("已开启安全模式.");
        } else {
            logger.info("已关闭安全模式.");
        }
    }

    public void save() {
        logger.info("正在保存玩家存档...");
        synchronized (centre.getAllPlayerInfo()) {
            for (ServicePlayerInfo playerInfo : centre.getAllPlayerInfo()) {
                File dataFile = new File(playerDataFolder, playerInfo.getUuid() + ".json");
                try {
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8);
                    writer.write(playerInfo.saveToJson().toString());
                    writer.close();
                } catch (Exception e) {
                    logger.error("保存玩家 " + playerInfo.getUuid() + " 的存档时遇到了一个异常: ", e);
                }
            }
        }
        logger.info("玩家存档保存完毕.");
    }
}
