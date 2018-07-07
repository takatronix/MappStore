package red.man10.mappstore.apps;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import red.man10.mappstore.MappApp;
import red.man10.mappstore.MappDraw;
import red.man10.mappstore.MappRenderer;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.String.format;


public class TypingApp extends MappApp implements Listener{
    final static String appName = "typing";

    static class TypingData{
        boolean title = true;
        double gameTime;
        boolean TypingCheck;
        int TypingCount;
        double wardCount;
        boolean ranking;
        int random;
        double score;
        Player gamePlayer;
        boolean sendChat;
        String playerMessage = "";
        boolean getMessage;
        boolean saveRanking;
        int awayTime;

    }

    static HashMap<Integer,TypingData> mapData = new HashMap<Integer, TypingData>();

    static TypingData loadData(int mapId) {
        TypingData data = mapData.get(mapId);
        if(data == null){
            data = new TypingData();
        }
        return data;
    }
    //      ユーザーデータ読み込
    static TypingData saveData(int mapId,TypingData d){
        return mapData.put(mapId,d);
    }

    static int drawRefreshCycle = 1;

    static String chatMessage = "§8§l[TypingApp]";
    static ArrayList<String> enterMessage = new ArrayList<String>();

    static int TypingTimes;

    static FileConfiguration config = MappRenderer.getAppConfig(appName);


    public static void register(){

        MappRenderer.init(appName, (String key, int mapId) ->{

            //      Configファイルに保存する

            TypingTimes = config.getInt("TypingTimes",0);

            if (config.getInt("TypingTimes") == 0){
                config.set("TypingTimes",5);
                config.set("AwayTime",1200);
                MappRenderer.saveAppConfig(appName,config);
            }
            if (config.getString("ranking.player.1") == null||
                    config.getString("ranking.player.5") == null){

                for (int i = 1;i<=5;i++) {
                    config.set("ranking.player."+i,"none");
                    config.set("ranking.score."+i,0.0);

                }
            }



            MappRenderer.saveAppConfig(appName,config);
//////////////////////////////////////////////////////////////////////////

            if (!enterMessage.isEmpty()){
                return true;
            }

            File wardsFolder = new File(Bukkit.getServer().getPluginManager().getPlugin("Mappstore").getDataFolder(),File.separator+"TypingApp");
            File wards = new File(wardsFolder,File.separator+"words.txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(wards));
                String str;
                for (int i = 0;(str  = br.readLine()) != null;i++){
                    enterMessage.add(i,str);
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!wards.exists()){
                System.out.println("Cord1");
            }
            System.out.println("config,words");
            return true;  //  true -> update map / trueでマップに画像が転送されます
        });



        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId, Graphics2D g) -> {

            TypingData data = loadData(mapId);

            if (data.title) {
                MappDraw.drawImage(g,"typing_background",0,0,128,128);

                g.setFont(new Font("SansSerif", Font.PLAIN, 10));

                MappDraw.drawOutlineString(g,"TypingGame", Color.WHITE,Color.BLACK,30, 10);
                MappDraw.drawOutlineString(g,"Push the button to start!",  Color.WHITE,Color.BLACK,10, 50);
                MappDraw.drawOutlineString(g,"ボタンを押して",  Color.WHITE,Color.BLACK,10, 60);
                MappDraw.drawOutlineString(g,"ゲームスタート！",  Color.WHITE,Color.BLACK,10, 70);
                g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                MappDraw.drawOutlineString(g,"一定時間放置すると",  Color.WHITE,Color.BLACK,10, 80);
                MappDraw.drawOutlineString(g,"自動でタイトルに戻ります",  Color.WHITE,Color.BLACK,10, 89);

            }else {
                    gameMain(data.gamePlayer,mapId,g);
                }
            if (data.ranking){
                loadRanking(g);
            }

            return true;  //  true -> update map / trueでマップに画像が転送されます
        });

