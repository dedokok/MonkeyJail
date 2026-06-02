package me.dedushka.monkeyJail.Listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

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
}
