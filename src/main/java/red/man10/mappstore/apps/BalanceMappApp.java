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


public class BalanceMappApp extends MappApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    final static String appName = "balance";   //       <- please change this

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 0;


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


            Graphics2D g = MappRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

            //  clear screen  　
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);


            g.setColor(Color.RED);

            g.drawString("Set map in item frame",0,60);
            g.drawString("   Touch to start",0,80);

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });


        ///////////////////////////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

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

            g.setColor(Color.YELLOW);

            g.setFont(new Font( "SansSerif", Font.PLAIN,12));
            g.drawString("Touch display",10,40);
            g.drawString(" to show your balance.",10,60);

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
            Graphics2D g = MappRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);



            //      残高を得る
            double balance = MappRenderer.vaultManager.getBalance(player.getUniqueId());

            g.setColor(Color.RED);

            g.setFont(new Font( "SansSerif", Font.BOLD,12));
            g.drawString("Your balance:",10,40);


            g.setColor(Color.YELLOW);
            g.setFont(new Font( "SansSerif", Font.PLAIN,10));
            g.drawString(String.format("$%,.0f",balance),10,60);


            g.setColor(Color.blue);
            g.drawString(" "+ player.getName(),10,80);

            //    true -> call drawing logic :描画更新
            return true;
        });


    }


}
