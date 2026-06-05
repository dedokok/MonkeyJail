package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Listeners.EventListener;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.bukkit.Bukkit.getLogger;


public final class MonkeyJail extends JavaPlugin {
    private DataBaseManager DBM = new DataBaseManager();
    private OneChunkWorldManager worldManager;
    public static MonkeyJail instance;
    public static SkinsRestorer skinsRestorerAPI;

    @Override
    public void onEnable() {
        instance = this;
        extractStructure();
        saveDefaultConfig();
        checkDataBase();
        new JailLogic(this).startJail();
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        Plugin skinsRest = Bukkit.getPluginManager().getPlugin("SkinsRestorer");
        if(skinsRest!=null){
            this.skinsRestorerAPI = SkinsRestorerProvider.get();
            getLogger().info("SkinsRestorer загружен");
        }
        else{
            getLogger().info("SkinsRestorer не загружен. У обезьян не будет скина");
        }
        getCommand("monkey").setExecutor(new JailCommands());
        getCommand("monkey").setTabCompleter(new JailCommandsTabCompleter());

        worldManager = new OneChunkWorldManager(this);

    }

    @Override
    public void onDisable() {
        JailCommands jC = new JailCommands();
        jC.stopProcess(null);
        JailCommands.tpAllFromShreakingMachine();
        JailCommands.removeShows();

    }

    private void checkDataBase(){
        File dataFolder = getDataFolder();
        File dbFile = new File(dataFolder, "database.db");
        if (dbFile.exists()) {
            DBM.connectDB();
            getLogger().info("База данных найдена");
        } else {
            getLogger().info("Файл базы данных не найден, будет создан новый");
            DBM.createDB();
        }
    }
    public static MonkeyJail getInstance() {
        return instance;
    }

    public OneChunkWorldManager getWorldManager() {
        return worldManager;
    }

    private void extractStructure() {
        File structureDir = new File(getDataFolder(), "structures");
        if (!structureDir.exists()) {
            structureDir.mkdirs();
        }

        File targetFile = new File(structureDir, "shreakmachine.nbt");

        if (!targetFile.exists()) {
            try (InputStream inputStream = getResource("structures/shreakmachine.nbt")) {
                if (inputStream == null) {
                    getLogger().severe("Файл structures/shreakmachine.nbt не найден в ресурсах плагина!");
                    getLogger().severe("Создайте папку src/main/resources/structures/ и положите туда shreakmachine.nbt");
                    return;
                }
                Files.copy(inputStream, targetFile.toPath());
                getLogger().info("Структура shreakmachine.nbt скопирована в папку плагина!");
            } catch (IOException e) {
                getLogger().warning("Не удалось получить структуру траходрома");
            }
        }
    }
}



