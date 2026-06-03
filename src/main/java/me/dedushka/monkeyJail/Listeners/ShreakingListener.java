package me.dedushka.monkeyJail.Listeners;

import me.dedushka.monkeyJail.JailCommands;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;

public class ShreakingListener implements Listener {
    public static ArrayList<String> monkey_names = new ArrayList<>();
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(monkey_names.contains(event.getPlayer().getName())){
            Location from = event.getFrom();
            Location to = event.getTo();

            // Проверяем, изменились ли координаты (движение тела)
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setCancelled(true);
                return;
            }
            if (to.getYaw()<-140 || to.getYaw()>-40) {
                event.setCancelled(true);
                //event.getPlayer().sendMessage("§eВы повернули голову!");
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (monkey_names.contains(event.getPlayer().getName()) && !event.isSneaking()) {
            event.setCancelled(true); // Запрещаем
            event.getPlayer().setSneaking(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(JailCommands.shreakLocations.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(JailCommands.shreakLocations.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(JailCommands.shreakLocations.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(JailCommands.shreakLocations.containsKey(event.getPlayer().getName())) {
            event.getPlayer().teleport(JailCommands.shreakLocations.get(event.getPlayer().getName()));
            JailCommands.shreakLocations.remove(event.getPlayer().getName());
        }
    }
}
