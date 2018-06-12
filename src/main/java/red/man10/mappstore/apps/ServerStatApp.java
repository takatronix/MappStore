package red.man10.mappstore.apps;


import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import red.man10.mappstore.MappRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/////////////////////////////////////////////////////////
//      created by takatronix.com
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////


public class ServerStatApp extends MappApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    final static String appName = "stat";   //       <- please change this

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 20;


    static ArrayList<Double> tpsList = new ArrayList<>();
    static ArrayList<Integer> loginCountList = new ArrayList<>();


    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register(){

        ///////////////////////////////////////////////////////////////////////
        //    When maps is initialized
        //    マップが初期化されたとき
        MappRenderer.init(appName, (String key, int mapId) ->{

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });


        ///////////////////////////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

             // Clear screen (画面消去)
              g.setColor(Color.BLACK);
              g.fillRect(0,0,128,128);

            g.setColor(Color.YELLOW);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));


            Double tps = MinecraftServer.getServer().recentTps[0];

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });


        /////////////////////////////////////////////////////////////////////////////
        //      Events when mapapp is placed in item_frame.
        //      マップをアイテムフレームに配置した時のイベント
        /////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////
        //  Button (nearby map) clicked event / ボタンが押された時の処理
        MappRenderer.buttonEvent(appName, (String key, int mapId,Player player) -> {

            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D g = MappRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

            //  clear screen  　
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);

            //    true -> call drawing logic / trueで描画ロジックがコールされます
            return true;
        });

        /////////////////////////////////////////////////
        //      Display touch event
        //      ディスプレイがタッチされた時の処理
        MappRenderer.displayTouchEvent(appName, (String key, int mapId, Player player, int x, int y) -> {

            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D gr = MappRenderer.getGraphics(mapId);
            if(gr == null){
                return false;
            }

            gr.setColor(Color.RED);
            gr.drawLine(x,y,x,y);

            //    true -> call drawing logic :描画更新
            return true;
        });


        MappRenderer.plateEvent(appName, (String key, int mapId,Player player) -> {

            return true;
        });



        ////////////////////////////////////
        //      Jump  /　ジャンプ
        MappRenderer.playerJumpEvent(appName,(String key,int mapId,Player player) ->{

           // player.sendMessage("showCursor");
            MappRenderer.showCursor(mapId);
            //    true -> call drawing logic :描画更新
            return true;
        });

        ////////////////////////////////////
        //      Sneak  /　スニーク
        MappRenderer.playerSneakEvent(appName,(String key,int mapId,Player player,boolean isSneaking) ->{

            if(isSneaking){
                MappRenderer.showCursor(mapId);
            }else{

            }
            return true; //    true -> call drawing logic :描画更新
        });

        ///////////////////////////////////////
        //  Pitch&Velocity  /　上下向き&速度
        MappRenderer.playerPitchEvent(appName,(String key,int mapId,Player player,double angle,double velocity) ->{

            return true; //    true -> call drawing logic :描画更新
        });

        ///////////////////////////////////////
        //  Yaw&Velocity  /　左右向き&速度
        MappRenderer.playerYawEvent(appName,(String key,int mapId,Player player,double angle,double velocity) ->{

            return true; //    true -> call drawing logic :描画更新
        });



    }


}
