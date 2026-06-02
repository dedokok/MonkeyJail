package me.dedushka.monkeyJail.Classes;

public class MonkeyClass {
    public int id;
    public String jail_name;
    public String username;
    public long time_left;
    public String admin_username;
    public String reason;
    MonkeyClass(){};
    public MonkeyClass(int id, String jail_name,String username, long time_left, String admin_username, String reason){
        this.id=id;
        this.jail_name=jail_name;
        this.username = username;
        this.time_left=time_left;
        this.admin_username=admin_username;
        this.reason=reason;
    }
}
