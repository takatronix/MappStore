package red.man10.mappstore.apps;

import org.bukkit.entity.Player;
import red.man10.mappstore.DynamicMapRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/////////////////////////////////////////////////////////
//          Mapp App default template
//
/////////////////////////////////////////////////////////

public class ClockMappApp extends MappApp {


    ///////////////////////////////////////////
    //      App name (must be unique key)
    //      アプリ名：ユニークな必要があります
    final  static String appName = "clock";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    final static int  drawRefreshCycle = 20 * 10;

    ///////////////////////////////////////////////////////
    //      Call this function to register your app
    //      アプリを登録するためにこの関数をコールしてください
    static public void register(){

        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        DynamicMapRenderer.registerButtonEvent(appName, (String key, int mapId,Player player) -> {

            //    true -> call drawing logic / trueで描画ロジックがコールされます
            return true;
        });


        /////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        DynamicMapRenderer.register( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            //      背景を黒に
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);

            LocalDateTime now = LocalDateTime.now();
            String time = DateTimeFormatter.ofPattern("HH:mm").format(now);

            g.setColor(Color.WHITE);
            g.setFont(new Font( "SansSerif", Font.BOLD ,40 ));
            g.drawString(time,6,80);


            //  true -> update map / trueでマップに画像が転送されます
            return true;
        });
    }


}
