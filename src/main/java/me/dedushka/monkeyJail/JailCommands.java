package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Classes.BlockPosClass;
import me.dedushka.monkeyJail.Classes.JailClass;
import me.dedushka.monkeyJail.Classes.JailProcessClass;
import me.dedushka.monkeyJail.Listeners.EventListener;
import me.dedushka.monkeyJail.Listeners.ShreakingListener;
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
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class JailCommands implements CommandExecutor{
   // static int creatingStage = 0;
   // static String creatorName = null;
    //static BlockPosClass angle1 = null;
    //static BlockPosClass angle2 = null;
    //static String jail_name = null;
   // static int jail_id = -1;
    //static int height = 1;
    //static World world = null;
    //public static Map<BlockPosClass, BlockDisplay> blocksDisplay = new HashMap<>();
    //static Set<BlockPosClass> blocks = null;
    //static BlockPosClass spawnBlock = null;
    static DataBaseManager DBM = new DataBaseManager();
    //private static SkinsRestorer skinsRestorerAPI;
    public static HashMap<String, JailProcessClass> jailsCreationProcesses = new HashMap<>();

    //static boolean isShowBorder = true;
    //static boolean isShowing = false;

    static boolean isTiny = false;

    static private MonkeyJail MJ;

    public JailCommands(MonkeyJail MJ, SkinsRestorer skinsRestorerAPI){
        this.MJ = MJ;
        //this.skinsRestorerAPI = skinsRestorerAPI;
//        Bukkit.getScheduler().runTaskTimer(MJ, () -> {
//
//            //JailCommands jC = new JailCommands();
//            //getLogger().info("Размер списка: "+jC.blocksDisplay.size());
//
//            getLogger().info("Размер списка: "+blocksDisplay.size());
//        }, 0L, 10L);
    }
    public JailCommands(){}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "createjail":{
                    createCommandExecutor(args,player);
                    return true;
                }
                case "jail":{
                    jailMonkeyCommandExecutor(args,player);
                    return true;
                }
                case "unjail":{
                    unJailMonkeyCommandExecutor(args,player);
                    return true;
                }
                case "editjail":{
                    editJailCommandExecutor(args,player);
                    return true;
                }
                case "fuck":{
                    fuckMonkeyCommandExecutor(args,player);
                    return true;
                }
                case "deletejail":{
                    deleteJailCommandExecutor(args,player);
                    return true;
                }
            }
        } else {
            sender.sendMessage("Эта команда только для игроков!");
        }
        return true;
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


    boolean fuckMonkeyCommandExecutor(String[] args, Player player) {
        if (isTiny) {
            player.sendMessage("§cТраходром пока занят, подождите немного");
        }
        //0 - fuck
        //1 - ник
        if (args.length < 2) {
            player.sendMessage("§cУкажите ник обезьяны!");
        }
        String monkey_name = args[1];
        Player player_monkey = Bukkit.getPlayer(monkey_name);
        if (player_monkey != null && !player_monkey.isOnline()) {
            player.sendMessage("§cОбезьяна не в сети!");
        }


        OneChunkWorldManager worldManager = (MJ).getWorldManager();
        World myWorld = worldManager.createWorld("my_custom_world", "shreakmachine");

        if (myWorld == null) {
            player.sendMessage("§cНе удалось создать или загрузить мир!");
            return true;
        }

        // Телепортируем игрока
        JailLogic.monkeys_shreaking.add(monkey_name);
        Location teleportLocation = new Location(myWorld, 8.5, 64, 8.5, -90, 0);
        PluginManager pM = MJ.getServer().getPluginManager();
        //final EventListener[] eventListener = {new EventListener()};
        ShreakingListener shreakingListener = new ShreakingListener();
        ShreakingListener.monkey_names.add(player_monkey.getName());
        pM.registerEvents(shreakingListener, MJ);

        Location playerLocation = player.getLocation();
        Location monkeyLocation = player_monkey.getLocation();

        player_monkey.teleport(teleportLocation);

        player.teleport(new Location(myWorld, 10, 64, 8));





        isTiny = true;
        new BukkitRunnable() {
            int cc = 200;

            @Override
            public void run() {
                if (cc <= 0) {
                    this.cancel();
                    return;
                }
                cc--;

                if (player_monkey.isOnline()) {



                    player.hidePlayer(MJ, player);
                    player.showPlayer(MJ, player);
                    player_monkey.setSneaking(true);
                }
            }


        }.runTaskTimer(MJ, 0L, 1L);
        int count = 200;
        new BukkitRunnable() {
            int secondsLeft = 10;

            @Override
            public void run() {
                if (secondsLeft <= 0)
                {

                    JailLogic.monkeys_shreaking.remove(monkey_name);

                    // Таймер закончился
                    player.sendMessage("§aВремя вышло!");
                    player.teleport(playerLocation);
                    player_monkey.teleport(monkeyLocation);
                    HandlerList.unregisterAll(shreakingListener);
                    //HandlerList.unregisterAll(eventListener[0]);
                   // eventListener[0] =null;

                    // Выгружаем мир (true - сохранить перед выгрузкой)

                   // Bukkit.unloadWorld(myWorld, true);

                   // File worldFolder = myWorld.getWorldFolder();
                    //deleteDirectory(worldFolder);

                    isTiny = false;
                    this.cancel();
                    return;
                }

                // Каждую секунду
                player.sendMessage("§eОсталось: §6" + secondsLeft + " §eсекунд");
                secondsLeft--;
            }
        }.runTaskTimer(MJ, 0L, 20L); // 0 тиков задержка, 20 тиков = 1 секунда

        return false;
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
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

        try {

            Player player_monkey = Bukkit.getPlayer(args[1]);
            if(player_monkey==null){return false;}
            getLogger().info("jailCommand 3");
            PlayerStorage playerStorage = MonkeyJail.skinsRestorerAPI.getPlayerStorage();
            Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
                    player_monkey.getUniqueId(),
                    player_monkey.getName()
            );
            SkinProperty sP = property.orElse(null);
            getLogger().info("Property = " + (sP==null ? "null" : "true"));

            JailLogic.skinsHistory.put(player.getName(),sP);
            getLogger().info("Размер skinsHistory в jailCommands: " + JailLogic.skinsHistory.size());

            JailCommands.setSkinFromUrl(Bukkit.getPlayer(player.getName()), "http://textures.minecraft.net/texture/af20e8affb49949274a61ad7cf3da9f83026abad7e184d184109ff86785bb6f5");
        }
        catch(Exception e ){
            getLogger().warning("Не удалось получить скин игрока");
        }


        DBM.addMonkey(args[2],args[1],time_left, player.getName(),String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
        new JailLogic().updateMonkeyList();
        Bukkit.broadcastMessage("§c"+player.getName()+" посадил обезьяну " + args[1] + " в зоопарк \""+ args[2]+"\" на "+(time_left/20)+" секунд по причине: " + String.join(" ", Arrays.copyOfRange(args, 4, args.length)));

        return true;
    }







    //unjail
    boolean unJailMonkeyCommandExecutor(String[] args, Player player){
        //0 - unjail
        //1 - ник
        //2 - тюрьма
        //3 - причина
        getLogger().info("Прошёл в разобезьянник 1");
        if(args.length<3 || args[1]==null || args[2]==null){
            getLogger().info("Прошёл в разобезьянник 2");
            player.sendMessage("§cУкажите ник и название тюрьмы!");
            return false;
        }

        getLogger().info("Прошёл в разобезьянник 3");
        if(DBM.isMonkeyInJail(args[1])){
            getLogger().info("Прошёл в разобезьянник 4");
            //DBM.removeMonkey(args[1]);
            Bukkit.broadcastMessage("§c"+player.getName()+" выпустил обезьяну " + args[1] + " из зоопарка"+((args.length==4 && args[3]!=null) ? (" по причине: " +String.join(" ", Arrays.copyOfRange(args, 3, args.length))) : "." )+".");
            //Bukkit.getPlayer(args[1]).teleport(Bukkit.getWorld("world").getSpawnLocation());
            JailLogic.removeFromMonkeys(args[1]);

        }

        new JailLogic().updateMonkeyList();

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
                return createStartCommandExecutor(player,null);
            }

            case "setFA": {
                return setFACommandExecutor(player);
            }
            case "setSA": {
                return createSetSACommandExecutor(player);
            }

            case "setHeight": {
                return createSetHeightCommandExecutor(args, player);
            }
            case "show": {
                return showCommandExecutor(player,true);
            }
            case "stop": {
                stopProcess(player.getName());
                return true;
            }
            case "setName": {
                return createSetNameCommandExecutor(args, player);
            }
            case "removeB": {
                return createRemoveBCommandExecutor(args,player);
            }
            case "addB":{
                return createAddBCommandExecutor(args,player);
            }
            case "done":{
                return createDoneCommandExecutor(player,false);
            }
            case "setSB":{
                return createSetSBCommandExecutor(player);
            }
            default: {
                player.sendMessage("§cТакого аргумента нет!");
                return false;
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
                return createStartCommandExecutor(player,jail_name);
            }

            case "setFA": {
                return setFACommandExecutor(player);
            }
            case "setSA": {
                return createSetSACommandExecutor(player);
            }

            case "setHeight": {
                return createSetHeightCommandExecutor(args, player);
            }
            case "show": {
                return showCommandExecutor(player, true);
            }
            case "stop": {
                stopProcess(player.getName());
                return true;
            }
            case "setName": {
                return createSetNameCommandExecutor(args, player);
            }
            case "removeB": {
                return createRemoveBCommandExecutor(args,player);
            }
            case "addB":{
                return createAddBCommandExecutor(args,player);
            }
            case "done":{
                return createDoneCommandExecutor(player, true);
            }
            case "setSB":{
                return createSetSBCommandExecutor(player);
            }
            default: {
                player.sendMessage("§cТакого аргумента нет!");
                return false;
            }
        }
    }


    // /monkey create start
    public boolean createStartCommandExecutor(Player player, String jail_name) {
        if (jailsCreationProcesses.containsKey(player.getName())) {
            player.sendMessage("§cВы уже создаёте/редактируете тюрьму! Можете остановить процесс командой /monkey createJail/editJail stop");
        }

        JailProcessClass jailProcess;
        openHelpBook(player);
        if (jail_name == null) {
            jailProcess = new JailProcessClass();
            jailProcess.world = player.getWorld().getName();
            player.sendMessage("Сделайте углы ПОЛА тюрьмы /monkey createJail setFA/setSA");
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
            getJailBlocks(jailProcess);
            showJailBorder(jailProcess);
            //player.sendMessage("Сделайте второй угол ПОЛА тюрьмы (чтобы получился горизонтальный прямоугольник) командой /monkey create setSecondAngle");
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
            //player.sendMessage("Если хотите расширить/сократить площадь тюрьмы, встаньте на нужный блок и напишите /monkey create removeBlock.");
            //player.sendMessage("Чтобы установить высоту тюрьмы, напишите /monkey create setHeight <число>.");
            //player.sendMessage("Если всё устраивает - напишите /monkey create done.");
            getJailBlocks(jailProcess);
            showJailBorder(jailProcess);
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

        if(jailProcess!=null){
            player.sendMessage("§cОшибка. Начните создание/редактирование тюрьмы");
            return false;
        }
        if(jailProcess.world!=null && jailProcess.blocks!=null && jailProcess.spawnBlock!=null){
            jailProcess.creatorName=player.getName();
            addJailToDataBase(jailProcess, player,isEdit);

            stopProcess(player.getName());
            new JailLogic().updateJailList();
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
                .max()
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
        jailProcess.blocks = new HashSet<>();

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
                Bukkit.getPlayer(username).sendMessage("§cОшибка. Нет тюрем");
                hideJailBorder(jailProcess);
            }
            jailsCreationProcesses.clear();
        }

    }



    public void addJailToDataBase(JailProcessClass jailProcess, Player player, boolean isEdit){
        try {
            //int jail_id = DBM.getJailCount();
            int jail_id = -1;
            if(isEdit){
                jail_id = jailProcess.jail_id;
                DBM.saveJail(jailProcess,isEdit);
            }
            else{
                jail_id = DBM.saveJail(jailProcess,isEdit);
            }
            DBM.saveJailBlocks(jail_id,jailProcess,isEdit);

        }
        catch(Exception e){
            getLogger().info(e.toString());
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
}
