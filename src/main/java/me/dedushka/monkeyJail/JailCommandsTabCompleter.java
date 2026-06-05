package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Classes.MonkeyClass;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class JailCommandsTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {


        List<String> completions = new ArrayList<>();

        // args[0] - первый аргумент (create, delete, list и т.д.)
        // args[1] - второй аргумент (start, stop, done)

        ArrayList<String> oneArgumentAndJailName = new ArrayList<>(List.of("editJail","deleteJail","showJail","tpJail"));

        // /monkey ...
        if (args.length == 1) {
            String[] options = {"fuck","createJail","jail","editJail","showJail","unjail","deleteJail","tpJail"};
            String partial = args[0];
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }

        }
        //тут /monkey edit/delete/show/tp <название тюрьмы>
        else if (args.length == 2 && oneArgumentAndJailName.contains(args[0])) {
            ArrayList<String> options = new ArrayList<>(JailLogic.jails.keySet());
            String partial = args[1];
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }
        }
        //тут /monkey jail <ник>
        else if (args.length == 2 && args[0].equals("jail")) {
            String partial = args[1];
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        }
        //тут /monkey unjail <ник> и /monkey fuck <ник>. Сделал отдельно, т.к. надо,чтобы отображались только заключённые, а не все игроки
        else if (args.length == 2 && (args[0].equals("unjail") || args[0].equals("fuck"))) {
            String partial = args[1];
            for(String monkey_name : JailLogic.monkeyList.keySet()){
                if (monkey_name.startsWith(partial)) {
                    completions.add(monkey_name);
                }
            }
        }
        //тут будет /monkey create ...
        else if ((args.length == 2 && args[0].equalsIgnoreCase("createJail"))
        ) {
            String[] options = {"help", "start", "setFA", "setSA", "setHeight", "stop", "done",
            "addB","removeB","show","setName","setSB"};

            String partial = args[1];
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }
        }
        // /monkey editJail <название_тюрьмы> ...
        else if(args.length == 3 && args[0].equalsIgnoreCase("editJail") && JailLogic.jails.containsKey(args[1])){
            String[] options = {"help", "start", "setFA", "setSA", "setHeight", "stop", "done",
                    "addB","removeB","show","setName","setSB"};
            String partial = args[2];
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }
        }
        // /monkey jail <ник_игрока>
        else if(args.length == 3 && args[0].equalsIgnoreCase("jail")){
            String[] options = JailLogic.jails.keySet().toArray(new String[0]);
            String partial = args[2];
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }
        }
        return completions;
    }
}
