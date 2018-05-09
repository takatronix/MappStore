package red.man10.mappstore.apps;

import org.bukkit.entity.Player;
import red.man10.mappstore.DynamicMapRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;

/////////////////////////////////////////////////////////
//          mApp default template
//
/////////////////////////////////////////////////////////

public class DrawMappApp extends MappApp {

    class DrawData{
        int x;
        int y;
        int x2;
        int y2;
        Color col;
    }


    ///////////////////////////////////////////
    //      App name (must be unique )
    //      アプリ名：ユニークな必要があります
    final  static String appName = "draw";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    final static int  drawRefreshCycle = 0;

    ///////////////////////////////////////////////////////
    //      Call this function to register your app
    //      アプリを登録するためにこの関数をコールしてください
    static public void register(){

        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        DynamicMapRenderer.registerButtonEvent(appName, (String key, int mapId,Player player) -> {


            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D g = DynamicMapRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

            //   画面をけす     　
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);

            //    true -> call drawing logic / trueで描画ロジックがコールされます
            return true;
        });

        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
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

            //    true -> updateView:描画更新
            return true;
        });


        /////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        DynamicMapRenderer.register( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {
//            g.setColor(Color.BLACK);
//            g.fillRect(0,0,128,128);

            g.setColor(Color.YELLOW);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
           // g.drawString("Your App: "+appName,10,70);

            //  true -> update map / trueでマップに画像が転送されます
            return true;
        });
    }


}
