package red.man10.mappstore;

import net.minecraft.server.v1_12_R1.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.mappstore.apps.*;

import java.io.File;
import java.util.List;

public final class MappStorePlugin extends JavaPlugin  implements Listener {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents (this,this);
        getCommand("mapp").setExecutor(new MappStoreCommand(this));


        saveDefaultConfig();


        createDefaultImages();

        //////////////////////////////////////
        //    Initialize map system
        DynamicMapRenderer.setup(this);

        //////////////////////////////////////
        //      Register your mApp
        ClockMappApp.register();
        HelloMappApp.register();
        YourMappApp.register();
        DrawMappApp.register();
        BallMappApp.register();

    }

    public void createDefaultImages() {
        try {
            File file = new File(getDataFolder(),  "images");
            if (!file.exists()){
                file.mkdirs();
                saveResource("images",false);
                saveResource("images/monsterball.png",false);
              //  plugin.saveResource("newYML.yml", false);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    ///////////////////////////////////////////
    //      Get map app
    public boolean giveMap(Player p, String mappName){
        ItemStack map = DynamicMapRenderer.getMapItem(this,mappName);
        if(map == null){
            p.sendMessage("§2[Error]No '"+mappName+"' map application.");

            String appString = "loaded apps:";
            List<String> apps = DynamicMapRenderer.getAppList();
            for(String s : apps){
                appString += "'"+s+"'";
            }
            p.sendMessage(apps.toString());
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
