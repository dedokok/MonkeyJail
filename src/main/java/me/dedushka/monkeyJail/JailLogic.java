package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Classes.BlockPosClass;
import me.dedushka.monkeyJail.Classes.JailClass;
import me.dedushka.monkeyJail.Classes.MonkeyClass;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.property.SkinApplier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinVariant;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

import static org.bukkit.Bukkit.getLogger;

public class JailLogic {
    public static DataBaseManager DBM = new DataBaseManager();
    public static HashMap<String,MonkeyClass>monkeyList = DBM.getAllMonkeys(-1,-1);
    public static HashMap<String,MonkeyClass>updatedTimeMonkeys = new HashMap<>();
    public static HashMap<String, JailClass> jails= DBM.loadAllJails();
    public static ArrayList<String> monkeys_shreaking = new ArrayList<>();
    public static ArrayList<String> needToRemove = new ArrayList<>();
    public static MonkeyJail MJ;
    public static int ticks = 0;
    public static boolean isTimeLeftUpdated = false;

    public JailLogic(MonkeyJail MJ){
        this.MJ = MJ;
        updateMonkeyList();
        updateJailList();
    }
    public JailLogic(){
    };

    public void updateMonkeyList() {
        monkeyList = DBM.getAllMonkeys(-1, -1);
    }
    public void updateJailList(){
        jails = DBM.loadAllJails();
    }

    public void startJail(){
        Bukkit.getScheduler().runTaskTimer(MJ, () -> {
            doEveryLoop();
        }, 0L, 10L);
    }

    //что делать каждую итерацию
    public void doEveryLoop(){
        ticks+=10;
        Iterator<MonkeyClass> it = monkeyList.values().iterator();
        while (it.hasNext()) {
            MonkeyClass monkey = it.next();
            if (!monkeys_shreaking.contains(monkey.username)) {
                Player player = Bukkit.getPlayer(monkey.username);
                if (player != null && player.isOnline()) {
                    if (monkey.time_left <= 0) {
                        removeFromMonkeys(monkey.username);
                    } else {
                        monkey.time_left -= 10;
                        updatedTimeMonkeys.put(monkey.username, monkey);
                        isTimeLeftUpdated = true;
                        Location pL = player.getLocation();
                        int x = (int) Math.floor(pL.getX());
                        int y = (int) Math.floor(pL.getY());
                        int z = (int) Math.floor(pL.getZ());
                        BlockPosClass playerPos = new BlockPosClass(x, y, z);
                        JailClass jail = jails.get(monkey.jail_name);
                        if (jail != null && !jail.blocks.contains(playerPos)) {
                            player.teleport(new Location(
                                    Bukkit.getWorld(jail.world),
                                    jail.spawnBlock.x,
                                    jail.spawnBlock.y,
                                    jail.spawnBlock.z
                            ));
                        }

                    }

                }
            }
        }
        //обновление базы данных обезьян каждую минуту (чтобы в БД время уменьшалось)
        if(ticks%1200==0 && isTimeLeftUpdated){
            isTimeLeftUpdated=false;
            DBM.updateMonkeyTable(updatedTimeMonkeys);
        }

    }

    //процесс удаления обезьяны из тюрьмы
    public static void removeFromMonkeys(String username){
        if(Bukkit.getPlayer(username)==null){
            return;
        }
        if(Bukkit.getPlayer(username)!=null) {
            DBM.removeMonkey(username);
            monkeyList.remove(username);
            Location location = Bukkit.getPlayer(username).getLocation();
            Bukkit.getPlayer(username).teleport(new Location(location.getWorld(), location.getX(), 1000, location.getZ()));
            JailLogic.needToRemove.add(username);
            Bukkit.getPlayer(username).setHealth(0);


            if (MonkeyJail.skinsRestorerAPI != null) {
                try {
                    PlayerStorage playerStorage = MonkeyJail.skinsRestorerAPI.getPlayerStorage();
                    SkinApplier<Player> applier = MonkeyJail.skinsRestorerAPI.getSkinApplier(Player.class);
                    playerStorage.removeSkinIdOfPlayer(Bukkit.getPlayer(username).getUniqueId());
                    applier.applySkin(Bukkit.getPlayer(username));
                } catch (Exception e) {
                    getLogger().info("Не удалось установить скин");
                }
            }
        }
    }
}
