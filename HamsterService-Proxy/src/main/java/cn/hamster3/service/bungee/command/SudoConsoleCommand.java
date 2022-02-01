package cn.hamster3.service.bungee.command;

import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class SudoConsoleCommand {
    public static final SudoConsoleCommand INSTANCE = new SudoConsoleCommand();

    private SudoConsoleCommand() {
    }

    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(new TextComponent("§c/service command [bukkit/proxy] [命令内容]"));
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
                sender.sendMessage(new TextComponent("§c/service command [bukkit/proxy] [命令内容]"));
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        ServiceMessageAPI.sendServiceMessage("HamsterService", action, builder.toString());
        sender.sendMessage(new TextComponent("§c已广播命令执行信息."));
    }
}
