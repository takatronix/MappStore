/*
  ☆ Bounen057 ☆
 > MCID-> Bounen057 <
 > Twitter-> https://twitter.com/Bounen057 <

   hello.Im Japanese.So I can write English a little.
   This application is a dirty program,
   but please do remodel it and use it.
   I am waiting for you at "man10.red"!

   改造して使って見てね!
   ちなみに迷路の真ん中にいるプレイヤーのモデルは
   takatronixさんだよ！

 */

package red.man10.mappstore.apps;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

  public class MazeMappApp {

    ////////////////////////////////
    //     Data
    ///////////////////////////////////
    static class MappAppData {
        int playing_mode;
        int blocks[][] = new int[100][100];//MazeMappApp max size
        int x_ofs;
        int y_ofs;
        int player_x;
        int player_y;
        int time;
        int return_mode;
        Player clickedPlayer;
    }

    static HashMap<Integer, MappAppData> hashMap = new HashMap<Integer, MappAppData>();

    //      読み込み / data road
    static MappAppData loadData(int MappAppId) {
        MappAppData data = hashMap.get(MappAppId);
        if (data == null) {
            data = new MappAppData();
        }
        return data;
    }

    //      保存 / data save
    static MappAppData saveData(int MappAppId, MappAppData data) {
        return hashMap.put(MappAppId, data);
    }


    ////////////////////////////////////////////
    //      App name (must be unique key)
    //      アプリ名：ユニークな必要があります
    final static String appName = "maze";

      //迷路のサイズ 奇数にしてください / MazeMappApp size.Make it an odd number
    static int MazeMappApp_size = 19;
    static int MazeMappApp_cord;

    ////////////////////////////////////////////
    //     Draw refresh Cycle:描画割り込み周期
    //     appTickCycle = 1 -> 1/20 sec
    static public void register() {

        ///////////////////////////////////////////////////////////////////////
        //    When maps is initialized
        //    マップが初期化されたとき
        MappRenderer.init(appName, (String key, int mapId) ->{
            Graphics2D g = MappRenderer.getGraphics(mapId);


            Bukkit.getLogger().info("***** initializing maze app *****");

            g.setColor(Color.black);
            g.fillRect(0,0,128,128);
            g.setColor(Color.red);
            g.drawString(" set in item frame",0,60);
            g.drawString(" press the button",0,80);

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });



        /////////////////////////////////////////////////
        //      Button (nearby MappApp) clicked event
        //      ボタン押された時の処理
        MappRenderer.buttonEvent(appName, (String key, int MappAppId, Player player) -> {
            int MappAppId_copy = MappAppId;
            MappAppData m = loadData(MappAppId_copy);
            Graphics2D g = MappRenderer.getGraphics(MappAppId);
            //変数 代入 / Variable substitution
            m.clickedPlayer = player;

            m.x_ofs = 48;//画面のずらす距離 / Screen shift distance X
            m.y_ofs = 48;//画面のずらす距離 / Screen shift distance Y
            m.player_x = 1;//プレイヤーのいる座標 / Player's coordinates X
            m.player_y = 1;//プレイヤーのいる座標 / Player's coordinates Y
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 128, 128);
            player.sendMessage("§a§l[MazeMappApp]Creating a MazeMappApp... 1/4");

            //描画 / drawing
            for (int x = 0; x != MazeMappApp_size; x++) {
                for (int y = 0; y != MazeMappApp_size; y++) {
                    m.blocks[x][y] = 0;
                }
            }
            player.sendMessage("§a§l[MazeMappApp]Creating a MazeMappApp... 2/4");
            ///////////////////////////
            //  MazeMappApp generation (algorithm)
            //  迷路の生成(生成ルーチン アルゴリズムの処理)
            //
            for (int x = 0; x != MazeMappApp_size; x++) {
                for (int y = 0; y != MazeMappApp_size; y++) {
                    /////////
                    //壁の作成 / creating a wall
                    if (x == 0) {
                        m.blocks[x][y] = 1;
                    }
                    if (x == MazeMappApp_size - 1) {
                        m.blocks[x][y] = 1;
                    }
                    if (y == 0) {
                        m.blocks[x][y] = 1;
                    }
                    if (y == MazeMappApp_size - 1) {
                        m.blocks[x][y] = 1;
                    }
                    //////////////////////////////////////////////////////
                    //棒倒し方 アルゴリズム 生成 / MazeMappApp generation (algorithm)
                    if (x % 2 == 0 && y % 2 == 0) {
                        m.blocks[x][y] = 1;

                        MazeMappApp_cord = new Random().nextInt(3);
                        if (y == 2) { MazeMappApp_cord = new Random().nextInt(4); }

                        if (MazeMappApp_cord == 0 && x != 0 && y != 0) {
                            if (m.blocks[x + 1][y] == 0) {
                                m.blocks[x + 1][y] = 1;
                            }
                        }
                        if (MazeMappApp_cord == 1 && x != 0 && y != 0) {
                            if (m.blocks[x + 1][y] == 0) {
                                m.blocks[x - 1][y] = 1;
                            }
                        }
                        if (MazeMappApp_cord == 2 && x != 0 && y != 0) {
                            if (m.blocks[x + 1][y] == 0) {
                                m.blocks[x][y + 1] = 1;
                            }
                        }
                        if (MazeMappApp_cord == 3 && x != 0 && y != 0) {
                            if (m.blocks[x + 1][y] == 0) {
                                m.blocks[x][y - 1] = 1;
                            }
                        }
                    }
                    //ゴールの設置 / set goal
                    m.blocks[1][2] = 0;
                    m.blocks[MazeMappApp_size - 2][MazeMappApp_size - 2] = 2;
                    m.blocks[MazeMappApp_size - 3][MazeMappApp_size - 2] = 0;
                    m.blocks[MazeMappApp_size - 2][MazeMappApp_size - 3] = 0;
                    saveData(MappAppId, m);
                    m = loadData(MappAppId_copy);
                }
            }
            player.sendMessage("§a§l[MazeMappApp]Creating a MazeMappApp... 3/4 ");
            saveData(MappAppId, m);
            m = loadData(MappAppId_copy);
            ////////////////
            //描画 / drawing
            for (int x = 0; x != MazeMappApp_size; x++) {
                for (int y = 0; y != MazeMappApp_size; y++) {
                    switch (m.blocks[x][y]) {
                        case 1:
                            drawblock(x, y, 0, 0, 8, g, 0, MappAppId);
                            break;
                        case 2:
                            drawblock(x, y, 0, 0, 8, g, 0, MappAppId);
                            break;
                    }
                }
            }

            ////////////////////
            //説明 / Description
            player.sendMessage("§a§l[MazeMappApp]All complete!Please click display! 4/4");
            player.sendMessage("");
            player.sendMessage("§a§l[MazeMappApp]<<rule ・ Description>>");
            player.sendMessage("§a§l[MazeMappApp]§4§lPlayer is takatronix's head!");
            player.sendMessage("§a§l[MazeMappApp]§7Click on the direction of the 4 colors you wish to navigate!");
            player.sendMessage("§a§l[MazeMappApp]§7Click reset button for reset");
            player.sendMessage("§a§l[MazeMappApp]§eThe goal is in the lower right of the MazeMappApp");


            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            MappDraw.drawOutlineString(g,"Please click here!",Color.YELLOW,Color.WHITE,12,70);

            //変数 / variable
            m.playing_mode = 2;
            m.time = 0;
            m.return_mode = 1;
            saveData(MappAppId, m);
            return true;
        });

        //////////////////////////////////////////////////
        //      DisplayTouch Event
        MappRenderer.displayTouchEvent(appName, (String key, int MappAppId, Player player, int x, int y) -> {
            MappAppData m = loadData(MappAppId);
            m.clickedPlayer = player;
            if (m.playing_mode == 1 || m.playing_mode==2) {
                Graphics2D g = MappRenderer.getGraphics(MappAppId);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 128, 128);
                ///////
                //ボタン 処理 / button
                MappDraw.drawImage(g, "maze_button", 0, 0, 128, 128);
                //下 / down x40~87 y65~128
                if (y >= 65 && x <= 87 && x >= 40) {
                    if (m.blocks[m.player_x][m.player_y + 1] != 1) {
                        m.player_y = m.player_y + 1;
                        m.y_ofs = m.y_ofs - 8;
                    }
                }
                //上 / up x40~87 y0~64
                if (y <= 64 && x <= 87 && x >= 40) {
                    if (m.blocks[m.player_x][m.player_y - 1] != 1) {
                        m.player_y = m.player_y - 1;
                        m.y_ofs = m.y_ofs + 8;
                    }
                }
                //左 / left x0~40 y30~108
                if (x <= 40 && y >= 30 && y <= 108) {
                    if (m.blocks[m.player_x - 1][m.player_y] != 1) {
                        m.player_x = m.player_x - 1;
                        m.x_ofs = m.x_ofs + 8;
                    }
                }
                //右 / right x88~128 y30~108
                if (x >= 88 && y >= 30 && y <= 108) {
                    if (m.blocks[m.player_x + 1][m.player_y] != 1) {
                        m.player_x = m.player_x + 1;
                        m.x_ofs = m.x_ofs - 8;
                    }
                }
                if (m.playing_mode == 2) {
                    m.x_ofs = 48;//画面のずらす距離 / Screen shift distance X
                    m.y_ofs = 48;//画面のずらす距離 / Screen shift distance Y
                    m.player_x = 1;//プレイヤーのいる座標 / Player's coordinates X
                    m.player_y = 1;//プレイヤーのいる座標 / Player's coordinates Y
                    m.playing_mode = 1;

                    //スタート メッセージ / start message
                    m.clickedPlayer.sendMessage("§a§l[MazeMappApp]START!");
                }
                World w = m.clickedPlayer.getWorld();//sounds
                w.playSound(m.clickedPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);

                /////////////
                //迷路の描画 / drawing a MazeMappApp
                for (int x_g = 0; x_g != MazeMappApp_size; x_g++) {
                    for (int y_g = 0; y_g != MazeMappApp_size; y_g++) {
                        if (m.blocks[x_g][y_g] == 1) {//壁
                            g.setColor(Color.WHITE);
                            drawblock(x_g, y_g, m.x_ofs, m.y_ofs, 8, g, 0, MappAppId);
                        }//ゴール / goal
                        if (m.blocks[x_g][y_g] == 2) {
                            g.setColor(Color.RED);
                            drawblock(x_g, y_g, m.x_ofs, m.y_ofs, 8, g, 2, MappAppId);
                        }
                    }
                }

                //プレイヤ- 表示 / drawing player
                MappDraw.drawImage(g, "maze_player", 56, 56, 8, 8);
                saveData(MappAppId, m);
            } else {
                m.clickedPlayer.sendMessage("§a§l[MazeMappApp]Click the button to play!");
            }
            m.return_mode = 1;
            if(goal(MappAppId)){
                //      give $10
                MappRenderer.vaultManager.deposit(player.getUniqueId(),10);

            }

            return true;
        });

        /////////////////////////////////////////////////
        //      rendering logic 描画ロジックをここに書く
        MappRenderer.draw(appName, 0, (String key, int MappAppId, Graphics2D g) -> {
            MappAppData m = loadData(MappAppId);
            //////////////
            //return mode(できる限りfalseにして負荷を減らす / return true or false
            if (m.return_mode == 0) {
                return false;
            }
            if (m.return_mode == 1) {
                m.return_mode = 0;
                saveData(MappAppId, m);
                return true;
            }
            return false;
        });
    }

    ///////////////////
    //設置の描画 メソッド/creating a wall method
    static void drawblock(int x, int y, int x_ofs, int y_ofs, int size, Graphics2D g, int mode, int MappAppId_copy) {
        MappAppData m = loadData(MappAppId_copy);
        switch (mode) {
            case 0://壁
                if (m.blocks[x][y + 1] == 0) {
                    MappDraw.drawImage(g, "maze_wall_2", x * 8 + x_ofs, y * 8 + y_ofs, size, size);
                } else {
                    MappDraw.drawImage(g, "maze_wall_1", x * 8 + x_ofs, y * 8 + y_ofs, size, size);
                }
                break;
            case 1://空白
                g.fillRect(x * 8 + x_ofs, y * 8 + y_ofs, size, size);
                break;
            case 2://ゴール
                MappDraw.drawImage(g, "maze_goal", x * 8 + x_ofs, y * 8 + y_ofs, size, size);
        }
        m.return_mode = 1;
    }
    ////////////
    //ゴール 処理 / goll method
    static boolean goal(int MappAppId){
        MappAppData m = loadData(MappAppId);
        Graphics2D g = MappRenderer.getGraphics(MappAppId);
        if (m.blocks[m.player_x][m.player_y] == 2 && m.playing_mode == 1) {
            m.return_mode = 1;
            //背景と文字の描画 / Background and character drawing
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 128, 128);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 32));
            g.drawString("GOAL!", 12, 70);
            //音 / sounds
            World w = m.clickedPlayer.getWorld();//sounds
            w.playSound(m.clickedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 4, 1);
            //メッセージ / sendMessage
            m.clickedPlayer.sendMessage("§a§l[MazeMappApp]GOAL! congratulations!");
            m.clickedPlayer.sendMessage("§a§l[MazeMappApp]When doing again please click the button!");

            m.playing_mode = 0;
            return true;
        }
        return false;
    }

}