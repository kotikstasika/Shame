package org.dimasik.shame;

import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.shame.command.CommandExecutor;
import org.dimasik.shame.command.impl.*;
import org.dimasik.shame.config.Config;
import org.dimasik.shame.database.DatabaseManager;
import org.dimasik.shame.fakelags.listener.PacketListener;
import org.dimasik.shame.fakelags.listener.PlayerListener;
import org.dimasik.shame.fakelags.manager.TrollingManager;
import org.dimasik.shame.modules.impl.AddModule;
import org.dimasik.shame.modules.impl.BanModule;
import org.dimasik.shame.modules.impl.DamageModule;

public final class Shame extends JavaPlugin {

    @Getter
    private static TrollingManager trollingManager;
    @Getter
    private static PacketListener packetListener;
    @Getter
    private CommandExecutor commandExecutor;
    @Getter
    private static Shame instance;
    @Getter
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        setupConfig();
        setupCommand();
        setupFakeLags();
        setupModules();
        setupDatabase();
    }

    private void setupConfig(){
        saveDefaultConfig();
        new Config(getConfig());
    }

    private void setupCommand(){
        commandExecutor = new CommandExecutor();
        var command = getCommand("shame");
        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);

        new Ban("ban").register();
        new Damage("damage").register();
        new Crash("crash").register();
        new BlackScreen("blackscreen").register();
        new Rollback("rollback").register();
        new MixInv("mixinv").register();
        new Troll("troll").register();
        new DeleteChunk("deletechunk").register();
        new Add("add").register();
        new Get("get").register();
        new BlockMovement("blockmovement").register();
    }

    private void setupFakeLags(){
        trollingManager = new TrollingManager();
        packetListener = new PacketListener();

        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void setupModules(){
        new AddModule();
        new BanModule();
        new DamageModule();
    }

    private void setupDatabase(){
        databaseManager = new DatabaseManager(
                Config.getString("mysql.host", "localhost"),
                Config.getString("mysql.database", "lite_shame"),
                Config.getString("mysql.user", "root"),
                Config.getString("mysql.password", "сайнес гпт кодер"),
                Config.getInteger("mysql.port", 3306)
        );
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
    }
}
