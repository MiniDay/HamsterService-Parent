package cn.hamster3.service.bukkit.command;

import cn.hamster3.service.bukkit.api.ServiceMessageAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SafeModeCommand implements CommandExecutor {
    public static final SafeModeCommand INSTANCE = new SafeModeCommand();

    private SafeModeCommand() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean mode;
        if (args.length < 2) {
            sender.sendMessage("§c/service safeMode [on/off]");
            return true;
        }
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
                sender.sendMessage("§c/service safeMode [on/off]");
                return true;
            }
        }
        ServiceMessageAPI.setSafeMode(mode);
        if (mode) {
            sender.sendMessage("§a已开启安全模式.");
        } else {
            sender.sendMessage("§a已关闭安全模式.");
        }
        return true;
    }
}
