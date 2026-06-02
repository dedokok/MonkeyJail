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
    DataBaseManager DBM = new DataBaseManager();
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {


        List<String> completions = new ArrayList<>();

        // args[0] - первый аргумент (create, delete, list и т.д.)
        // args[1] - второй аргумент (start, stop, done)

        ArrayList<String> oneArgumentAndJailName = new ArrayList<>(List.of("editJail","deleteJail","showJail","tpJail"));
        //ArrayList<String> oneArgumentAndUsername = new ArrayList<>(List.of("jail","unjail"));


        //тут будет /monkey ...
        //getLogger().info("Количество аргументов: "+args.length);
        if (args.length == 1) {
            //getLogger().info("Прошёл в аргументы /monkey");
            String[] options = {"createJail","jail","editJail","showJail","unjail","deleteJail","tpJail"};
            String partial = args[0];

            for (String option : options) {
               // getLogger().info("Получил аргумент /monkey "+option);
                if (option.startsWith(partial)) {
                    //getLogger().info("Прошёл в if у /monkey "+option);
                    completions.add(option);
                }
            }

        }
        //тут /monkey edit/delete/show/tp <название тюрьмы>
        else if (args.length == 2 && oneArgumentAndJailName.contains(args[0])) {
            ArrayList<String> options = DBM.getJailNames();

            String partial = args[1];
            for (String option : options) {
                if (option.startsWith(partial)) {
                    completions.add(option);
                }
            }
        }
        //тут /monkey jail <ник>
        else if (args.length == 2 && args[0].equals("jail")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        //тут /monkey unjail <ник>. Сделал отдельно, т.к. надо,чтобы отображались только заключённые, а не все игроки
        else if (args.length == 2 && args[0].equals("unjail")) {
            completions.addAll(JailLogic.monkeyList.keySet());
        }
        //тут будет /monkey create ... и /monkey editJail <название_тюрьмы> ...
        else if ((args.length == 2 && args[0].equalsIgnoreCase("createJail")) ||
                (args.length == 3 && args[0].equalsIgnoreCase("editJail") && DBM.getJailNames().contains(args[1]))
        ) {
            String[] options = {"help", "start", "setFA", "setSA", "setHeight", "stop", "done",
            "addB","removeB","show","setName","setSB"};
            //getLogger().info("Прошёл в аргументы после /monkey create");
            //getLogger().info("Получил аргумент /monkey create "+option);
            //getLogger().info("Прошёл в if у /monkey create "+option);
            completions.addAll(Arrays.asList(options));
        }
        // /monkey jail/unjail <ник> <название_тюрьмы)


        return completions;
    }
}
