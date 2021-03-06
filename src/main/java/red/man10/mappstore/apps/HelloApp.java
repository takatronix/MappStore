package red.man10.mappstore.apps;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.mappstore.MappRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;


/////////////////////////////////////////////////////////
//          Hello map app example
/////////////////////////////////////////////////////////



public class HelloApp extends MappApp {

    ////////////////////////////////
    //     Data
    ///////////////////////////////////
    static class MappData{
        int         data;

        //   Add your data here
        //   マップごとに保存するデータはここに追加
    }


    ////////////////////////////////////////////
    //      App name (must be unique key)
    //      アプリ名：ユニークな必要があります
    final  static String appName = "hello";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    final static int  drawRefreshCycle = 20;

    static int clickedCount;

    static public void register(){


        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        MappRenderer.buttonEvent(appName, (String key, int mapId,Player player) -> {
            clickedCount++;
            //    true -> updateView:描画更新
            return true;
        });

        //////////////////////////////////////////////////
        //      DisplayTouch Event
        MappRenderer.displayTouchEvent(appName, (String key, int mapId,Player player,int x,int y) -> {

            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D gr = MappRenderer.getGraphics(mapId);
            if(gr == null){
                return false;
            }

            gr.setColor(Color.RED);
            gr.drawLine(x,y,x,y);

            //    true -> updateView:描画更新
            return true;
        });

        /////////////////////////////////////////////////
        //      rendering logic 描画ロジックをここに書く
        MappRenderer.draw( appName, 0, (String key, int mapId,Graphics2D g) -> {
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);
            g.setColor(Color.RED);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Hello:"+clickedCount,4,70);
            return true;
        });

    }

}



