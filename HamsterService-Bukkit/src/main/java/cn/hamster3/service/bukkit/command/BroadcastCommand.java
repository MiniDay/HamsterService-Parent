package cn.hamster3.service.bukkit.command;

import cn.hamster3.service.bukkit.api.ServiceMessageAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BroadcastCommand implements CommandExecutor {
    public static final BroadcastCommand INSTANCE = new BroadcastCommand();

    private BroadcastCommand() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("service.admin")) {
            sender.sendMessage("§c你没有权限执行这个命令!");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§c/service command [bukkit/proxy] [命令内容]");
            return true;
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
                sender.sendMessage("§c/service command [bukkit/proxy] [命令内容]");
                return true;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        ServiceMessageAPI.sendServiceMessage("HamsterService", action, builder.toString());
        sender.sendMessage("§c已广播命令执行信息.");
        return true;
    }
}
