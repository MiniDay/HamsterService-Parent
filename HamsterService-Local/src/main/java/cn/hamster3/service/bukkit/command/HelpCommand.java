package cn.hamster3.service.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements CommandExecutor {
    public static final HelpCommand INSTANCE = new HelpCommand();

    private HelpCommand() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage("§c/service safeMode [on/off]");
        sender.sendMessage("§c/service command [bukkit/proxy] [命令内容]");
        return true;
    }
}
