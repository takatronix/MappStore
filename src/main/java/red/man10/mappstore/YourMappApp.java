package red.man10.mappstore;

import java.awt.*;

/////////////////////////////////////////////////////////
//          Mapp App default template
//
/////////////////////////////////////////////////////////

public class YourMappApp {

    ////////////////////////////////////////////
    //      App name (must be unique key)
    //      アプリ名：ユニークな必要があります
    final  static String appName = "yourapp";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    final static int  drawRefreshCycle = 20;


    ///////////////////////////////////////////////////////
    //      Call this function to register your app
    //      アプリを登録するためにこの関数をコールしてください
    static void register(){
        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        DynamicMapRenderer.registerButtonEvent(appName, (String key, int mapId) -> {

            //    true -> call drawing logic / trueで描画ロジックがコールされます
            return true;
        });

        /////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        DynamicMapRenderer.register( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);
            g.setColor(Color.YELLOW);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Your Mapp App",10,70);

            //  true -> update map / trueでマップに画像が転送されます
            return true;
        });
    }


}
