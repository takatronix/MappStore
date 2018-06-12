package red.man10.mappstore;

import net.minecraft.server.v1_12_R1.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import red.man10.mappstore.apps.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class MappStorePlugin extends JavaPlugin  implements Listener {



    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents (this,this);
        getCommand("mapp").setExecutor(new MappStoreCommand(this));
        getCommand("map").setExecutor(new MappStoreCommand(this));


        saveDefaultConfig();


        createDefaultImages();

        //////////////////////////////////////
        //    Initialize map system
        MappRenderer.setup(this);

        //////////////////////////////////////
        //      Register your mApp
        ClockApp.register();
        HelloApp.register();
        YourMappApp.register();
        DrawApp.register();
        BallApp.register();
        MazeApp.register();
        WeatherApp.register();
        BalanceApp.register();
        AnalogClockApp.register();
        PianoApp.register();
        BreakoutApp.register();
        CalcApp.register();
        ServerStatApp.register();



        //      call init
        MappRenderer.initAllMaps();


    }






    public void createDefaultImages() {
        try {

            //      画像フォルダがなければ作成
            File file = new File(getDataFolder(),  "images");
            if (!file.exists()){
                file.mkdirs();
                saveResource("images",false);
            }


            saveResource("images/monsterball.png",false);
            saveResource("images/cursor.png",false);
            saveResource("images/piano.png",false);


            saveResource("images/maze/maze_goal.png",false);
            saveResource("images/maze/maze_player.png",false);
            saveResource("images/maze/maze_button.png",false);
            saveResource("images/maze/maze_wall_2.png",false);
            saveResource("images/maze/maze_wall_1.png",false);

            saveResource("images/mcclock/mcclock_rainy.png",false);
            saveResource("images/mcclock/mcclock_sunny.png",false);

            saveResource("images/analog_clock/analog_clock.png",false);

            saveResource("images/breakout/breakout_background.png",false);
            saveResource("images/breakout/breakout_logo.png",false);


        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    ///////////////////////////////////////////
    //      Get map app
    public boolean giveMap(Player p, String mappName){
        ItemStack map = MappRenderer.getMapItem(this,mappName);
        if(map == null){
            p.sendMessage("§2[Error]No '"+mappName+"' map application.");
            showList(p);
            return false;
        }
        p.getInventory().addItem(map);
        MappRenderer.updateAll();
        return true;
    }

    public boolean sendMap(Player p, String mapIDString){

        int mapID = Integer.parseInt(mapIDString);

        //     mapIDから新規にマップを作成する
        MapView map = Bukkit.getMap((short) mapID);
        if (map == null) {
            p.sendMessage("Map not found");
            map = Bukkit.createMap(Bukkit.getWorlds().get(0));
        }else{
            p.sendMessage("Map found");
        }


        p.sendMap(map);

        return true;
    }


    //      show List
    public boolean showList(Player p){
        String appString = "loaded apps:";
        List<String> apps = MappRenderer.getAppList();
        for(String s : apps){
            appString += "'"+s+"'";
        }
        p.sendMessage(apps.toString());
        return true;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
