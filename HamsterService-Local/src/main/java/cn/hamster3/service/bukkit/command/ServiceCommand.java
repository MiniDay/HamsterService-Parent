package cn.hamster3.service.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ServiceCommand implements CommandExecutor {
    public static final ServiceCommand INSTANCE = new ServiceCommand();

    private ServiceCommand() {
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("service.admin")) {
            sender.sendMessage("§c你没有权限执行这个命令!");
            return true;
        }
        if (args.length < 1) {
            return HelpCommand.INSTANCE.onCommand(sender, command, label, args);
        }
        switch (args[0]) {
            case "safemode": {
                return SafeModeCommand.INSTANCE.onCommand(sender, command, label, args);
            }
            case "command": {
                return SudoConsoleCommand.INSTANCE.onCommand(sender, command, label, args);
            }
        }
        return true;
    }
}
