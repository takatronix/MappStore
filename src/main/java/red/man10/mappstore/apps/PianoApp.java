package red.man10.mappstore.apps;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import red.man10.mappstore.MappApp;
import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;

import java.awt.*;
import java.util.HashMap;

/////////////////////////////////////////////////////////
//          mApp default template
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////



public class PianoApp extends MappApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    static String appName = "piano";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 0;


    ///////////////////////////////
    //     Data
    ///////////////////////////////
    static class MappData{
        int         data;
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

        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタン押された時の処理

        MappRenderer.buttonEvent(appName, (String key, int mapId, Player player) -> {

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

            if (y >= 64 ) {
                if (x <= 28 ) {
                    player.sendMessage("DO");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.G));
                    return false;
                }
                if (x <= 38 ) {
                    player.sendMessage("RE");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.A));
                    return false;
                }
                if (x <= 49 ) {
                    player.sendMessage("MI");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.B));
                    return false;
                }
                if (x <= 60 ) {
                    player.sendMessage("FA");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.C));
                    return false;
                }
                if (x <= 70 ) {
                    player.sendMessage("SO");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.D));
                    return false;
                }
                if (x <= 81 ) {
                    player.sendMessage("LA");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.E));
                    return false;
                }
                if (x <= 93 ) {
                    player.sendMessage("TI");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(0, Note.Tone.F));
                    return false;
                }
                if (x <= 103 ) {
                    player.sendMessage("DO");
                    player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.G));
                    return false;
                }
            }

            //    true -> call drawing logic :描画更新
            return false;
        });


        /////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            g.setColor(Color.WHITE);
            g.fillRect(0,0,128,128);

            g.setColor(Color.BLACK);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            g.drawString("Piano",10,70);

            MappDraw.drawImage(g, "piano", 0, 0, 128, 128);

            //  true -> update map / trueでマップに画像が転送されます
            return true;
        });
    }


}
