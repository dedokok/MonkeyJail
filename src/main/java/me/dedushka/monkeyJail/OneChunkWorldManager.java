package me.dedushka.monkeyJail;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;

public class OneChunkWorldManager {

    private final JavaPlugin plugin;
    private World customWorld;

    public OneChunkWorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public World createWorld(String worldName, String structureName) {
        if (Bukkit.getWorld(worldName) != null) {
            Bukkit.getLogger().info("Мир " + worldName + " уже существует!");
            customWorld = Bukkit.getWorld(worldName);
            return customWorld;
        }

        File structureFile = new File(plugin.getDataFolder(), "structures/" + structureName + ".nbt");
        if (!structureFile.exists()) {
            Bukkit.getLogger().severe("Файл структуры не найден: " + structureFile.getPath());
            return null;
        }

        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new SingleChunkWorldGenerator(plugin, structureName));
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);

        customWorld = Bukkit.createWorld(creator);

        if (customWorld != null) {
            customWorld.loadChunk(0, 0);
            customWorld.getChunkAt(0, 0).setForceLoaded(true);
            Location spawn = new Location(customWorld, 8, 64, 8);
            customWorld.setSpawnLocation(spawn);
            WorldBorder border = customWorld.getWorldBorder();
            border.setCenter(8, 8);
            border.setSize(16);
            border.setWarningDistance(0);
            customWorld.setGameRule(GameRules.ADVANCE_TIME,false);
            customWorld.setGameRule(GameRules.ADVANCE_WEATHER,false);
            customWorld.setGameRule(GameRules.SPAWN_MOBS,false);
            customWorld.setTime(1000);
            customWorld.setStorm(false);
            customWorld.setThundering(false);
            customWorld.setDifficulty(Bukkit.getWorld("world").getDifficulty());
            customWorld.setAutoSave(false);
            Bukkit.getLogger().info("Мир '" + worldName + "' успешно создан!");
            Bukkit.getLogger().info("Структура '" + structureName + "' будет размещена генератором на координатах 8, 64, 8");
        }

        return customWorld;
    }

    public World getWorld() {
        return customWorld;
    }

    public World getWorld(String worldName) {
        return Bukkit.getWorld(worldName);
    }
}