package org.dimasik.shame.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final boolean SUPPORTS_RGB;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        int subVersion = Integer.parseInt(version.replace("v", "").replace("1_", "").replaceAll("_R\\d", ""));
        SUPPORTS_RGB = subVersion >= 16;
    }

    public static String color(String message) {
        if (message == null) return "";

        if (SUPPORTS_RGB) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder buffer = new StringBuilder();

            while (matcher.find()) {
                String hex = matcher.group(1);
                try {
                    String replacement = ChatColor.of("#" + hex).toString();
                    matcher.appendReplacement(buffer, replacement);
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, "");
                }
            }

            matcher.appendTail(buffer);
            message = buffer.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}