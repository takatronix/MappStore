package red.man10.mappstore.apps;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.mappstore.DynamicMapRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;
import java.util.HashMap;

/////////////////////////////////////////////////////////
//          mApp default template
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////



public class YourMappApp extends MappApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    static String appName = "yourapp";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 20;


    ///////////////////////////////
    //     Data
    ///////////////////////////////
    static class MappData{


        //   Add your data here / マップごとに保存するデータはここに追加


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
    //      ユーザーデータ読み込
    static MappData saveData(int mapId,MappData data){
        return hashMap.put(mapId,data);
    }


    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register(){


        ///////////////////////////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        //////////////////////////////////////////////////////////////////////
        DynamicMapRenderer.register( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            //  Clear screen (画面消去)
            //  g.setColor(Color.BLACK);
            //  g.fillRect(0,0,128,128);

            g.setColor(Color.YELLOW);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Your App: "+appName,10,70);

            //  true -> update map / trueでマップに画像が転送されます
            return true;
        });


        /////////////////////////////////////////////////////////////////////////////
        //      マップをアイテムフレームに配置した時のイベント
        /////////////////////////////////////////////////////////////////////////////


        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタンが押された時の処理
        DynamicMapRenderer.registerButtonEvent(appName, (String key, int mapId,Player player) -> {

            ///////////////////////////////////////////////////////////////
            //      mapごとに別々のデータを表示したい場合は
            //      mapIdをキーにハッシュマップにデータを読み込み・保存してください
            /*
            //     load app data / mapIDをキーにをロードする　
            MappData data = loadData(mapId);

            //    save app data
            saveData(mapId,data);
            */

            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D g = DynamicMapRenderer.getGraphics(mapId);
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
        DynamicMapRenderer.registerDisplayTouchEvent(appName, (String key, int mapId, Player player, int x, int y) -> {

            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D gr = DynamicMapRenderer.getGraphics(mapId);
            if(gr == null){
                return false;
            }

            gr.setColor(Color.RED);
            gr.drawLine(x,y,x,y);

            //    true -> call drawing logic :描画更新
            return true;
        });


        /////////////////////////////////////////////////////////////////////////////
        //      Events when player jumped with the map
        //      マップをもった状態のイベント
        /////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////
        //      Sneak  /　ジャンプ
        DynamicMapRenderer.registerPlayerJumpEvent(appName,(String key,int mapId,Player player) ->{

            player.sendMessage("Jumped :" + key + " mapID:"+mapId);

            //    true -> call drawing logic :描画更新
            return true;
        });

        ////////////////////////////////////
        //      Sneak  /　スニーク
        DynamicMapRenderer.registerPlayerSneakEvent(appName,(String key,int mapId,Player player,boolean isSneaking) ->{

            if(isSneaking){
                player.sendMessage("Sneaked: " + key + " mapID:"+mapId);
            }else{
                player.sendMessage("Sneaked off: " + key + " mapID:"+mapId);
            }

            //    true -> call drawing logic :描画更新
            return true;
        });


        ///////////////////////////////////////
        //  Pitch&Velocity  /　上下向き&速度
        DynamicMapRenderer.registerPlayerPitchEvent(appName,(String key,int mapId,Player player,double angle,double velocity) ->{

           // player.sendMessage("pitch:"+angle + " velocity:"+velocity);

            //    true -> call drawing logic :描画更新
            return true;
        });


        ///////////////////////////////////////
        //  Yaw&Velocity  /　左右向き&速度
        DynamicMapRenderer.registerPlayerYawEvent(appName,(String key,int mapId,Player player,double angle,double velocity) ->{

            // player.sendMessage("angle:"+angle + " velocity:"+velocity);

            Graphics2D g = DynamicMapRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }


            int x2 = 64 + (int)(velocity * 1.0);

            g.setColor(Color.black);
            g.fillRect(0,0,128,128);

            g.setColor(Color.red);
            g.drawLine(64,80,x2,80);


            g.drawString("angle:"+(int)angle,10,100);
            //    true -> call drawing logic :描画更新
            return true;
        });



    }


}
