package red.man10.mappstore.apps;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;

/////////////////////////////////////////////////////////
//          mApp default template
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////
//             WeatherMappApp
//
//       It can display the time in Minecraft world
//       and display the weather in mincraft world too!
//
//              created by Ryotackey
//          https://github.com/Ryotackey
/////////////////////////////////////////////////////////

public class WeatherApp extends MappApp {

    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    static String appName = "weather";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 20*10;


    ///////////////////////////////
    //     Data
    ///////////////////////////////
    static class MappData{
        //   Add your data here / マップごとに保存するデータはここに追加
        World world;
    }
    static HashMap<Integer,MappData> hashMap = new  HashMap<Integer,MappData>();

    //      ユーザーデーター保存
    static MappData  loadData(int mapId) {
        MappData data = hashMap.get(mapId);
        if(data == null){
            data = new MappData();
        }
        return data;
    }

    static FileConfiguration config = null;

    //      ユーザーデータ読み込
    static MappData saveData(int mapId,MappData data){
        return hashMap.put(mapId,data);
    }

    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register(){


        ///////////////////////////////////////////////////////////////////////
        //    When maps is initialized
        //    マップが初期化されたとき
        MappRenderer.init(appName, (String key, int mapId) ->{
            Graphics2D g = MappRenderer.getGraphics(mapId);

            g.setColor(Color.black);
            g.fillRect(0,0,128,128);
            g.setColor(Color.red);
            g.drawString(" set in item frame",0,60);
            g.drawString(" touch to start",0,80);

            config = MappRenderer.getAppConfig(appName);
            String worldname  = config.getString("MapID:"+mapId,null);
            if(worldname == null){
                Bukkit.getLogger().info("weather:no world name.");
                return true;
            }

            World world =  Bukkit.getServer().getWorld(worldname);
            if(world != null){
                MappData data = loadData(mapId);
                data.world = world;
                //    save app data
                saveData(mapId,data);
                MappRenderer.updateMap(appName);
            }

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });

        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        MappRenderer.displayTouchEvent(appName, (String key, int mapId, Player player,int x ,int y) -> {


            //  プレイヤーのワールドを保存
            MappData data = loadData(mapId);
            data.world = player.getWorld();
            //    save app data
            saveData(mapId,data);

            config = MappRenderer.getAppConfig(appName);
            //      ワールド情報を保存
            config.set("MapID:"+mapId,data.world.getName());
            //      ワールド保存
            MappRenderer.saveAppConfig(appName,config);


            //    true -> call drawing logic / trueで描画ロジックがコールされます
            return true;
        });

        /////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            //    get world by mapId;
            World w = loadData(mapId).world;
            if (w == null) {
                return false;
            }

            if (!w.hasStorm()) {
                digitalclockDraw(w,Color.WHITE, g, "mcclock_sunny");
            }else {
                digitalclockDraw(w,Color.WHITE, g, "mcclock_rainy");
            }

            return true;
        });
    }

    public static void digitalclockDraw(World w,Color maincolor, Graphics2D g, String path){

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 128, 128);

      //  MappDraw.drawImage(g, "ryotackey_digital", 0, 0, 128, 128);
        MappDraw.drawImage(g, path, 88, 88, 40, 40);

        Format f = new DecimalFormat("00");

        double time = w.getTime();

        double time2 = time/1000.0;
        int hour = (int) Math.floor(time2);

        double time3 = time2-hour;

        int ms = (int) Math.round(time3*1000.0/16.7);

        hour += 6;

        g.setColor(Color.BLACK);
        g.setFont(new Font( "SansSerif", Font.BOLD,40));
        g.drawString(String.valueOf(f.format(hour%24) + ":" + f.format(ms)), 14, 82);

        g.setColor(maincolor);
        g.setFont(new Font( "SansSerif", Font.BOLD,40));
        g.drawString(String.valueOf(f.format(hour%24) + ":" + f.format(ms)), 12, 80);

        g.setColor(Color.BLACK);
        g.setFont(new Font( "SansSerif", Font.BOLD,15));
        g.drawString("MinecraftTime", 12, 40);

        g.setColor(maincolor);
        g.setFont(new Font( "SansSerif", Font.BOLD,15));
        g.drawString("MinecraftTime", 10, 40);

    }

}
