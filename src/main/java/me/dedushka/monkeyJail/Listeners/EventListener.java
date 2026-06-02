package me.dedushka.monkeyJail.Listeners;

import me.dedushka.monkeyJail.JailCommands;
import me.dedushka.monkeyJail.JailLogic;
import me.dedushka.monkeyJail.MonkeyJail;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
            PlayerStorage playerStorage = MonkeyJail.skinsRestorerAPI.getPlayerStorage();
            try {
                Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
                        player.getUniqueId(),
                        player.getName()
                );
                SkinProperty sP = property.orElse(null);
                getLogger().info("Property = " + (sP==null ? "null" : "true"));

                JailLogic.skinsHistory.put(player.getName(),property.orElse(null));

                JailCommands.setSkinFromUrl(Bukkit.getPlayer(player.getName()), "http://textures.minecraft.net/texture/af20e8affb49949274a61ad7cf3da9f83026abad7e184d184109ff86785bb6f5");

            }
            catch(Exception e ){
                getLogger().warning("Не удалось получить скин игрока");
            }
        }
    }
}
