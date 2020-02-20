/*
     ☆ Bounen057 ☆
 > MCID-> Bounen057 <
 > Twitter-> https://twitter.com/Bounen057 <
 */
package red.man10.mappstore.apps;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Location;


public class BreakoutApp {


    ////////////////////////////////////////////
    //      App name (must be unique)
    //      アプリ名：ユニークな必要があります
    final static String appName = "breakout";   //       <- please change this
    final static String th="th";

    ///////////////////////////////
    //     Data
    ///////////////////////////////
    static class MappData {
        int y_cursor=50;
        int sneak_amount;

        int x_player[] = new int[16];//ber x
        int y_player[] = new int[16];//ber y
        int size_player[] = new int[16];//player size

        int x_vector[] = new int[16];//vector
        int y_vector[] = new int[16];//vector

        int x_ball[] = new int[16];//ball x
        int y_ball[] = new int[16];//ball y

        int game_time;
        Player playing_player;
        int break_amount=0;
        int break_combo=0;
        int break_comboMax=0;
        int blocks[][] = new int[1024][1024];//blocks data

        int game_mode=0;
        //0=title
        //1=game
        //2=finished game
        //3=ranking


        int ball_amount=1;//ball amount
    }

    static HashMap<Integer, BreakoutApp.MappData> hashMap = new HashMap<Integer, BreakoutApp.MappData>();

    //      ユーザーデーター保存
    static BreakoutApp.MappData loadData(int mapId) {
        BreakoutApp.MappData data = hashMap.get(mapId);
        if (data == null) {
            data = new BreakoutApp.MappData();
        }
        return data;
    }

    //      ユーザーデータ読み込
    static BreakoutApp.MappData saveData(int mapId, BreakoutApp.MappData data) {
        return hashMap.put(mapId, data);
    }


    static FileConfiguration config = MappRenderer.getAppConfig(appName);
    //      MappRenderer.saveAppConfig(appName,config)  で保存


