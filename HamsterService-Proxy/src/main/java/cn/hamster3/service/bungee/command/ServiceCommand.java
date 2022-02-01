package cn.hamster3.service.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ServiceCommand extends Command {
    public static final ServiceCommand INSTANCE = new ServiceCommand();

    private ServiceCommand() {
        super("service", "service.admin");
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("service.admin")) {
            sender.sendMessage(new TextComponent("§c你没有权限执行这个命令!"));
            return;
        }
        if (args.length < 1) {
            HelpCommand.INSTANCE.onCommand(sender, args);
            return;
        }
        switch (args[0]) {
            case "safemode": {
                SafeModeCommand.INSTANCE.onCommand(sender, args);
                break;
            }
            case "command": {
                SudoConsoleCommand.INSTANCE.onCommand(sender, args);
                break;
            }
        }

    }
}