        MappRenderer.buttonEvent(appName, (String key, int mapId,Player player) -> {

            Graphics2D g = MappRenderer.getGraphics(mapId);
            if(g == null){
                return false;
            }

            TypingData data = loadData(mapId);

            if (!data.ranking) {
                if (data.title) {
                    System.out.println("gameStart");
                    data.getMessage = true;
                    data.title = false;
                    data.gamePlayer = player;
                    data.TypingCheck = true;
                    data.gameTime = 0;
                    data.saveRanking = true;

                } else {
                    finish(mapId);
                }
            }
            if (data.ranking){
                data.title = true;
            }
            saveData(mapId,data);

            return true;
        });

        MappRenderer.displayTouchEvent(appName, (String key, int mapId, Player player,int x,int y) -> {

            TypingData data = loadData(mapId);
            if (data.title){
                data.ranking = !data.ranking;
                saveData(mapId,data);

            }

            return true;
        });


        MappRenderer.playerChatEvent(appName,(String key,int mapId,AsyncPlayerChatEvent chatEvent)  ->{
        TypingData data = loadData(mapId);
        Player p = chatEvent.getPlayer();
        if (data.getMessage && p == data.gamePlayer) {
            data.playerMessage = chatEvent.getMessage();
            chatEvent.setCancelled(true);
            data.sendChat = true;
            saveData(mapId, data);
        }
            return true;
        });

    }

    private static void saveRanking(int mapId,double score){
        TypingData data = loadData(mapId);
        data.saveRanking = false;

        for (int i = 1;i<=5;i++){
            if (data.score >=config.getDouble("ranking.score."+i)){
                if (i !=5){
                    String p = config.getString("ranking.player."+i);
                    Double s = config.getDouble("ranking.score."+i);
                    int i_2 = i+1;
                    config.set("ranking.player."+i_2,p);
                    config.set("ranking.score."+i_2,s);
                }
                config.set("ranking.score."+i,score);
                config.set("ranking.player."+i,data.gamePlayer.getName());

                MappRenderer.saveAppConfig(appName,config);

                return;
            }

        }
    }
    private static void gameMain(Player player,int mapId,Graphics2D g){
        TypingData data = loadData(mapId);
        data.awayTime ++;

        if (data.awayTime<=80){
            countDown(g,data,player);
            return;
        }

        if (data.awayTime == config.getInt("AwayTime")){//○分立ったらタイトル
            finish(mapId);
            return;
        }

        if (data.TypingCount == TypingTimes){//○回クリアしたら終了
            MappDraw.drawImage(g,"typing_background",0,0,128,128);
            g.setColor(Color.black);
            data.playerMessage = "";

            g.setFont(new Font("", Font.PLAIN, 10));

            String typing = format("%.1f",data.wardCount/data.gameTime);
            String time = format("%.1f",data.gameTime);
            MappDraw.drawOutlineString(g,"タイム："+time,Color.WHITE,Color.YELLOW,10,30);
            MappDraw.drawOutlineString(g,"秒間"+typing+"typing",Color.WHITE,Color.YELLOW,10,40);
            data.score = (data.wardCount/data.gameTime)+TypingTimes*100-data.gameTime;
            String s = format("%.1f",data.score);

            MappDraw.drawOutlineString(g, format("スコア："+"%.1f",data.score),Color.WHITE,Color.YELLOW,10,50);
            MappDraw.drawOutlineString(g, "Player:"+player.getName(),Color.WHITE,Color.YELLOW,10,60);

            MappDraw.drawOutlineString(g,"ボタンを押すまで",Color.WHITE,Color.BLACK,5,80);
            MappDraw.drawOutlineString(g,"チャットを打てません",Color.WHITE,Color.BLACK,5,90);
            if (data.saveRanking){
                player.sendMessage(chatMessage+"§2終了！！");
                player.sendMessage(chatMessage+"§b§lPlayerName:"+player.getName()+",time:"+time+",typing"+typing+"typing/s"+",score:"+s);
                Double score = Double.parseDouble(s);

                saveRanking(mapId,score);
            }

            saveData(mapId,data);

            return;
        }
        data.gameTime +=0.1;//error
        if (enterMessage.size() <=1){
            System.out.println("error1");
            return;
        }

        if (data.TypingCheck) {//タイピング成功orスタート時
            MappDraw.drawImage(g,"typing_background",0,0,128,128);


            int checkNum = data.random;
            data.random  = new java.util.Random().nextInt(enterMessage.size() -1);
            if (checkNum==data.random){
                data.random  = new java.util.Random().nextInt(enterMessage.size() -1);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            MappDraw.drawOutlineString(g,"Player:"+player.getName(),Color.WHITE,Color.BLACK,10,90);
            MappDraw.drawOutlineString(g,enterMessage.get(data.random),Color.WHITE,Color.BLACK,10,50);
            data.TypingCheck = false;

        }
        if (data.sendChat) {//文字のチェック、失敗したときの処理
            if (data.playerMessage.equals(enterMessage.get(data.random))) {//判定、カウント、合ってなかったらスルー
                data.TypingCheck = true;
                data.TypingCount++;
                data.wardCount += data.playerMessage.length();

                player.sendMessage(chatMessage + "§2入力成功！");

            }if (!data.playerMessage.equals(enterMessage.get(data.random))){
                player.sendMessage(chatMessage + "§4打ち間違えています");
            }
            data.sendChat = false;
            data.playerMessage = "";

        }
        saveData(mapId,data);

    }
    private static void loadRanking(Graphics2D g) {
        MappDraw.drawImage(g,"typing_background",0,0,128,128);

        String th;
        Color ranking_color;

        g.setColor(Color.black);
        g.setFont(new Font( "SansSerif", Font.PLAIN,12));
        MappDraw.drawOutlineString(g,"<RANKING>",Color.white,Color.yellow,15,18);
        g.setFont(new Font( "SansSerif", Font.PLAIN,10));
        MappDraw.drawOutlineString(g,"Player/Score",Color.white,Color.yellow,18,30);
        g.setFont(new Font( "SansSerif", Font.PLAIN,12));


        for (int i = 1; i<=5; i++){
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
            MappDraw.drawOutlineString(g,i+th+"/"+config.getString("ranking.player."+i)+
                    "/"+config.getDouble("ranking.score."+i),Color.WHITE,ranking_color,2,i*13+32);

        }
    }
    private static void finish(int mapId){
        TypingData data = loadData(mapId);

        System.out.println("game finish");
        data.getMessage = false;
        data.title = true;
        data.TypingCount = 0;
        data.wardCount = 0;
        data.score = 0;
        data.gamePlayer = null;
        data.awayTime = 0;
        saveData(mapId,data);

    }
    private static void countDown(Graphics2D g,TypingData data ,Player player){
        g.setFont(new Font("", Font.PLAIN, 20));
        MappDraw.drawImage(g,"typing_background",0,0,128,128);
        World w = player.getWorld();//sounds

        if (data.awayTime==10||data.awayTime==30||data.awayTime==50){
            w.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 4, 2);

        }
        if (data.awayTime == 70){
            player.sendMessage(chatMessage+"§2ゲームスタート！");
            w.playSound(player.getLocation(),Sound.ENTITY_FIREWORK_SHOOT,8,2);
        }

        if (data.awayTime<=20){
            MappDraw.drawOutlineString(g,"③",Color.WHITE,Color.BLACK,54,70);
            return;
        }
        if (data.awayTime<=40){
            MappDraw.drawOutlineString(g,"②",Color.WHITE,Color.BLACK,54,70);
            return;
        }
        if (data.awayTime<=60){
            MappDraw.drawOutlineString(g,"①",Color.WHITE,Color.BLACK,54,70);
            return;
        }
        if (data.awayTime<=80){
            MappDraw.drawOutlineString(g,"START!",Color.WHITE,Color.BLACK,25,70);

        }

    }
}
