package me.dedushka.monkeyJail;

import me.dedushka.monkeyJail.Classes.BlockPosClass;
import me.dedushka.monkeyJail.Classes.JailClass;
import me.dedushka.monkeyJail.Classes.MonkeyClass;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.*;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class DataBaseManager {
    private static Connection connection;
    private static MonkeyJail MJ;

    public DataBaseManager(MonkeyJail MJ){
        this.MJ=MJ;
    }
    public DataBaseManager(){}

    private final Map<Integer, Set<Long>> jailBlocks = new HashMap<>();

    public void createDB(){
        try {
            String url = "jdbc:sqlite:plugins/MonkeyJail/database.db";
            connection = DriverManager.getConnection(url);


            String sql = "CREATE TABLE IF NOT EXISTS jails (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "jail_name TEXT NOT NULL," +
                    "creator_name TEXT NOT NULL, " +
                    "world TEXT NOT NULL, " +
                    "spawn_key INTEGER NOT NULL)";
            connection.prepareStatement(sql).executeUpdate();

            sql = "CREATE TABLE IF NOT EXISTS blocks (" +
                    "block_key INTEGER NOT NULL, " +
                    "jail_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (jail_id, block_key), " +
                    "FOREIGN KEY (jail_id) REFERENCES jails(id) ON DELETE CASCADE)";
            connection.prepareStatement(sql).executeUpdate();

            sql =   "CREATE TABLE IF NOT EXISTS monkeys (id INTEGER PRIMARY KEY AUTOINCREMENT, jail_name TEXT NOT NULL, username TEXT NOT NULL," +
                    "time_left INTEGER, admin_username TEXT, reason TEXT)";
            connection.prepareStatement(sql).executeUpdate();



        }
        catch( SQLException e){
            e.printStackTrace();
        }
    }

    //подключение к бд обезьян
    public void connectDB() {
        try {

            String url = "jdbc:sqlite:plugins/MonkeyJail/database.db";
            connection = DriverManager.getConnection(url);
            //getLogger().info("2 Connection is "+ (connection==null ? true : false));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //добавить обезьяну в БД
    public void addMonkey(String jail_name, String username, long time_left, String admin_username, String reason){
        String sql = "INSERT INTO monkeys (jail_name,username,time_left,admin_username,reason) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, jail_name);
            pstmt.setString(2, username);
            pstmt.setLong(3, time_left);
            pstmt.setString(4, admin_username);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
            //getLogger().info("Добавил запись в обезьянник");
            //getLogger().info("3 Connection is "+ (connection==null ? true : false));
        }
        catch(SQLException e){
            e.printStackTrace();
            //getLogger().info("Ошибка добавления обезьяны");
        }
    }

    //удалить обезьяну из БД
    public void removeMonkey(String username){
        String sql = "DELETE FROM monkeys WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    //получить список обезьян
    public HashMap<String,MonkeyClass> getAllMonkeys(int monkeyAmount, int lastMonkeyId){
        HashMap<String,MonkeyClass> monkeyList = new HashMap<>();
        String sql = "SELECT * FROM monkeys ORDER BY id DESC";
        if(monkeyAmount!=-1){sql+=" LIMIT ?";}
        if(lastMonkeyId!=-1){sql+=" WHERE id < ?";}
        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String jail_name = rs.getString("jail_name");
                String username = rs.getString("username");
                long time_left = rs.getLong("time_left");
                String admin_username = rs.getString("admin_username");
                String reason = rs.getString("reason");

                monkeyList.put(username,new MonkeyClass(jail_name,username, time_left, admin_username, reason));
            }
        } catch (SQLException e) {
            //getLogger().info(e.getMessage());
        }

        return monkeyList;
    }





//    public void loadJailBlocks(int jailId) throws SQLException {
//        Set<Long> blocks = new HashSet<>(1024);
//
//        try (PreparedStatement stmt = db.prepareStatement(
//                "SELECT x, y, z FROM jail_blocks WHERE jail_id = ?")) {
//            stmt.setInt(1, jailId);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                long key = toKey(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
//                blocks.add(key);
//            }
//        }
//
//        jailBlocks.put(jailId, blocks);
//    }

    private long toKey(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    private int xFromKey(long key) {
        int raw = (int)(key >> 38) & 0x3FFFFFF;
        return (raw << 6) >> 6;
    }

    private int zFromKey(long key) {
        int raw = (int)(key >> 12) & 0x3FFFFFF;
        return (raw << 6) >> 6;
    }

    private int yFromKey(long key) {
        int raw = (int) key & 0xFFF;
        return (raw << 20) >> 20;
    }

    public void saveJailBlocks(int jailId, JailClass jail, boolean isEdit) throws SQLException {
        // Сначала удаляем старые блоки этой тюрьмы
        String deleteSql = "DELETE FROM blocks WHERE jail_id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, jailId);
            deleteStmt.executeUpdate();
        }


        String insertSql = "INSERT OR IGNORE INTO blocks (jail_id, block_key) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);

            for (BlockPosClass block : jail.blocks) {
                long key = toKey(block.x, block.y, block.z);
                insertStmt.setInt(1, jailId);
                insertStmt.setLong(2, key);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        }
    }


    public int saveJail(JailClass jail, boolean isEdit) throws SQLException {
        String sql = "";
        if(isEdit){
            sql = "UPDATE jails SET spawn_key = ? WHERE jail_name = ?";
        }
        else {
            sql = "INSERT INTO jails (jail_name, creator_name, world, spawn_key) VALUES (?, ?, ?, ?)";
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if(isEdit) {
                stmt.setLong(1,toKey(jail.spawnBlock.x, jail.spawnBlock.y, jail.spawnBlock.z));
                stmt.setString(2, jail.jail_name);
            }
            else{
                stmt.setString(1, jail.jail_name);
                stmt.setString(2, jail.creatorName);
                stmt.setString(3, jail.world.getName());
                stmt.setLong(4, toKey(jail.spawnBlock.x, jail.spawnBlock.y, jail.spawnBlock.z));
            }
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            int jailId = keys.next() ? keys.getInt(1) : -1;

            saveJailBlocks(jailId, jail, isEdit);
            return jailId;
        }
    }

    // Загрузить все тюрьмы
    public HashMap<String, JailClass> loadAllJails() {
        getLogger().info("Вошёл в loadAllJails");
        HashMap<String, JailClass> jails = new HashMap<>();

        String sql = "SELECT id, jail_name, creator_name, world, spawn_key FROM jails ORDER BY id ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int jail_id       = rs.getInt("id");
                String jail_name = rs.getString("jail_name");
                String creator   = rs.getString("creator_name");
                String worldName = rs.getString("world");
                long spawnKey    = rs.getLong("spawn_key");

                World world = Bukkit.getWorld(worldName);
                BlockPosClass spawn = new BlockPosClass(xFromKey(spawnKey), yFromKey(spawnKey), zFromKey(spawnKey));
                Set<BlockPosClass> blocks = loadJailBlocks(jail_id);

                jails.put(jail_name, new JailClass(jail_id,jail_name, world, creator, blocks, spawn));
            }
        } catch (Exception e) {
            getLogger().info("Ошибка loadAllJails");
        }

        return jails;
    }


    //получить одну тюрьму
    public JailClass loadJail(String jail_name) {
        getLogger().info("Вошёл в loadJail");
        JailClass jail = null;
        getLogger().info("jail_name = " + jail_name);
        String sql = "SELECT id, creator_name, world, spawn_key FROM jails WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jail_name);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int jail_id       = rs.getInt("id");
                String creator_name   = rs.getString("creator_name");
                String world_name = rs.getString("world");
                long spawnKey    = rs.getLong("spawn_key");

                World world = Bukkit.getWorld(world_name);
                BlockPosClass spawn_block = new BlockPosClass(xFromKey(spawnKey), yFromKey(spawnKey), zFromKey(spawnKey));
                Set<BlockPosClass> blocks = loadJailBlocks(jail_id);
                getLogger().info("Получил тюрьму");
                jail = new JailClass(jail_id,jail_name,world,creator_name,blocks,spawn_block);
            }
        } catch (Exception e) {
            getLogger().info("Ошибка loadAllJails");
        }

        return jail;
    }




    //получить айди тюрьмы по имени
    public int getJailID(String jail_name) {
        getLogger().info("Вошёл в getJailID");
        String sql = "SELECT id FROM jails WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jail_name);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            getLogger().info("Ошибка loadAllJails");
        }
        return -1;

    }






    //получить количество тюрем из таблицы blocks
    public ArrayList<String> getJailNames() {
        ArrayList<String>jail_names = new ArrayList<>();
        String sql = "SELECT DISTINCT jail_name FROM jails";
            try {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    jail_names.add(rs.getString("jail_name"));
                }
            }
            catch(Exception e){
                getLogger().warning("Ошибка получения списка тюрем");
            }

        return jail_names;
    }

    // ── Загрузить блоки тюрьмы

    public Set<BlockPosClass> loadJailBlocks(int jailId) throws SQLException {
        Set<BlockPosClass> blocks = new HashSet<>();

        String sql = "SELECT block_key FROM blocks WHERE jail_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, jailId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long key = rs.getLong("block_key");
                blocks.add(new BlockPosClass(xFromKey(key), yFromKey(key), zFromKey(key)));
            }
            getLogger().info("Получил блоки, количество: "+blocks.size());
        }

        return blocks;
    }

    // Удалить тюрьму (блоки удалятся сами)
    public void deleteJail(String jailName) throws SQLException {
        String sql = "DELETE FROM jails WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jailName);
            stmt.executeUpdate();
        }
        sql = "UPDATE monkeys SET time_left = 0 WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jailName);
            stmt.executeUpdate();
        }
    }


    //проверить, есть ли в зоопарке данная обезьяна
    boolean isMonkeyInJail(String username){
        String sql = "SELECT 1 FROM monkeys WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    //getLogger().info("Нашёл руду в БД "+pos_x + " "+pos_y+" "+pos_z);
                    return true;
                }
                return false;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        //getLogger().info("Не нашёл руду в БД "+pos_x + " "+pos_y+" "+pos_z);

        return false;
    }


    //проверить, есть ли уже такая тюрьма
    boolean isJailExists(String jail_name){
        String sql = "SELECT 1 FROM jails WHERE jail_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, jail_name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
                return false;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }




    void updateMonkeyTable(HashMap<String,MonkeyClass> updatedMonkeyList){
        for(MonkeyClass monkey : updatedMonkeyList.values()){
            String sql = "UPDATE monkeys SET time_left = ? WHERE username = ? AND jail_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, monkey.time_left);
                pstmt.setString(1, monkey.username);
                pstmt.setString(2, monkey.jail_name);
                pstmt.executeUpdate();
            }
            catch(SQLException e){
                e.printStackTrace();
            }
        }
    }
}
