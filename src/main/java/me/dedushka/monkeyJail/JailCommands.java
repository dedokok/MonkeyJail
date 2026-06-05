package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Classes.BlockPosClass;
import me.dedushka.monkeyJail.Classes.JailProcessClass;
import me.dedushka.monkeyJail.Listeners.ShreakingListener;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinVariant;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.File;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class JailCommands implements CommandExecutor{
    static DataBaseManager DBM = new DataBaseManager();
    public static HashMap<String, JailProcessClass> jailsCreationProcesses = new HashMap<>();
    static boolean isTiny = false;
    public static HashMap<String,Location>shreakLocations = new HashMap<>();
    public JailCommands(){}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "createjail": {
                    createCommandExecutor(args, player);
                    return true;
                }
                case "jail": {
                    jailMonkeyCommandExecutor(args, player);
                    return true;
                }
                case "unjail": {
                    unJailMonkeyCommandExecutor(args, player);
                    return true;
                }
                case "editjail": {
                    editJailCommandExecutor(args, player);
                    return true;
                }
                case "fuck": {
                    shreakCommandExecutor(args, player);
                    return true;
                }
                case "deletejail": {
                    deleteJailCommandExecutor(args, player);
                    return true;
                }
                case "tpjail": {
                    tpJailCommandExecutor(args, player);
                    return true;
                }
            }
        } else {
            sender.sendMessage("Эта команда только для игроков!");
        }
        return false;
    }

    boolean tpJailCommandExecutor(String[] args,Player player) {
        //0 - tpJail
        //1 - название
        getLogger().info("Начал процесс телепорта");
        if (args.length < 2) {
            player.sendMessage("§cУкажите название тюрьмы");
        }
        if(JailLogic.jails.containsKey(args[1])){
            World jail_world = Bukkit.getWorld(JailLogic.jails.get(args[1]).world);
            BlockPosClass spawnBlock = JailLogic.jails.get(args[1]).spawnBlock;
            double x = spawnBlock.x, z = spawnBlock.z, y=spawnBlock.y;
            player.teleport(new Location(jail_world,x,y,z));
            return true;
        }
        else{
            player.sendMessage("§cТакой тюрьмы не существует!");
            return false;
        }

    }
    boolean deleteJailCommandExecutor(String[] args,Player player){
        //0 - deleteJail
        //1 - название
        getLogger().info("Начал процесс удаления");
        if(args.length<2){
            player.sendMessage("§cУкажите название тюрьмы");
        }
        try {
            getLogger().info("До удаления");
            DBM.deleteJail(args[1]);
            getLogger().info("После удаления");
            JailLogic jL = new JailLogic();
            jL.updateJailList();
            jL.updateMonkeyList();
            player.sendMessage("§cТюрьма успешно удалена");
            return true;
        }
        catch(Exception e){
            player.sendMessage("§cНе удалось удалить тюрьму");
        }
        return false;
    }


    boolean shreakCommandExecutor(String[] args, Player player) {
        //0 - fuck
        //1 - ник
        if (args.length < 2) {
            player.sendMessage("§cУкажите ник обезьяны!");
            return false;
        }

        String monkey_name = args[1];

        if(player.getName().equals(monkey_name)){
            player.sendMessage("§cСебя нельзя отправить на траходром!");
            return false;
        }

        if(JailLogic.monkeyList.containsKey(player.getName())){
            player.sendMessage("§cТы обезьяна!");
            return false;
        }

        if(!JailLogic.monkeyList.containsKey(monkey_name)){
            player.sendMessage("§cЭто не обезьяна!");
            return false;
        }

        Player player_monkey = Bukkit.getPlayer(monkey_name);
        if (player_monkey == null) {
            player.sendMessage("§cОбезьяна не в сети!");
            return false;
        }
        if(!JailLogic.monkeyList.containsKey(monkey_name)){
            player.sendMessage("§cЭто не обезьяна!");
            return false;
        }
        if(player.getLocation().distance(player_monkey.getLocation())>player_monkey.getWorld().getViewDistance()*16){
            player.sendMessage("§cВы должны быть возле обезьяны");
            return false;
        }
        if (isTiny) {
            player.sendMessage("§cТраходром пока занят, подождите немного");
            return false;
        }


        OneChunkWorldManager worldManager = (MonkeyJail.getInstance()).getWorldManager();
        World myWorld = worldManager.createWorld("trahodrom", "shreakmachine");

        if (myWorld == null) {
            player.sendMessage("§cНе удалось создать или загрузить мир!");
            return true;
        }

        JailLogic.monkeys_shreaking.add(monkey_name);

        Location teleportLocation = new Location(myWorld, 8.5, 64, 8.5, -90, 0);
        PluginManager pM = MonkeyJail.getInstance().getServer().getPluginManager();

        ShreakingListener shreakingListener = new ShreakingListener();
        ShreakingListener.monkey_names.add(player_monkey.getName());
        pM.registerEvents(shreakingListener, MonkeyJail.getInstance());

        Location playerLocation = player.getLocation();
        Location monkeyLocation = player_monkey.getLocation();
        shreakLocations.put(player.getName(),playerLocation);
        shreakLocations.put(player_monkey.getName(),monkeyLocation);

        player_monkey.teleport(teleportLocation);

        player.teleport(new Location(myWorld, 7, 64, 4,0,0));

        isTiny = true;

        player_monkey.setSneaking(true);
        BossBar halfBar = BossBar.bossBar(Component.text("Осталось 10 секунд"), 1f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_10);
        player_monkey.showBossBar(halfBar);
        player.showBossBar(halfBar);

        new BukkitRunnable() {
            int secondsLeft = 10;
            @Override
            public void run() {
                if (secondsLeft <= 0)
                {
                    JailLogic.monkeys_shreaking.remove(monkey_name);
                    player.sendMessage("§aВремя вышло!");
                    player_monkey.setSneaking(false);
                    player.teleport(playerLocation);
                    player_monkey.teleport(monkeyLocation);
                    HandlerList.unregisterAll(shreakingListener);
                    shreakLocations.remove(player.getName());
                    shreakLocations.remove(player_monkey.getName());

                    player.hideBossBar(halfBar);
                    player_monkey.hideBossBar(halfBar);

                    isTiny = false;
                    this.cancel();
                    return;
                }
                halfBar.progress((float)secondsLeft/10f);
                halfBar.name(Component.text("Осталось "+secondsLeft+" секунд"));
                secondsLeft--;
            }
        }.runTaskTimer(MonkeyJail.getInstance(), 0L, 20L); // 0 тиков задержка, 20 тиков = 1 секунда

        return false;
    }

    boolean jailMonkeyCommandExecutor(String[] args, Player player){
        //0 - jail
        //1 - ник
        //2 - название тюрьмы
        //3 - время
        //4-... - причина
        if(args.length<3 || args[1]==null){
            player.sendMessage("§cУкажите все аргументы!");
            return false;
        }
        if(DBM.isMonkeyInJail(args[1])){
            DBM.removeMonkey(args[1]);
            player.sendMessage("§cОбезьяна уже есть в этой тюрьме, пересадили с новым сроком");
        }
        if(!JailLogic.jails.containsKey(args[2])){
            player.sendMessage("§cТакой тюрьмы не существует!");
            return false;
        }

        long time_left = -1;
        try{
            time_left = Integer.parseInt(args[3]);
        }
        catch(Exception e){
            getLogger().warning("Не удалось запарсить время");
            return false;
        }
        getLogger().info("jailCommand 1");
        if(MonkeyJail.skinsRestorerAPI==null){return false;}
        getLogger().info("jailCommand 2");
        Player player_monkey = Bukkit.getPlayer(args[1]);
        try {
            if (player_monkey!=null) {
                getLogger().info("jailCommand 3");
                JailCommands.setSkinFromUrl(Bukkit.getPlayer(player_monkey.getName()), "http://textures.minecraft.net/texture/af20e8affb49949274a61ad7cf3da9f83026abad7e184d184109ff86785bb6f5");
                Bukkit.broadcastMessage("§c"+player.getName()+" посадил обезьяну " + args[1] + " в зоопарк \""+ args[2]+"\" на "+(time_left/20)+" секунд по причине: " + String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
            }
            else{
                Bukkit.broadcastMessage("§c"+player.getName()+" посадил в оффлайне обезьяну " + args[1] + " в зоопарк \""+ args[2]+"\" на "+(time_left/20)+" секунд по причине: " + String.join(" ", Arrays.copyOfRange(args, 4, args.length)));

            }
        }
        catch(Exception e ){
            getLogger().warning("Не удалось получить скин игрока");
        }


        DBM.addMonkey(args[2],args[1],time_left, player.getName(),String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
        new JailLogic().updateMonkeyList();

        return true;
    }







    //unjail
    boolean unJailMonkeyCommandExecutor(String[] args, Player player){
        //0 - unjail
        //1 - ник
        //2 - тюрьма
        //3 - причина
        getLogger().info("Прошёл в разобезьянник 1");
        if(args.length<2 || args[1]==null){
            getLogger().info("Прошёл в разобезьянник 2");
            player.sendMessage("§cУкажите ник и название тюрьмы!");
            return false;
        }


        getLogger().info("Прошёл в разобезьянник 3");
        if(JailLogic.monkeyList.containsKey(args[1])){
            getLogger().info("Время обезьяны до: "+JailLogic.monkeyList.get(args[1]).time_left);
            if(JailLogic.monkeyList.get(args[1]).time_left<=0){
                return false;
            }


            getLogger().info("Прошёл в разобезьянник 4");
            //DBM.removeMonkey(args[1]);
            if(JailLogic.monkeyList.get(args[1]).time_left>0) {
                Bukkit.broadcastMessage("§c" + player.getName() + " выпустил обезьяну " + args[1] + " из зоопарка" + ((args.length == 4 && args[3] != null) ? (" по причине: " + String.join(" ", Arrays.copyOfRange(args, 3, args.length))) : "") + ".");

                //Bukkit.getPlayer(args[1]).teleport(Bukkit.getWorld("world").getSpawnLocation());
                JailLogic.monkeyList.get(args[1]).time_left = 0;
                getLogger().info("Время обезьяны после: "+JailLogic.monkeyList.get(args[1]).time_left);

                JailLogic.removeFromMonkeys(args[1]);

            }

        }


        return true;
    }







    boolean createCommandExecutor(String[] args, Player player){
        if(args.length<2){
            player.sendMessage("§cУкажите аргумент!");
            return false;
        }
        getLogger().info("0: "+args[0]+", 1: "+args[1]);
        switch(args[1]) {
            case "help": {
                openHelpBook(player);
                return true;
            }
            case "start": {
                createStartCommandExecutor(player, null);
                return true;
            }

            case "setFA": {
                setFACommandExecutor(player);
                return true;
            }
            case "setSA": {
                createSetSACommandExecutor(player);
                return true;
            }

            case "setHeight": {
                createSetHeightCommandExecutor(args, player);
                return true;
            }
            case "show": {
                showCommandExecutor(player, true);
                return true;
            }
            case "stop": {
                stopProcess(player.getName());
                return true;
            }
            case "setName": {
                createSetNameCommandExecutor(args, player);
                return true;
            }
            case "removeB": {
                createRemoveBCommandExecutor(args, player);
                return true;
            }
            case "addB": {
                createAddBCommandExecutor(args, player);
                return true;
            }
            case "done": {
                createDoneCommandExecutor(player, false);
                return true;
            }
            case "setSB": {
                createSetSBCommandExecutor(player);
                return true;
            }
            default: {
                player.sendMessage("§cТакой команды нет!");
                return true;
            }
        }

    }


    boolean editJailCommandExecutor(String[] args, Player player){
        if(args.length<3){
            player.sendMessage("§cУкажите аргумент!");
            return false;
        }

        //JailClass jail = DBM.loadJail(args[2]);
        String jail_name = args[1];

        //getLogger().info("0: "+args[0]+", 1: "+args[1]);
        switch(args[2]) {
            case "help": {
                openHelpBook(player);
                return true;
            }
            case "start": {
                createStartCommandExecutor(player, jail_name);
                return true;
            }
            case "setFA": {
                setFACommandExecutor(player);
                return true;
            }
            case "setSA": {
                createSetSACommandExecutor(player);
                return true;
            }
            case "setHeight": {
                createSetHeightCommandExecutor(args, player);
                return true;
            }
            case "show": {
                showCommandExecutor(player, true);
                return true;
            }
            case "stop": {
                stopProcess(player.getName());
                return true;
            }
            case "setName": {
                createSetNameCommandExecutor(args, player);
                return true;
            }
            case "removeB": {
                createRemoveBCommandExecutor(args, player);
                return true;
            }
            case "addB": {
                createAddBCommandExecutor(args, player);
                return true;
            }
            case "done": {
                createDoneCommandExecutor(player, true);
                return true;
            }
            case "setSB": {
                createSetSBCommandExecutor(player);
                return true;
            }
            default: {
                player.sendMessage("§cТакой команды нет!");
                return true;
            }
        }
    }


    // /monkey create start
    public boolean createStartCommandExecutor(Player player, String jail_name) {
        if (jailsCreationProcesses.containsKey(player.getName())) {
            player.sendMessage("§cВы уже создаёте/редактируете тюрьму! Можете остановить процесс командой /monkey createJail/editJail stop");
            return false;
        }

        JailProcessClass jailProcess;
        openHelpBook(player);
        if (jail_name == null) {
            jailProcess = new JailProcessClass();
            jailProcess.world = player.getWorld().getName();
            player.sendMessage("Открыть меню помощи /monkey createJail help");
        } else {
            jailProcess = new JailProcessClass(DBM.loadJail(jail_name));
            jailProcess.isShowBorder = true;
            showJailBorder(jailProcess);
        }
        jailsCreationProcesses.put(player.getName(),jailProcess);

        return true;
    }

    // /monkey create setFA
    public boolean setFACommandExecutor(Player player){
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if (jailProcess!=null) {
            Location pL = player.getLocation();
            int x = (int) Math.floor(pL.getX());
            int y = (int) Math.floor(pL.getY());
            int z = (int) Math.floor(pL.getZ());
            jailProcess.angle1 = new BlockPosClass(x, y, z);
            if (jailProcess.angle2 == null) {
                jailProcess.angle2 = jailProcess.angle1;
            } else {
                jailProcess.angle2.y = jailProcess.angle1.y;
            }
            jailProcess.blocks.clear();
            getJailBlocks(jailProcess);
            showJailBorder(jailProcess);
            player.sendMessage("Первый угол успешно установлен");
            return true;
        } else {
            player.sendMessage("§cНачните процесс создания/редактирования тюрьмы!");
        }
        return false;
    }

    // /monkey create setSA
    public boolean createSetSACommandExecutor(Player player){
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if (jailProcess!=null) {
            Location pL = player.getLocation();
            int x = (int) Math.floor(pL.getX());
            int y = (int) Math.floor(pL.getY());
            int z = (int) Math.floor(pL.getZ());
            jailProcess.angle2 = new BlockPosClass(x, y, z);
            if (jailProcess.angle1 == null) {
                jailProcess.angle1 = jailProcess.angle2;
            } else {
                jailProcess.angle2.y = jailProcess.angle1.y;
            }
            jailProcess.blocks.clear();

            //player.sendMessage("Если хотите расширить/сократить площадь тюрьмы, встаньте на нужный блок и напишите /monkey create removeBlock.");
            //player.sendMessage("Чтобы установить высоту тюрьмы, напишите /monkey create setHeight <число>.");
            //player.sendMessage("Если всё устраивает - напишите /monkey create done.");
            getJailBlocks(jailProcess);
            showJailBorder(jailProcess);
            player.sendMessage("Второй угол успешно установлен");
            return true;
        } else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
        }
        return false;
    }


    // /monkey setHeight
    public boolean createSetHeightCommandExecutor(String[] args, Player player){
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if (jailProcess!=null) {
            if (args.length == 3) {
                int number = 0;
                try {
                    number = Integer.parseInt(args[2]);
                }
                catch (Exception e) {
                    getLogger().warning("Не удалось преобразовать строку" + args[2]);
                }
                jailProcess.angle2.y += number;
                //jailProcess.height = number;
                extendHeight(jailProcess,number);
                showJailBorder(jailProcess);

            }
            player.sendMessage("Высота успешно установлена");
            return true;
        } else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
        }
        showJailBorder(jailProcess);
        return false;
    }

    // /monkey create show
    public boolean showCommandExecutor(Player player, boolean isForce){
        //getLogger().info("Вошёл в show");
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if (jailProcess.isShowBorder) {
            //getLogger().info("Вошёл в true");
            jailProcess.isShowBorder = false;
            hideJailBorder(jailProcess);
        } else {
            //getLogger().info("Вошёл в false");
            jailProcess.isShowBorder = true;
            showJailBorder(jailProcess);

        }
        return true;
    }


    // /monkey create setName
    public boolean createSetNameCommandExecutor(String[] args, Player player){
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if (jailProcess!=null) {
            if (args.length > 2 && args[2]!=null) {
                if(DBM.isJailExists(args[2])){
                    player.sendMessage("§cТакая тюрьма уже существует!");
                    return false;
                }
                jailProcess.jail_name = args[2];
                player.sendMessage("Установлено имя "+args[2]);

                return true;
            }
            else{
                player.sendMessage("§cНеверное количество аргументов");
                return false;
            }
        } else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
        }
        return false;
    }

    // /monkey create removeB
    public boolean createRemoveBCommandExecutor(String[] args, Player player) {
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if (jailProcess != null) {
            Location location = player.getLocation();
            int x = (int) Math.floor(location.getX());
            int y = (int) Math.floor(location.getY());
            int z = (int) Math.floor(location.getZ());

            BlockPosClass target = new BlockPosClass(x, y, z);
            if (jailProcess.blocks.remove(target)) {
                BlockDisplay display = jailProcess.blocksDisplay.get(target);
                if (display != null) {
                    display.remove();
                    jailProcess.blocksDisplay.remove(target);
                    player.sendMessage("Блок удалён");

                }
                return true;
            }

            player.sendMessage("§cЭтого блока нет в тюрьме");
        }
        else {
            player.sendMessage("§cНачните процесс создания/редактирования тюрьмы!");
        }
        return false;
    }

    // /monkey create addB
    public boolean createAddBCommandExecutor(String[] args, Player player) {
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if (jailProcess != null) {
        Location location = player.getLocation();
        int x = (int) Math.floor(location.getX());
        int y = (int) Math.floor(location.getY());
        int z = (int) Math.floor(location.getZ());
        for (BlockPosClass block : jailProcess.blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                player.sendMessage("§cЭтот блок уже есть в тюрьме");
                return false;
            }
        }
        player.sendMessage("Блок добавлен");
        jailProcess.blocks.add(new BlockPosClass(x, y, z));
        showJailBorder(jailProcess);
    }
        else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
        }
        return true;
    }


    // /monkey create done
    public boolean createDoneCommandExecutor(Player player, boolean isEdit){
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if(jailProcess==null){
            player.sendMessage("§cОшибка. Начните создание/редактирование тюрьмы");
            return false;
        }
        if(jailProcess.jail_name !=null && jailProcess.world!=null && jailProcess.blocks!=null && jailProcess.spawnBlock!=null){
            jailProcess.creatorName=player.getName();
            addJailToDataBase(jailProcess,isEdit);

            stopProcess(player.getName());
            new JailLogic().updateJailList();
            player.sendMessage("Успешно создана тюрьма "+jailProcess.jail_name + " на координатах " + jailProcess.spawnBlock.x + " " + jailProcess.spawnBlock.y + " " + jailProcess.spawnBlock.z);

            return true;
        }
        else{
            if(jailProcess.world==null){
                player.sendMessage("§cОшибка. Не указан мир");
            }
            if(jailProcess.blocks==null){
                player.sendMessage("§cОшибка. Нет блоков");
            }
            if(jailProcess.spawnBlock==null){
                player.sendMessage("§cОшибка. Нет позиции спавна");
            }
            if(jailProcess.jail_name==null){
                player.sendMessage("§cОшибка. Нет имени тюрьмы");

            }
            return false;
        }
    }


    // /monkey create setSB
    public boolean createSetSBCommandExecutor(Player player){
        Location location = player.getLocation();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if(jailProcess!=null) {
            int x = (int) Math.floor(location.getX());
            int y = (int) Math.floor(location.getY());
            int z = (int) Math.floor(location.getZ());
            jailProcess.spawnBlock = new BlockPosClass(x, y, z);
            player.sendMessage("Спавн-блок установлен");

            return true;
        }
        else{
            player.sendMessage("§cОшибка. Начните процесс создания/редактирования тюрьмы");
            return false;
        }
    }



    void openHelpBook(Player player){
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            // 2. Add content and metadata
            meta.setTitle("Jail create help");
            meta.setAuthor("dedushka_1");
            meta.addPage("\"§b§l...§r\" это \"/monkey createJail\"\n" +
                    "You can open this book with §b§l...help§r.\n" +
                    "§lTo create jail:\n" +
                    "§l1.§c§l!!!§r Start: §b§l...start§r\n" +
                    "§l2.§c§l!!!§r Create floor: §b§l...setFirstAngle§r and §b§l...setSecondAngle§r. Here you need to make HORIZONTAL rectangle\n");
            meta.addPage("§l3.§r Change shape: §b§l...addB§r and §b§l...removeB§r. Your pos = block\n"+
                    "§l4.§r Set height: §b§l...setHeight <number>§r\n" +
                    "§l5.§r Set name: §b§l...setName <name>§r\n" +
                    "§l6.§c§l!!!§r Set spawn block: §b§l...setSB§r. Your pos = block\n" +
                    "§l7.§c§l!!!§r Complete: §b§l...done§r.\n" +
                    "§lForce stop: §b§l...stop§r\n" +
                    "§lShow/hide border: §b§l...show§r");

            book.setItemMeta(meta);

            // 3. Open the GUI for the player
            player.openBook(book);
        }
    }


    public void extendHeight(JailProcessClass jailProcess, int height){

        int maxHeight = jailProcess.blocks.stream()
                .mapToInt(block -> block.y)
                .min()
                .orElse(0);

        //getLogger().info("max height: "+maxHeight);
        Iterator<BlockPosClass> it = jailProcess.blocks.iterator();
        while (it.hasNext()) {
            BlockPosClass block = it.next();
            if (block.y > maxHeight) {
                BlockDisplay display = jailProcess.blocksDisplay.get(block);
                if (display != null) {
                    display.remove();
                    jailProcess.blocksDisplay.remove(block);
                }
                it.remove();
            }
        }
        Set<BlockPosClass> newBlocks = new HashSet<>();
        for (BlockPosClass blockPos : jailProcess.blocks) {
            for (int j = 1; j < height; j++) {
                newBlocks.add(new BlockPosClass(blockPos.x, blockPos.y + j, blockPos.z));
            }
        }
        jailProcess.blocks.addAll(newBlocks);
    }

    public void getJailBlocks(JailProcessClass jailProcess) {

        if (jailProcess.angle1 != null && jailProcess.angle2 != null) {
            int minX = Math.min(jailProcess.angle1.x, jailProcess.angle2.x);
            int minY = Math.min(jailProcess.angle1.y, jailProcess.angle2.y);
            int minZ = Math.min(jailProcess.angle1.z, jailProcess.angle2.z);
            int maxX = Math.max(jailProcess.angle1.x, jailProcess.angle2.x);
            int maxY = Math.max(jailProcess.angle1.y, jailProcess.angle2.y);
            int maxZ = Math.max(jailProcess.angle1.z, jailProcess.angle2.z);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        jailProcess.blocks.add(new BlockPosClass(x, y, z));
                    }
                }
            }

            getLogger().info("Всего блоков в тюрьме: " + jailProcess.blocks.size());
        } else {
            getLogger().warning("Какой-то угол null");
        }
    }

    public void hideJailBorder(JailProcessClass jailProcess){
        //getLogger().info("отключение границ. Размер списка: "+blocksDisplay.size());
        if(jailProcess.blocksDisplay!=null) {
            for (BlockDisplay blockDisplay : jailProcess.blocksDisplay.values()) {
                blockDisplay.remove();
            }
            jailProcess.blocksDisplay.clear();
        }
    }


    //показать границы тюрьмы
    public void showJailBorder(JailProcessClass jailProcess) {
        if (jailProcess.isShowBorder) {
            hideJailBorder(jailProcess);
            getLogger().info("Включение границ. Размер списка блоков(не дисплеев): "+jailProcess.blocks.size());
                //blocksDisplay = new HashMap<>();
            World jPWorld = Bukkit.getWorld(jailProcess.world);
            for (BlockPosClass block : jailProcess.blocks) {
                BlockDisplay display = jPWorld.spawn(
                        new Location(jPWorld, block.x, block.y, block.z), BlockDisplay.class
                );
                display.setBlock(Material.RED_STAINED_GLASS.createBlockData());
                jailProcess.blocksDisplay.put(block, display);
            }
            getLogger().info("Границы включены. Размер списка дисплеев: "+jailProcess.blocksDisplay.size());
        }
        else{
            Bukkit.getPlayer(jailProcess.creatorName).sendMessage("Где-то уже показывается тюрьма");
        }
    }

    //закончить процесс
    public void stopProcess(String username){
        getLogger().info("Остановка процесса");
        if(username!=null) {
            JailProcessClass jailProcess = jailsCreationProcesses.get(username);
            if (jailProcess != null) {
                hideJailBorder(jailProcess);
                jailsCreationProcesses.remove(username);
            }
            else{
                Bukkit.getPlayer(username).sendMessage("§cОшибка. Нет тюрем");
            }
        }
        else{
            for(JailProcessClass jailProcess : jailsCreationProcesses.values()){
                //Bukkit.getPlayer(username).sendMessage("§cОшибка. Нет тюрем");
                hideJailBorder(jailProcess);
                getLogger().info("Итерация остановки");
            }
            jailsCreationProcesses.clear();
        }

    }


    //добавить тюрьму в БД
    public void addJailToDataBase(JailProcessClass jailProcess, boolean isEdit){

            if(isEdit){
                DBM.saveJail(jailProcess,isEdit);
            }
            else{
                DBM.saveJail(jailProcess,isEdit);
            }
    }

    public static void setSkinFromUrl(Player player, String url) {
        getLogger().info("Начал устанавливать скин");

        MineSkinAPI mineSkinAPI = MonkeyJail.skinsRestorerAPI.getMineSkinAPI();
        // Generate skin from URL (use CLASSIC or SLIM)
        try {
            MineSkinResponse response = mineSkinAPI.genSkin(url, SkinVariant.CLASSIC);
            SkinProperty skinProperty = response.getProperty();
            // Apply directly to player
            MonkeyJail.skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, skinProperty);
        }
        catch(Exception e){
            getLogger().info("Не удалось установить скин");
        }
    }

    public static void tpAllFromShreakingMachine(){
       for(String username : shreakLocations.keySet()){
           if(Bukkit.getPlayer(username).isOnline()) {
               Bukkit.getPlayer(username).teleport(shreakLocations.get(username));
           }
       }
    }
}
