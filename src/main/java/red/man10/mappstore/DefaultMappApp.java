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


            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D g = DynamicMapRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

            //     　
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);

            //    true -> updateView:描画更新
            return true;
        });

        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理
        DynamicMapRenderer.registerDisplayTouchEvent("hello", (String key, int mapId,int x,int y) -> {

            //////////////////////////////////////////////
            //  Get Graphics context for drawing
            //  描画用コンテキスト取得
            Graphics2D gr = DynamicMapRenderer.getGraphics(mapId);
            if(gr == null){
                return false;
            }

            Bukkit.getLogger().info("ddddd"+x);
            gr.setColor(Color.RED);
            gr.drawLine(x,y,x,y);

            //    true -> updateView:描画更新
            return true;
        });

        /////////////////////////////////////////////////
        //      rendering logic 描画ロジックをここに書く
        DynamicMapRenderer.register( "hello", 0, (String key, int mapId,Graphics2D g) -> {
           // g.setColor(Color.BLACK);
           // g.fillRect(0,0,128,128);
            g.setColor(Color.RED);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Hello:",10,70);
            return true;
        });

    }

}



