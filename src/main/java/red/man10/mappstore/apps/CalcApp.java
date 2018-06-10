package red.man10.mappstore.apps;

import org.bukkit.entity.Player;
import red.man10.mappstore.MappApp;
import red.man10.mappstore.MappRenderer;

import java.awt.*;
import java.util.HashMap;


public class CalcApp extends MappApp{
    static int  drawRefreshCycle = 0;
    static final String appName = "calc";


     static class CalcMappData{
         boolean operator;//演算子を使うかどうか
         long result;//計算結果
         long addNumber;//追加する値
         String addNumber2 = "0";//追加する値2
         boolean plus;//和
         boolean minus;//差
         boolean power;//積
         boolean division;//商
         boolean calc;
         boolean resultNumber;
    }
    static HashMap<Integer,CalcMappData> hashMap = new  HashMap<Integer,CalcMappData>();

    //      ユーザーデーター保存
    static CalcMappData loadData(int mapId) {
        CalcMappData data = hashMap.get(mapId);
        if(data == null){
            data = new CalcMappData();
        }
        return data;
    }
    //      ユーザーデータ読み込
    static CalcMappData saveData(int mapId, CalcMappData data){
        return hashMap.put(mapId,data);
    }


    static public void register(){

        MappRenderer.draw( appName, drawRefreshCycle, (String key, int mapId,Graphics2D g) -> {

            CalcMappData data = loadData(mapId);


            g.setColor(Color.WHITE);
            g.fillRect(0,0,128,128);
            if (!data.operator){
                keyNumber(g);//テンキー

            }
            if (data.operator){
                calcDisplay(g);//四則キー

            }
            g.setColor(Color.BLACK);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            if (data.resultNumber){
                g.drawString("result:"+data.result,10,10);

            }else {
                g.drawString(data.addNumber2,10,10);
            }

            return true;
        });

        MappRenderer.displayTouchEvent(appName, (String key, int mapId, Player player, int x, int y) -> {
            touchKey(mapId,x,y,player);
            return true;
        });

    }
    private static void keyNumber(Graphics2D g){
        g.setColor(Color.lightGray);
        int[] x = {16,16,16,16,16,49,49,49,49,49,82,82,82,82,82};
        int[] y = {16,35,54,73,92,16,35,54,73,92,16,35,54,73,92};
        for (int i = 0; x.length>i; i++){
            g.fill3DRect(x[i],y[i],32,18,true);

        }
        int[] xWord = {23,50,85,28,62,95,28,62,95,28,62,95,28,52,95};
        int[] yWord = {28,28,28,47,47,47,67,67,67,86,86,86,106,106,106};
        String[] word = {"|←","Delete","result","1","2","3","4","5","6","7","8","9","0","+-x÷","="};
        g.setColor(Color.BLACK);
        g.setFont(new Font( "SansSerif", Font.BOLD ,10));
        for(int j = 0;word.length>j;j++){
            g.drawString(word[j],xWord[j],yWord[j]);
        }


    }


