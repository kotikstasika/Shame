package org.dimasik.shame.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.dimasik.shame.utils.Pair;

public class Config {
    private static FileConfiguration config;

    public Config(FileConfiguration config){
        this.config = config;
    }

    public static String getString(String path, String def){
        return config.getString(path, def);
    }

    public static int getInteger(String path, int def){
        return config.getInt(path, def);
    }

    @SafeVarargs
    public static String replace(String string, Pair<String, String>... replaces){
        for(Pair<String, String> entry : replaces){
            while (string.contains(entry.getFirst())){
                string = string.replace(entry.getFirst(), entry.getSecond());
            }
        }
        return string;
    }
}
