package cn.hamster3.service.bukkit.command;

import cn.hamster3.service.bukkit.api.ServiceMessageAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SudoConsoleCommand implements CommandExecutor {
    public static final SudoConsoleCommand INSTANCE = new SudoConsoleCommand();

    private SudoConsoleCommand() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
