package me.dedushka.monkeyJail.Classes;

import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JailProcessClass extends JailClass{
    public BlockPosClass angle1;
    public BlockPosClass angle2;
    public boolean isShowBorder = true;
    public HashMap<BlockPosClass, BlockDisplay> blocksDisplay = new HashMap<>();


    public JailProcessClass(){}
    public JailProcessClass(JailClass jail) {
        super(jail.jail_id, jail.jail_name, jail.world,
                jail.creatorName, jail.blocks, jail.spawnBlock);
    }
}
