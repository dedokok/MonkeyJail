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


public final class MonkeyJail extends JavaPlugin {
    private DataBaseManager DBM = new DataBaseManager(this);
    private OneChunkWorldManager worldManager;
    private static MonkeyJail instance;
    public static SkinsRestorer skinsRestorerAPI;

    @Override
    public void onEnable() {
        extractStructure();
        saveDefaultConfig();
        checkDataBase();
        new JailLogic(this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        Plugin skinsRest = Bukkit.getPluginManager().getPlugin("SkinsRestorer");
        if(skinsRest!=null){
            this.skinsRestorerAPI = SkinsRestorerProvider.get();
            getLogger().info("SkinsRestorer загружен");
        }
        else{
            getLogger().info("SkinsRestorer не загружен. У обезьян не будет скина");
        }
        getCommand("monkey").setExecutor(new JailCommands(this,skinsRestorerAPI));
        getCommand("monkey").setTabCompleter(new JailCommandsTabCompleter());






        //getServer().getPluginManager().registerEvents(new EventListener(),this);

        //getServer().getPluginManager().registerEvents(new CoreProtectINC(this), this);

        //getCommand("xfinder").setExecutor(new CommandManager());


        //new VeinGUI(this);
        //new ManageGUI(this);
        //new BlackListGUI(this);
        // Plugin startup logic

        worldManager = new OneChunkWorldManager(this);




        //new JailCommands(this);
        new JailLogic(this).startJail();

    }

    @Override
    public void onDisable() {
        JailCommands jC = new JailCommands();
        jC.stopProcess();

    }

    private void checkDataBase(){
        File dataFolder = getDataFolder();
        File dbFile = new File(dataFolder, "database.db");
        if (dbFile.exists()) {
            DBM.connectDB();
            //getLogger().info("База данных найдена!");
        } else {
            //getLogger().info("Файл базы данных не найден, будет создан новый");
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
                e.printStackTrace();
            }
        }
    }
}