    private static void calc(int mapId,long number,Player player){
        CalcMappData data = loadData(mapId);
        if(data.plus){
            data.result+=number;
            data.plus = false;
        }
        if (data.minus){
            data.result-=number;
            data.minus = false;
        }
        if (data.power){
            data.result = data.result*number;
            data.power=false;
        }
        if (data.division){
            if (number==0){
                player.sendMessage("§2§l[calculator]§40で割ることはできません 計算をスルーします");
                data.division = false;
                data.operator = false;
                return;
            }
            data.result/=number;
            data.division=false;
        }
        data.operator =false;
        data.addNumber = 0;
        data.addNumber2 = "0";
        data.calc = false;
        player.sendMessage("結果："+data.result);

        //      Save Data
        saveData(mapId,data);
    }
    private static void calcDisplay(Graphics2D g){
        g.setColor(Color.lightGray);
        int[] yCoo = {16,16,54,54};
        int[] xCoo = {16,64,16,64};
        for (int i = 0;xCoo.length>i;i++){
            g.fill3DRect(xCoo[i],yCoo[i],52,38,true);
        }
        g.fill3DRect(16,93,52,16,true);
        g.fill3DRect(64,93,52,16,true);


        g.setColor(Color.BLACK);
        g.setFont(new Font( "SansSerif", Font.BOLD ,25));
        int[] xCoo2 = {34,34,85,85,35,73};
        int[] yCoo2 = {52,85,52,85,104,104};
        String[] word = {"+","-","×","÷","=","Cancel"};
        for(int j = 0;word.length>j;j++){
            if(j>=4){
                g.setFont(new Font( "SansSerif", Font.BOLD ,10));
            }
            g.drawString(word[j],xCoo2[j],yCoo2[j]);
        }
    }
    private static void touchKey(int mapId,int x,int y,Player player){
        CalcMappData data = loadData(mapId);
        if (data.operator){
            if(x<64&&y>14&&y<52){
                player.sendMessage("+");
                data.plus = true;
                data.calc = true;
            }
            if(x<64&&y>54&&y<92){
                player.sendMessage("-");
                data.minus = true;
                data.calc = true;

            }
            if(x>64&&y>14&&y<52){
                player.sendMessage("x");
                data.power = true;
                data.calc = true;

            }
            if(x>64&&y>54&&y<92){
                player.sendMessage("÷");
                data.division = true;
                data.calc = true;
            }
            if (x>14&&x<64&&y>96){
                player.sendMessage("=");
                data.resultNumber = true;
            }

            if (x>65&&y>96){//Cancel
                player.sendMessage("cancel");
            }
            data.operator = false;

            saveData(mapId,data);
            return;

        }
        if (!data.operator) {
            if (x > 13 && x < 46 && y > 14 && y < 32) {
                player.sendMessage("backspace");
                try {
                    data.addNumber2 = data.addNumber2.substring(0, data.addNumber2.length() - 1);
                }catch (java.lang.StringIndexOutOfBoundsException e){
                    player.sendMessage("§l§4エラー：値はありません");
                }
            }
            if (x > 13 && x < 46 && y > 34 && y < 52) {
                player.sendMessage("1");
                data.addNumber2 += 1;
            }
            if (x > 13 && x < 46 && y > 54 && y < 72) {
                player.sendMessage("4");
                data.addNumber2 += 4;

            }
            if (x > 13 && x < 46 && y > 74 && y < 92) {
                player.sendMessage("7");
                data.addNumber2 += 7;

            }
            if (x > 13 && x < 46 && y > 94 && y < 112) {
                player.sendMessage("0");
                data.addNumber2 += 0;
            }
            //2列目
            if (x > 50 && x < 80 && y > 14 && y < 32) {
                player.sendMessage("delete");
                data.addNumber2 = "0";
                data.result = 0;
                data.calc = false;
                data.resultNumber = false;
            }
            if (x > 50 && x < 80 && y > 34 && y < 52) {
                player.sendMessage("2");
                data.addNumber2 += 2;
            }
            if (x > 50 && x < 80 && y > 54 && y < 72) {
                player.sendMessage("5");
                data.addNumber2 += 5;
            }
            if (x > 50 && x < 80 && y > 74 && y < 92) {
                player.sendMessage("8");
                data.addNumber2 += 8;
            }
            if (x > 50 && x < 80 && y > 94 && y < 112) {
                data.addNumber = Long.parseLong(data.addNumber2);
                player.sendMessage("計算");
                if (data.calc) {
                    calc(mapId,data.addNumber, player);
                }
                if (data.result == 0){
                    data.result = data.addNumber;
                }
                data.addNumber2 = "0";
                data.operator = true;
                saveData(mapId,data);

                return;
            }
            //3列目
            if (x > 84 && x < 112 && y > 14 && y < 32) {
                player.sendMessage("result");
                data.resultNumber = !data.resultNumber;
            }
            if (x > 84 && x < 112 && y > 34 && y < 52) {
                player.sendMessage("3");
                data.addNumber2+=3;
            }
            if (x > 84 && x < 112 && y > 54 && y < 72) {
                player.sendMessage("6");
                data.addNumber2+=6;
            }
            if (x > 84 && x < 112 && y > 74 && y < 92) {
                player.sendMessage("9");
                data.addNumber2+=9;
            }
            if (x > 84 && x < 112 && y > 94 && y < 112) {
                player.sendMessage("=");
                data.addNumber = Long.parseLong(data.addNumber2);
                calc(mapId,data.addNumber,player);
                data.resultNumber = true;

            }

            saveData(mapId,data);

        }

    }


}
