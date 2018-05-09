package red.man10.mappstore;

import org.bukkit.Bukkit;

import java.awt.*;


/////////////////////////////////////////////////////////
//          Hello map app example
/////////////////////////////////////////////////////////

public class DefaultMappApp {


    static int clickedCount;

    static void register(){


        Bukkit.getLogger().info("reg");
        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        DynamicMapRenderer.registerButtonEvent("hello", (String key, int mapId) -> {
            clickedCount++;

            //    true -> updateView:描画更新
            return true;
        });

        /////////////////////////////////////////////////
        //      rendering logic 描画ロジックをここに書く
        DynamicMapRenderer.register( "hello", 0, (String key, int mapId,Graphics2D g) -> {
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);
            g.setColor(Color.RED);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Hello:"+clickedCount,10,70);
            return true;
        });

    }

}



