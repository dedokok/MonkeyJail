package me.dedushka.monkeyJail.Listeners;

import me.dedushka.monkeyJail.JailCommands;
import me.dedushka.monkeyJail.JailLogic;
import me.dedushka.monkeyJail.MonkeyJail;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.Optional;

import static org.bukkit.Bukkit.getLogger;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        getLogger().info("Вошёл в onPlayerJoin");
        if(MonkeyJail.skinsRestorerAPI==null) {return;}
        Player player = event.getPlayer();
        if(JailLogic.monkeyList.containsKey(player.getName())){
            getLogger().info("Вошёл в containsKey");
            //PlayerStorage playerStorage = MonkeyJail.skinsRestorerAPI.getPlayerStorage();
            try {
//                Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
//                        player.getUniqueId(),
//                        player.getName()
//                );
//                SkinProperty sP = property.orElse(null);
//                getLogger().info("Property = " + (sP==null ? "null" : "true"));

                //JailLogic.skinsHistory.put(player.getName(),property.orElse(null));

                JailCommands.setSkinFromUrl(Bukkit.getPlayer(player.getName()), "http://textures.minecraft.net/texture/af20e8affb49949274a61ad7cf3da9f83026abad7e184d184109ff86785bb6f5");

            }
            catch(Exception e ){
                getLogger().warning("Не удалось получить скин игрока");
            }
        }
    }

    @EventHandler
    public void onUseEvent(PlayerInteractEvent event){
        if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlaceEvent(BlockPlaceEvent event){
        if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        if (JailLogic.needToRemove.contains(event.getEntity().getName())) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDeathMessage(null);
            MonkeyJail.instance.getServer().getScheduler().runTaskLater(MonkeyJail.instance, () -> {
                event.getEntity().spigot().respawn();
            }, 1L);
            JailLogic.needToRemove.remove(event.getEntity().getName());

        }

    }
    @EventHandler
    public void onHitEvent(EntityDamageByEntityEvent event){
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        if (damager instanceof Player && victim instanceof Player) {
            Player attacker = (Player) damager;
            Player target = (Player) victim;
            if(JailLogic.monkeyList.containsKey(attacker.getName())){
                if(!JailLogic.monkeyList.containsKey(target.getName())){
                    event.setCancelled(true);
                }
            }
        }
    }
}
