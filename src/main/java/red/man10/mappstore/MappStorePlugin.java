package red.man10.mappstore;

import net.minecraft.server.v1_12_R1.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class MappStorePlugin extends JavaPlugin  implements Listener {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents (this,this);
        getCommand("mapp").setExecutor(new MappStoreCommand(this));


        //////////////////////////////////////
        //    Initialize map system
        DynamicMapRenderer.setup(this);

        //////////////////////////////////////
        //      Regiser your mapp apps
        DefaultMappApp.register();
        YourMappApp.register();


        BlockPosition bp = new BlockPosition(0,0,0);
    }


    ///////////////////////////////////////////
    //      Get map app
    public boolean giveMap(Player p, String mappName){
        ItemStack map = DynamicMapRenderer.getMapItem(this,mappName);
        if(map == null){
            p.sendMessage("§2[Error]No '"+mappName+"' map application.");
            return false;
        }
        p.getInventory().addItem(map);
        DynamicMapRenderer.updateAll();
        return true;
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
