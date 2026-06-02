package me.dedushka.monkeyJail;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
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
        // Проверяем, не существует ли уже мир
        if (Bukkit.getWorld(worldName) != null) {
            Bukkit.getLogger().info("Мир " + worldName + " уже существует!");
            customWorld = Bukkit.getWorld(worldName);
            return customWorld;
        }

        // Проверяем, существует ли файл структуры в папке плагина
        File structureFile = new File(plugin.getDataFolder(), "structures/" + structureName + ".nbt");
        if (!structureFile.exists()) {
            Bukkit.getLogger().severe("Файл структуры не найден: " + structureFile.getPath());
            Bukkit.getLogger().severe("Убедитесь, что файл " + structureName + ".nbt находится в папке plugins/MonkeyJail/structures/");
            return null;
        }

        // Создаем мир с кастомным генератором (передаём plugin и имя структуры)
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new SingleChunkWorldGenerator(plugin, structureName));
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);

        customWorld = Bukkit.createWorld(creator);

        if (customWorld != null) {
            // Принудительно генерируем только чанк (0,0)
            customWorld.loadChunk(0, 0);

            // Закрепляем чанк в памяти
            customWorld.getChunkAt(0, 0).setForceLoaded(true);

            // Устанавливаем спавн (координаты 8, 64, 8 как у вас)
            Location spawn = new Location(customWorld, 8, 64, 8);
            customWorld.setSpawnLocation(spawn);

            // Устанавливаем границу мира
            WorldBorder border = customWorld.getWorldBorder();
            border.setCenter(8, 8);
            border.setSize(32);
            border.setWarningDistance(0);

            // Отключаем автосохранение
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