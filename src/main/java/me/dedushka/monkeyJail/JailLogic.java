package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Classes.BlockPosClass;
import me.dedushka.monkeyJail.Classes.JailClass;
import me.dedushka.monkeyJail.Classes.MonkeyClass;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
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
    private static DataBaseManager DBM = new DataBaseManager();
    public static HashMap<String,MonkeyClass>monkeyList = DBM.getAllMonkeys(-1,-1);
    private static HashMap<String,MonkeyClass>updatedTimeMonkeys = new HashMap<>();
    public static HashMap<String, JailClass> jails= DBM.loadAllJails();

    public static HashMap<String,SkinProperty>skinsHistory = new HashMap<>();

    public static ArrayList<String> monkeys_shreaking = new ArrayList<>();

    private static MonkeyJail MJ;
    private static int ticks = 0;
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

            //JailCommands jC = new JailCommands();
            //getLogger().info("Размер списка: "+jC.blocksDisplay.size());

            doEveryLoop();
        }, 0L, 10L);
    }


    public void doEveryLoop(){
        ticks+=10;
        //ArrayList<Player>onlinePlayers = Bukkit.getOnlinePlayers();
        Iterator<MonkeyClass> it = monkeyList.values().iterator();
        //getLogger().info("Количество обезьян: "+monkeyList.size());
        while (it.hasNext()) {
            //getLogger().info("Прошёл в обезьянник 1");
            MonkeyClass monkey = it.next();
            if(!monkeys_shreaking.contains(monkey.username)) {
                Player player = Bukkit.getPlayer(monkey.username);
                //getLogger().info("Прошёл в обезьянник 2");
                if (player != null && player.isOnline()) {
                    //getLogger().info("Прошёл в обезьянник 3");
                    if (monkey.time_left <= 0) {
                        //getLogger().info("Прошёл в обезьянник 4");
                        removeFromMonkeys(monkey.username);
                    } else {
                        //getLogger().info("Прошёл в обезьянник 5");
                        monkey.time_left -= 10;
                        updatedTimeMonkeys.put(monkey.username, monkey);
                        isTimeLeftUpdated = true;
                        Location pL = player.getLocation();
                        int x = (int) Math.floor(pL.getX());
                        int y = (int) Math.floor(pL.getY());
                        int z = (int) Math.floor(pL.getZ());
                        BlockPosClass playerPos = new BlockPosClass(x, y, z);
                        JailClass jail = jails.get(monkey.jail_name);
                        //getLogger().info("Название тюрьмы обезьяны: "+monkey.jail_name + ". Найдена: "+(jail==null ? "false" : "true"));
                        if (jail != null) {
                            //getLogger().info("Прошёл в обезьянник 6");
                            if (!jail.blocks.contains(playerPos)) {
                                //getLogger().info("Прошёл в обезьянник 7");
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
        }
        //обновление базы данных обезьян каждую минуту (чтобы в БД время уменьшалось)
        if(ticks%1200==0 && isTimeLeftUpdated){
            isTimeLeftUpdated=false;
            DBM.updateMonkeyTable(updatedTimeMonkeys);
        }

    }

    public static void removeFromMonkeys(String username){
        DBM.removeMonkey(username);
        monkeyList.remove(username);
        // Generate skin from URL (use CLASSIC or SLIM)
        try {
            getLogger().info("размер skinsHistory: " + skinsHistory.size() +". Есть " + (skinsHistory.get(username)==null ? "false" : "true"));
            // Apply directly to player
            Player pp = Bukkit.getPlayer(username);
            MonkeyJail.skinsRestorerAPI.getSkinApplier(Player.class).applySkin(pp,skinsHistory.get(username));
        }
        catch(Exception e){
            getLogger().info("Не удалось установить скин");
        }
        Bukkit.getPlayer(username).teleport(Bukkit.getWorld("world").getSpawnLocation());
    }


    public JailClass getJail(String jailName){
        //BlockPosClass bP = new BlockPos(123,62,321);
        //MJ.getConfig().set("block_pos",bP);

        //saveConfig();
        JailClass jail = MJ.getConfig().getObject("jails."+jailName,JailClass.class);
        if(jail==null){
            getLogger().info("Ошибка. Такой тюрьмы нет");
            return null;
        }
        getLogger().info("Тюрьма " +jailName + " успешно получена");
        return jail;
//        BlockPos Bp2 = getConfig().getObject("block_pos",BlockPos.class);
//        getLogger().info(Bp2.toString());
    }






}
