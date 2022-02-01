package cn.hamster3.service.bungee.command;

import cn.hamster3.service.bungee.api.ServiceMessageAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class SafeModeCommand {
    public static final SafeModeCommand INSTANCE = new SafeModeCommand();

    private SafeModeCommand() {
    }

    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        boolean mode;
        if (args.length < 2) {
            sender.sendMessage(new TextComponent("§c/service safeMode [on/off]"));
            return;
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
                sender.sendMessage(new TextComponent("§c/service safeMode [on/off]"));
                return;
            }
        }
        ServiceMessageAPI.setSafeMode(mode);
        if (mode) {
            sender.sendMessage(new TextComponent("§a已开启安全模式."));
        } else {
            sender.sendMessage(new TextComponent("§a已关闭安全模式."));
        }
    }
}
