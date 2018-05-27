package red.man10.mappstore.apps;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import red.man10.mappstore.MappRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;
import java.util.HashMap;

/////////////////////////////////////////////////////////
//          mApp default template
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////


//      yourMappApp : please change class name
public class YourMappApp extends MappApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    final static String appName = "yourapp";   //       <- please change this

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


    static FileConfiguration config = MappRenderer.getAppConfig(appName);
    //      MappRenderer.saveAppConfig(appName,config)  で保存


    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register(){

        ///////////////////////////////////////////////////////////////////////
        //    When maps is initialized
        //    マップが初期化されたとき
        MappRenderer.init(appName, (String key, int mapId) ->{

            //      Configファイルに保存する
            config.set("init",1);
            MappRenderer.saveAppConfig(appName,config);



            return true;  //  true -> update map / trueでマップに画像が転送されます
        });


        ///////////////////////////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            //  Clear screen (画面消去)
            //  g.setColor(Color.BLACK);
            //  g.fillRect(0,0,128,128);

            g.setColor(Color.YELLOW);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Your App: "+appName,10,70);

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });


        /////////////////////////////////////////////////////////////////////////////
        //      Events when mapapp is placed in item_frame.
        //      マップをアイテムフレームに配置した時のイベント
        /////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////
        //  Button (nearby map) clicked event / ボタンが押された時の処理
        MappRenderer.buttonEvent(appName, (String key, int mapId,Player player) -> {

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

            player.sendMessage("showCursor");
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
