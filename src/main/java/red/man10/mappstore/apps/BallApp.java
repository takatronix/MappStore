package red.man10.mappstore.apps;

import org.bukkit.entity.Player;
import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;
import red.man10.mappstore.MappApp;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/////////////////////////////////////////////////////////
//          mApp default template
//
//     https://github.com/takatronix/MappStore/
//     Please give me pull request your mApp!
/////////////////////////////////////////////////////////

public class BallApp extends MappApp {

    static int hit = 0;

    static class Ball{
        Random r = new Random();
        double x = r.nextInt(64);
        double y = r.nextInt(64);
        int w = 8;
        int h = 8;
        double vx = 5.0 * r.nextDouble() - 5.0;
        double vy = 5.0 * r.nextDouble()- 5.0;


        public boolean hitCheck(Ball b){

            double rx = x - w/2;
            double ry = y - h/2;


            double sx = b.x - b.w/2;
            double sy = b.y - b.h/2;

            Rectangle r = new Rectangle((int)rx,(int)ry,w,h);
            Rectangle r2 = new Rectangle((int)sx,(int)sy,b.w,b.h);


            if(r.intersects(r2)){

                vx *= -1;
                vy *= -1;
                move();
                return true;
            }
            return false;
        }


        //      オブジェクトを動かす
        public boolean move(){

            boolean flag = false;
            x += vx;
            y += vy;

            if( x <  w/2){
                x = w/2;
                vx *= -1;
                flag = true;
            }
            if( x > 128 - w/2){
                x = 128-w/2;
                vx *= -1;
                flag = true;
            }

            if( y < w/2){
                y = w/2;
                vy *= -1;
            }

            if( y > 128 - w/2){
                y = 128-w/2;
                vy *= -1;
                flag = true;
            }
            return flag;
        }


    }



  static   ArrayList<Ball> balls = new ArrayList<>();
    static int ballCount = 100;
//    static Ball[] balls = new Ball[ballCount];


    static Ball cursor = new Ball();
   // static double         cx = 64;
    //static double         cy = 64;

   // static double vx = 0;
  //  static double vy = 0;

    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    static String appName = "ball";

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static int  drawRefreshCycle = 1;


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


    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register(){


        ///////////////////////////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        //////////////////////////////////////////////////////////////////////
        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);

            for(Ball ball:balls){
                if( ball.hitCheck(cursor)) {
                    hit++;
                }
                ball.move();
                MappDraw.drawImage(g,"monsterball",(int)ball.x - 4,(int)ball.y - 4,8,8);
            }


            cursor.move();

            MappDraw.drawImage(g,"monsterball",(int)cursor.x - 4,(int)cursor.y - 4,16,16);

            g.setColor(Color.RED);
            g.drawString("hit:"+hit, 10, 10);

            return true;
        });


        /////////////////////////////////////////////////////////////////////////////
        //      マップをアイテムフレームに配置した時のイベント
        /////////////////////////////////////////////////////////////////////////////


        /////////////////////////////////////////////////
        //      Button (nearby map) clicked event
        //      ボタンが押された時の処理
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


        /////////////////////////////////////////////////////////////////////////////
        //      Events when player jumped with the map
        //      マップをもった状態のイベント
        /////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////
        //      Sneak  /　ジャンプ
        MappRenderer.playerJumpEvent(appName,(String key,int mapId,Player player) ->{

            player.sendMessage("Jumped :" + key + " mapID:"+mapId);

            cursor.w = 16;
            cursor.h = 16;


            for(int i = 0;i < 10;i++){
                balls.add(new Ball());

            }


            cursor.x = 64;
            //    true -> call drawing logic :描画更新
            return true;
        });

        ////////////////////////////////////
        //      Sneak  /　スニーク
        MappRenderer.playerSneakEvent(appName,(String key,int mapId,Player player,boolean isSneaking) ->{

            if(isSneaking){
                player.sendMessage("Sneaked: " + key + " mapID:"+mapId);
            }else{
                player.sendMessage("Sneaked off: " + key + " mapID:"+mapId);
            }

            //    true -> call drawing logic :描画更新
            return true;
        });


        ///////////////////////////////////////
        //  Pitch&Velocity  /　上下向き&速度
        MappRenderer.playerPitchEvent(appName,(String key,int mapId,Player player,double angle,double velocity) ->{

            //player.sendMessage("pitch:"+angle + " velocity:"+velocity);

            //vy = velocity;
/*
            double dy = 128 -(Math.abs(angle - 90) * 3);
            //player.sendMessage("angle"+dy);

            cy = (int)dy;
            if(cy < 4){
                cy = 4;
            }
            if( cy > 124){
                cy = 124;
            }
*/

            //    true -> call drawing logic :描画更新
            return true;
        });


        ///////////////////////////////////////
        //  Yaw&Velocity  /　左右向き&速度
        MappRenderer.playerYawEvent(appName,(String key,int mapId,Player player,double angle,double velocity) ->{

            // player.sendMessage("angle:"+angle + " velocity:"+velocity);

            Graphics2D g = MappRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

         //   vx = velocity;
            cursor.vx = velocity;
            cursor.vy = 0;
            cursor.y = 64;




            return true;
        });



    }


}
