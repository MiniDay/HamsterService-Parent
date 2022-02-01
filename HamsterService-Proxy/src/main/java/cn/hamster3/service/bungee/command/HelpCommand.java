package cn.hamster3.service.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class HelpCommand  {
    public static final HelpCommand INSTANCE = new HelpCommand();

    private HelpCommand() {
    }

    public void onCommand(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(new TextComponent("§c/service safeMode [on/off]"));
        sender.sendMessage(new TextComponent("§c/service command [bukkit/proxy] [命令内容]"));
    }
}
