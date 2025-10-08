package org.dimasik.shame.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.dimasik.shame.utils.Pair;

import java.util.List;

public class Config {
    private static FileConfiguration config;

    public Config(FileConfiguration config){
        this.config = config;
    }

    public static String getString(String path){
        return config.getString(path);
    }

    public static String getString(String path, String def){
        return config.getString(path, def);
    }

    public static Integer getInteger(String path){
        return config.getInt(path);
    }

    public static int getInteger(String path, int def){
        return config.getInt(path, def);
    }

    public static List<String> getStringList(String path){
        return config.getStringList(path);
    }

    public static List<String> getStringList(String path, List<String> def){
        List<String> list = config.getStringList(path);
        if(list == null || list.isEmpty()){
            return def;
        }
        else{
            return list;
        }
    }

    public static String replace(String string, Pair<String, String>... replaces){
        for(Pair<String, String> entry : replaces){
            while (string.contains(entry.getFirst())){
                string = string.replace(entry.getFirst(), entry.getSecond());
            }
        }
        return string;
    }
}