    ///////////////////////////////////////////////////////
    //    Call this function to register the your app
    //    アプリを登録するためにこの関数をコールしてください
    static public void register() {

        ///////////////////////////////////////////////////////////////////////
        //    When maps is initialized
        //    マップが初期化されたとき
        MappRenderer.init(appName, (String key, int mapId) -> {
            //      Configファイルに保存する
            for(int i=1;i!=6;i++) {
                if (config.getString("ranking.mcid." + i)==null || config.getString("ranking.mcid." + i).isEmpty()) {
                    config.set("ranking.score." + i, 0);
                    config.set("ranking.mcid." + i,"No record" );
                }
            }
            MappRenderer.saveAppConfig(appName, config);


            return true;  //  true -> update map / trueでマップに画像が転送されます
        });


        ///////////////////////////////////////////////////////////////////////
        //     drawing logic
        //     描画ロジックをここに書く
        MappRenderer.draw(appName, 1, (String key, int mapId, Graphics2D g) -> {
            MappData m = loadData(mapId);
            //////////////////////////
            //title / タイトル
            if(m.game_mode==0) {
                //  Clear screen (画面消去)
                MappDraw.drawImage(g,"breakout_background",0,0,128,128);
                MappDraw.drawImage(g,"breakout_logo",25,2,80,25);
                //draw logo
                g.setFont(new Font( "SansSerif", Font.PLAIN,9));
                MappDraw.drawOutlineString(g,"Sneak -> ↑↓",Color.black,Color.white,2,114);
                MappDraw.drawOutlineString(g,"Jump -> Select",Color.black,Color.white,2,124);

                //select mode
                g.setColor(Color.MAGENTA);
                g.setFont(new Font( "SansSerif", Font.PLAIN,10));
                g.drawString("[Play]",13,50);
                g.drawString("[Ranking]",13,60);

                g.setColor(Color.WHITE);
                g.drawString("←",66,m.y_cursor);
                switch (m.y_cursor){
                    case 50:
                        g.drawString("[Play]",13,50);
                        break;
                    case 60:
                        g.drawString("[Ranking]",13,60);
                        break;
                }
            }
            //////////////////////////
            //game mode / ゲームモード
            if(m.game_mode==1) {
                //  Clear screen (画面消去)
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 128, 128);
                m.game_time=m.game_time-1;
                ////////////
                //count down
                switch (m.game_time){
                    case 4060:
                    case 4040:
                    case 4020:
                        m.playing_player.sendMessage("§e§l[Breakout]§a§l"+(int)(m.game_time-4000)/20+"...");
                        //音 / sounds
                        World w = m.playing_player.getWorld();
                        w.playSound(m.playing_player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        break;
                    case 4000:
                        m.playing_player.sendMessage("§e§l[Breakout]§a§lGAME START!");
                        //音 / sounds
                        w = m.playing_player.getWorld();
                        w.playSound(m.playing_player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                }
                //ステージ 描画
                BreakoutApp.drawStage(mapId);
                ////////////////////////////////
                //Single score mode / 1人用ゲームモード
                for (int i = 0; i != m.ball_amount; i++) {

                    //下のバー / down ber
                    g.setColor(Color.WHITE);
                    g.fillRect(m.x_player[0], m.y_player[0], m.size_player[0], 3);

                    m.size_player[0]=8+(56-m.break_amount)/2;
                    //hit event
                    BreakoutApp.hit(mapId);
                    ///////////////////////////
                    //ball vector / ベクトル計算
                    if((int)m.game_time/20<=199) {
                        m.y_ball[i] = m.y_ball[i] + m.y_vector[i];
                        m.x_ball[i] = m.x_ball[i] + m.x_vector[i];
                    }
                    /////////////////////////////
                    //draw ball / 球を表示
                    g.setColor(Color.WHITE);
                    g.fillRect(m.x_ball[i] + 1, m.y_ball[i], 2, 4);
                    g.fillRect(m.x_ball[i], m.y_ball[i] + 1, 4, 2);
                    /////////////
                    //draw stats

                    //time
                    g.setColor(Color.MAGENTA);
                    g.setFont(new Font( "SansSerif", Font.PLAIN,10));
                    MappDraw.drawOutlineString(g,"TIME: "+(int)m.game_time/20,Color.white,Color.black,2,10);
                    //combo
                    MappDraw.drawOutlineString(g,"COMBO: "+m.break_combo,Color.white,Color.black,50,10);

                    //Finish
                    if(m.break_amount>=56 || m.y_ball[i]>=130 || m.game_time==-1){
                        if (m.break_comboMax<m.break_combo){
                            m.break_comboMax=m.break_combo;
                            Bukkit.broadcastMessage(""+m.break_comboMax);
                        }
                        m.break_combo=0;

                        BreakoutApp.gameFinish(mapId);
                    }
                }
            }
            if(m.game_mode==3){
                //  Clear screen (画面消去)
                MappDraw.drawImage(g,"breakout_background",0,0,128,128);
                Color ranking_color= Color.black;
                String th="th";
                //draw ranking mcid and score
                for(int i=1;i!=6;i++){
                    switch (i){
                        case 1:
                            th="st";
                            ranking_color= Color.YELLOW;break;
                        case 2:
                            th="nd";
                            ranking_color= Color.LIGHT_GRAY;break;
                        case 3:
                            th="rd";
                            ranking_color= Color.RED;break;
                        default:
                            ranking_color= Color.BLACK;
                            th="th";break;
                    }
                    String mcid = "ranking.mcid."+i;
                    if(mcid != null){
                        g.setFont(new Font( "SansSerif", Font.PLAIN,10));
                        MappDraw.drawOutlineString(g,i+th+"/"+(config.getString("ranking.mcid."+i)+"/"+(config.getInt("ranking.score."+i))),ranking_color,Color.white,2,30+(i*12));
                    }

                }
                //draw help
                g.setFont(new Font( "SansSerif", Font.PLAIN,16));
                MappDraw.drawOutlineString(g,"<RANKING>",Color.white,Color.yellow,15,18);
                g.setFont(new Font( "SansSerif", Font.PLAIN,10));
                MappDraw.drawOutlineString(g,"Shift->title",Color.black,Color.white,2,124);
            }
            saveData(mapId, m);
            return true;  //  true -> update map / trueでマップに画像が転送されます
        });
        /////////////////////////////////////////////////////////////////////////////
        //      Events when player have the mapapp in main hand
        //      マップをメインハンドにもった状態のイベント
        /////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////
        //      Jump  /　ジャンプ
        MappRenderer.playerJumpEvent(appName, (String key, int mapId, Player player) -> {
            MappData m = loadData(mapId);
            /////////////////////////////////////////////////////////
            //game_mode=title mode event / タイトル画面でのジャンプの処理
            if(m.game_mode==0) {
                switch (m.y_cursor){
                    case 50://to gamemode 1 playing game
                        BreakoutApp.gameStart(mapId);
                        m.playing_player = player;
                        World w = m.playing_player.getWorld();//sounds
                        w.playSound(m.playing_player.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);
                        break;
                    case 60://to gamemode 3 looking ranking
                        m.game_mode=3;
                        w = m.playing_player.getWorld();//sounds
                        w.playSound(m.playing_player.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);
                }
                //game_mode=Finished game / ゲームが終わった時でのスニークの処理
            }if(m.game_mode==2) {
                m.game_mode=0;
                World w = m.playing_player.getWorld();//sounds
                w.playSound(m.playing_player.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);
            }
            saveData(mapId, m);
            return true;
        });

        ////////////////////////////////////
        //      Sneak  /　スニーク
        MappRenderer.playerSneakEvent(appName, (String key, int mapId, Player player, boolean isSneaking) -> {
            MappData m = loadData(mapId);
            /////////////////////////////////////////////////////////
            //game_mode=title mode event / タイトル画面でのスニークの処理
            if(m.game_mode==0) {
                m.sneak_amount = m.sneak_amount + 1;
                if (m.sneak_amount % 2 == 1) {
                    switch (m.y_cursor) {
                        case 60://to gamemode 1 playing game
                            m.y_cursor = 50;
                            World w = m.playing_player.getWorld();//sounds
                            w.playSound(m.playing_player.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);
                            break;
                        case 50://to gamemode 3 looking ranking
                            m.y_cursor = 60;
                            w = m.playing_player.getWorld();//sounds
                            w.playSound(m.playing_player.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);
                            break;
                    }
                }
                //game_mode=Finished game / ゲームが終わった時でのスニークの処理
            }if(m.game_mode==2) {
                BreakoutApp.gameStart(mapId);
                m.playing_player = player;
            }if(m.game_mode==3) {//game_mode=ranking / ランキングでのスニークの処理
                m.game_mode=0;
            }
            saveData(mapId, m);
            return true; //    true -> call drawing logic :描画更新
        });
        ///////////////////////////////////////
        //  Yaw&Velocity  /　左右向き&速度
        MappRenderer.playerYawEvent(appName, (String key, int mapId, Player player, double angle, double velocity) -> {
            MappData m = loadData(mapId);
            ////////////
            //game mode
            if(m.game_mode==1) {
                //////////////////////
                //バー 操作 / play ber
                if((int)m.game_time/20<=199) {
                    if (m.x_player[0] <= 116 && m.x_player[0] >= 0) {
                        m.x_player[0] = m.x_player[0] + (int) velocity;
                    }
                }
                ///////////////
                //左右 制限 バー / limit ber
                if (m.x_player[0] >= 123 - m.size_player[0]) {
                    m.x_player[0] = 123 - m.size_player[0] - 1;
                }
                if (m.x_player[0] <= 5) {
                    m.x_player[0] = 6;
                }
            }
            saveData(mapId, m);

            return true; //    true -> call drawing logic :描画更新
        });


    }

    //////////////////////////
    // 当たり判定 / hit events
    static public void hit(int mapId) {
        MappData m = loadData(mapId);
        boolean x_play;//If true then invert the x vector
        boolean y_play;//If true then invert the y vector
        boolean hit_ber;//If statement omitted variable
        for (int i=0;i!=m.ball_amount;i++) {
            x_play = false;y_play = false;
            hit_ber = false;//
            for (int i_ber = 0; i_ber != m.size_player[0]; i_ber++) {
                ///////////////////////////////////////////////
                //当たり判定 下のバーとボール / underber hit event
                if (m.y_vector[i] >= 0 && m.y_player[0] >= m.y_ball[i] && m.y_player[0] - 4 <= m.y_ball[i]) {
                    hit_ber = true;
                }
                if (m.x_player[0] + i_ber == m.x_ball[i] + 2 && hit_ber == true) {
                    y_play = true;
                    if (m.break_comboMax<m.break_combo){
                        m.break_comboMax=m.break_combo;
                    }
                    m.x_vector[i] = (int) ((m.size_player[i] / 2 - i_ber) - ((m.size_player[0] / 2 - i_ber))*2)/4;
                    if(m.x_vector[i]==0){
                        m.x_vector[i]=new Random().nextInt(2) -1;
                    }
                    m.break_combo=0;
                }
            }
            ///////////////////////////////////////////////
            //壊れない壁のボール / Cant break wall hit event
            //上 / up
            if (m.y_ball[i] <= 8) {
                m.y_ball[i] = 9;
                y_play = true;
            }
            //右 / right
            if (m.x_ball[i] >= 118) {
                m.x_ball[i] = 117;
                x_play = true;
            }
            //左 / left
            if (m.x_ball[i] <= 5) {
                m.x_ball[i] = 6;
                x_play = true;
            }
            ///////////////////////////////////////////////
            //ボールと壁の当たり判定 / ball and wall hit event
            //                up/上       right/右  down/下 left/左
            int[] sx =     {  0, 1, 2, 3, 5,5,5,5, 0,1,2,3 ,-2,-2,-2,-2};
            int[] sy =     { -2,-2,-2,-2, 0,1,2,3, 5,5,5,5 ,0 , 1, 2, 3};
            int[] sevent = {  0, 0, 0, 0, 1,1,1,1, 0,0,0,0 ,1 , 1, 1, 1};

            //hiting event / 当たった時の処理
            for(int i_2=0;i_2!=16;i_2++) {
                if (m.blocks[(m.x_ball[i] + sx[i_2]) / 11][(m.y_ball[i] + sy[i_2]) / 6] == 1) {
                    m.blocks[(m.x_ball[i] + sx[i_2]) / 11][(m.y_ball[i] + sy[i_2]) / 6] = 0;
                    m.break_amount=m.break_amount+1;
                    m.break_combo=m.break_combo+1;

                    //reverse vectors
                    switch (sevent[i_2]) {
                        case 0: y_play = true;break;
                        case 1: x_play = true;
                    }
                }
            }

            if (x_play == true) { BreakoutApp.reverseVectors(mapId, 0, i); }
            if (y_play == true) { BreakoutApp.reverseVectors(mapId, 1, i); }
        }
        saveData(mapId, m);
    }

    ////////////////////////////////////
    //reverse vectors / ベクトルを逆にする
    //vectormode = 0 //X
    //vectormode = 1 //Y
    static public void reverseVectors(int mapId, int vectormode,int i) {
        MappData m = loadData(mapId);
        if (vectormode == 0) {
            m.x_vector[i] = m.x_vector[i] - m.x_vector[i] * 2;
        }
        if (vectormode == 1) {
            m.y_vector[i] = m.y_vector[i] - m.y_vector[i] * 2;
        }
        //音 / sounds
        World w = m.playing_player.getWorld();
        w.playSound(m.playing_player.getLocation(), Sound.BLOCK_METAL_BREAK, 1, 1);
        saveData(mapId, m);
    }

    ////////////////////////////
    //make stage / ステージの生成
    static public void makeStage(int mapId) {
        MappData m = loadData(mapId);
        for (int x = 2; x != 10; x++) {
            for (int y = 3; y != 10; y++) {
                m.blocks[x][y] = 1;
            }
        }
        saveData(mapId, m);
    }

    ////////////////////////////
    //draw stage / ステージの描画
    static public void drawStage(int mapId) {
        MappData m = loadData(mapId);
        Graphics2D g = MappRenderer.getGraphics(mapId);
        g.setColor(Color.WHITE);
        //create dont break wall / 壊れない壁の作成
        g.fillRect(3, 3, 122, 5);   //上
        g.fillRect(3, 3, 3, 125); //左
        g.fillRect(122, 3, 3, 125); //右
        //create wall / 普通の壁の生成
        for (int x = 2; x != 10; x++) {
            for (int y = 3; y != 10; y++) {
                if (m.blocks[x][y] == 1) {
                    ///////////////////////
                    //Block's color / 壁の色
                    switch (x){
                        case 2:
                            g.setColor(Color.RED);break;
                        case 3:
                            g.setColor(Color.getHSBColor(20,100,100));break;
                        case 4:
                            g.setColor(Color.YELLOW);break;
                        case 5:
                            g.setColor(Color.green);break;
                        case 6:
                            g.setColor(Color.CYAN);break;
                        case 7:
                            g.setColor(Color.BLUE);break;
                        case 8:
                            g.setColor(Color.lightGray);break;
                        case 9:
                            g.setColor(Color.WHITE);break;
                    }
                    //壁の描画
                    g.fillRect(x * 11, y * 6, 10, 5);
                }
            }
        }
        saveData(mapId, m);
    }

    ////////////////////////////////////////
    //GAME START or RESET / 初期化
    static public void gameStart(int mapId) {
        MappData m = loadData(mapId);
        //////////////////////////////
        //Player data reset / プレイヤー情報リセット
        for(int i=0;i!=m.ball_amount;i++) {
            m.size_player[0] = 32;
            m.x_player[0] = 64 - m.size_player[0] / 2;
            m.y_player[0] = 110;

            m.x_vector[i] = new Random().nextInt(8) - 4;
            m.y_vector[i] = -2;

            m.x_ball[i] = 64;
            m.y_ball[i] = 105;
        }
        //reset variable / リセット変数
        m.game_mode=1;
        m.game_time=4070;
        m.break_comboMax=0;
        m.break_amount=0;
        makeStage(mapId);
        drawStage(mapId);
        saveData(mapId, m);
    }
    /////////////////////////////////////////
    //Finished game / 終わったゲームの処理
    static public void gameFinish(int mapId) {
        MappData m = loadData(mapId);
        m.game_mode=2;
        Graphics2D g = MappRenderer.getGraphics(mapId);
        //音 と エフェクト / sounds and effects
        World w = m.playing_player.getWorld();
        w.playSound(m.playing_player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
        Location loc = m.playing_player.getLocation();
        ///////////////////////////////
        //Draw message / メッセージを塗る
        g.setFont(new Font( "SansSerif", Font.PLAIN,23));
        MappDraw.drawOutlineString(g,"<FINISH>",Color.black,Color.RED,10,40);
        g.setFont(new Font( "SansSerif", Font.PLAIN,13));
        MappDraw.drawOutlineString(g,"Sneak-> Play again",Color.GREEN,Color.white,5,70);
        MappDraw.drawOutlineString(g,"Jump -> Menu",Color.orange,Color.white,5,83);

        //////////////////////////////////////////////////////////////
        //player sendMessage score / プレイヤーのスコアの計算
        m.playing_player.sendMessage("§e§l[Breakout]§a§l<FINISH>");
        m.playing_player.sendMessage("§e§l[Breakout]§7time:"+m.game_time/20+"*("+m.break_amount+"/56)="+(int)((double)m.game_time*((double)m.break_amount/56)/20));
        m.playing_player.sendMessage("§e§l[Breakout]§7break amount:"+m.break_amount+"*2="+m.break_amount*2);
        m.playing_player.sendMessage("§e§l[Breakout]§7break max combo:"+m.break_comboMax+"*2="+m.break_comboMax*2);
        //score
        int player_score = (int)((double)m.game_time*((double)m.break_amount/56)/20 + m.break_amount*2 + m.break_comboMax*2);
        m.playing_player.sendMessage("§e§l[Breakout]§7total score:"+player_score);
        ///////////////////////////////////////////////////////
        //top ranking score / プレイヤーのスコアが順位に載る際の処理
        for(int i=1;i!=6;i++) {
            if(config.getInt("ranking.score."+i)<=player_score) {
                /////////////////////////////////////////////
                //Save player score / プレイヤーのスコアをセーブ
                for(int i_2=1;i_2!=7-i;i_2++) {
                    config.set("ranking.score." + (7-i_2), config.getInt("ranking.score." +(6-i_2)));
                }
                config.set("ranking.score.6", null);
                config.set("ranking.score." + i, player_score);
                ///////////////////////////////////////////
                //Save player mcid / プレイヤーのMCIDをセーブ
                for(int i_2=1;i_2!=7-i;i_2++) {
                    config.set("ranking.mcid." + (7-i_2), config.getString("ranking.mcid." +(6-i_2)));
                }
                config.set("ranking.mcid.6", null);
                config.set("ranking.mcid." + i, m.playing_player.getName());
                MappRenderer.saveAppConfig(appName, config);

                //////////////////////////////////
                //New record notice / 新記録時の通知
                String th="th";
                switch (i){
                    case 1: th="st";break;
                    case 2: th="nd";break;
                    case 3: th="rd";break;
                    default:th="th";break;
                }
                Bukkit.broadcastMessage("§e§l[BreakOut]§6§lNew record!§e§l"+m.playing_player.getName()+" §e§lscore:"+player_score+" §egot§l "+i+th+"");
                break;
            }
        }
        saveData(mapId, m);
    }
}