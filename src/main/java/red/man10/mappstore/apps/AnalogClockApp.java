package red.man10.mappstore.apps;

import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;
import red.man10.mappstore.MappApp;

import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/////////////////////////////////////////////////////////
//          mApp default template
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////
//             RyotackeyClockmAPP
//
//       It can swich from Analog Clock to Digital Clock
//       and change the color!
//
//              created by Ryotackey
//        https://github.com/Ryotackey
/////////////////////////////////////////////////////////


public class AnalogClockApp extends MappApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    static String appName = "analogclock";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 20*60;




    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register(){

        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            AnalogClockApp.clockDraw(g,Color.black,Color.white);
            return true;
        });

    }

    public static void clockDraw( Graphics2D g,Color backcolor, Color maincolor){

        String hour = formattedTimestamp(current(), "HH");
        String minutes = formattedTimestamp(current(), "mm");
        String seconds = formattedTimestamp(current(), "ss");

     //   int x1 = getX(50, seconds, 6);
     //   int y1 = getY(50, seconds, 6);

        int x2 = getX(50, minutes, 6);
        int y2 = getY(50, minutes, 6);

        int x3 = getX(28, hour, 30);
        int y3 = getY(28, hour, 30);

        g.setColor(backcolor);
        g.fillRect(0, 0, 128, 128);

        MappDraw.drawImage(g, "analog_clock", 0, 0, 128, 128);

        BasicStroke wideStroke;

        g.setColor(maincolor);
    //    g.drawLine(64, 64, x1, y1);
        wideStroke = new BasicStroke(3.0f);
        g.setStroke(wideStroke);
        g.drawLine(64, 64, x2, y2);
        wideStroke = new BasicStroke(5.0f);
        g.setStroke(wideStroke);
        g.drawLine(64, 64, x3, y3);

    }

    public static int getY(int range, String time, int scale){

        int coord = (int) (64 + range * Math.sin(Integer.valueOf(time) * scale * (Math.PI / 180) - Math.PI / 2));

        return coord;
    }

    public static int getX(int range, String time, int scale){

        int coord = (int) (64 + range * Math.cos(Integer.valueOf(time) * scale * (Math.PI / 180) - Math.PI / 2));

        return coord;
    }

    public static String formattedTimestamp(Timestamp timestamp, String timeFormat) {
        return new SimpleDateFormat(timeFormat).format(timestamp);
    }

    public static Timestamp current() {
        return new Timestamp(System.currentTimeMillis());
    }

}
