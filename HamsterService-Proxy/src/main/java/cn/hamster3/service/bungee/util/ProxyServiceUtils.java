package cn.hamster3.service.bungee.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("deprecation")
public class ProxyServiceUtils {
    public ProxyServiceUtils() {
    }

    public static BaseComponent[] parseComponentFromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new ComponentBuilder(json.getAsString()).create();
        }
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            ArrayList<BaseComponent> list = new ArrayList<>();
            for (JsonElement element : array) {
                Collections.addAll(list, parseComponentFromJson(element));
            }
            BaseComponent[] components = new BaseComponent[list.size()];
            for (int i = 0; i < list.size(); i++) {
                components[i] = list.get(i);
            }
            return components;
        }
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            ComponentBuilder builder = new ComponentBuilder(object.get("text").getAsString());
            if (object.has("color")) {
                builder.color(getColorByName(object.get("color").getAsString()));
            }
            if (object.has("bold")) {
                builder.bold(object.get("bold").getAsBoolean());
            }
            if (object.has("italic")) {
                builder.italic(object.get("italic").getAsBoolean());
            }
            if (object.has("underlined")) {
                builder.underlined(object.get("underlined").getAsBoolean());
            }
            if (object.has("strikethrough")) {
                builder.strikethrough(object.get("strikethrough").getAsBoolean());
            }
            if (object.has("obfuscated")) {
                builder.obfuscated(object.get("obfuscated").getAsBoolean());
            }
            if (object.has("insertion")) {
                builder.insertion(object.get("insertion").getAsString());
            }
            if (object.has("clickEvent")) {
                builder.event(parseClickEvent(object.getAsJsonObject("clickEvent")));
            }
            if (object.has("hoverEvent")) {
                builder.event(parseHoverEvent(object.getAsJsonObject("hoverEvent")));
            }
            if (object.has("extra")) {
                builder.append(parseComponentFromJson(object.get("extra")));
            }
            return builder.create();
        }
        throw new IllegalArgumentException("非法json字符串: " + json);
    }

    private static ChatColor getColorByName(String name) {
        for (ChatColor value : ChatColor.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }


    private static ClickEvent parseClickEvent(JsonObject object) {
        return new ClickEvent(
                ClickEvent.Action.valueOf(
                        object
                                .get("action")
                                .getAsString()
                                .toUpperCase()
                ),
                object.get("value").getAsString()
        );
    }

    private static HoverEvent parseHoverEvent(JsonObject object) {
        return new HoverEvent(
                HoverEvent.Action.valueOf(
                        object
                                .get("action")
                                .getAsString()
                                .toUpperCase()
                ),
                parseComponentFromJson(object.get("value"))
        );
    }
}
