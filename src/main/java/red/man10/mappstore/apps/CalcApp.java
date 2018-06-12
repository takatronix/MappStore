package red.man10.mappstore.apps;

import org.bukkit.entity.Player;
import red.man10.mappstore.MappApp;
import red.man10.mappstore.MappRenderer;

import java.awt.*;
import java.util.HashMap;


public class CalcApp extends MappApp{
    static final int  drawRefreshCycle = 0;
    static final String appName = "calc";
    static final String chatMassage = "§2§l[calculator]";


     static class CalcMappData{
         boolean operator;//演算子を使うかどうか
         double result;//計算結果
         double addNumber;//追加する値
         String addNumber2 = "0";//追加する値2
         String ope = "";
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


            g.setColor(Color.lightGray);
            g.fillRect(0,0,128,128);
            g.setColor(Color.BLACK);
            g.fillRect(0,0,2,128);
            g.fillRect(126,0,2,128);
            g.fillRect(0,125,128,3);

            if (!data.operator){
                keyNumber(g);//テンキー

            }
            if (data.operator){
                calcDisplay(g);//四則キー

            }
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,12);

            g.setColor(Color.WHITE);
            g.setFont(new Font( "SansSerif", Font.BOLD ,10));

            if (data.resultNumber){
                String put = Double.toString(data.result);
                g.drawString("result:"+put,10,10);

            }else {
                g.drawString(data.addNumber2,10,10);
            }

            g.drawString(data.ope,90,10);

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


    private static void calc(int mapId,double number,Player player){
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
                player.sendMessage(chatMassage+"§40で割ることはできません 計算をスルーします");
                player.sendMessage(chatMassage+"§4Division by 0 is not possible.");
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
        data.ope = "";

        player.sendMessage(chatMassage+"§3結果："+data.result);
        player.sendMessage(chatMassage+"§3Result:"+data.result);

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
            if(x<64&&y>14&&y<52){//plus
                data.plus = true;
                data.calc = true;
                data.ope = "+";
            }
            if(x<64&&y>54&&y<92){//minus
                data.minus = true;
                data.calc = true;
                data.ope = "-";

            }
            if(x>64&&y>14&&y<52){//power
                data.power = true;
                data.calc = true;
                data.ope = "x";

            }
            if(x>64&&y>54&&y<92){//division
                data.division = true;
                data.calc = true;
                data.ope = "/";
            }
            if (x>14&&x<64&&y>96){//equal
                data.resultNumber = true;
            }

            if (x>65&&y>96){//Cancel
            }
            data.operator = false;

            saveData(mapId,data);
            return;

        }
        if (!data.operator) {

            if (data.addNumber2 =="0"){
                data.addNumber2 ="";
            }

            if (x > 13 && x < 46 && y > 14 && y < 32) {//Backspace

                try {
                    data.addNumber2 = data.addNumber2.substring(0, data.addNumber2.length() - 1);

                }catch (java.lang.StringIndexOutOfBoundsException e){
                    player.sendMessage(chatMassage+"§4エラー：値はありません");
                    player.sendMessage(chatMassage+"§4Error:There is no value.");
                }

            }
            if (x > 13 && x < 46 && y > 34 && y < 52) {//1
                data.addNumber2 += 1;
            }
            if (x > 13 && x < 46 && y > 54 && y < 72) {//4
                data.addNumber2 += 4;

            }
            if (x > 13 && x < 46 && y > 74 && y < 92) {//7
                data.addNumber2 += 7;

            }
            if (x > 13 && x < 46 && y > 94 && y < 112) {//0
                data.addNumber2 += 0;
            }
            //2
            if (x > 50 && x < 80 && y > 14 && y < 32) {//Delete
                player.sendMessage(chatMassage+"§3数字をすべて削除しました");
                player.sendMessage(chatMassage+"§3Deleted all value.");
                data.addNumber2 = "0";
                data.result = 0;
                data.calc = false;
                data.resultNumber = false;
                data.division = false;
                data.ope = "";
                data.minus = false;
                data.plus = false;
                data.power = false;
                data.operator = false;
            }
            if (x > 50 && x < 80 && y > 34 && y < 52) {//2
                data.addNumber2 += 2;
            }
            if (x > 50 && x < 80 && y > 54 && y < 72) {//5
                data.addNumber2 += 5;
            }
            if (x > 50 && x < 80 && y > 74 && y < 92) {//8
                data.addNumber2 += 8;
            }
            if (x > 50 && x < 80 && y > 94 && y < 112) {//calc

                if (data.addNumber2 ==""||data.addNumber2 ==null){
                    data.addNumber2 ="0";
                    return;
                }
                data.addNumber = Double.parseDouble(data.addNumber2);


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
            //3
            if (x > 84 && x < 112 && y > 14 && y < 32) {//result
                data.resultNumber = !data.resultNumber;
                if (data.addNumber2 == ""){
                    data.addNumber2 ="0";
                }
            }
            if (x > 84 && x < 112 && y > 34 && y < 52) {//3
                data.addNumber2+=3;
            }
            if (x > 84 && x < 112 && y > 54 && y < 72) {//6
                data.addNumber2+=6;
            }
            if (x > 84 && x < 112 && y > 74 && y < 92) {//9
                data.addNumber2+=9;
            }
            if (x > 84 && x < 112 && y > 94 && y < 112) {//equal
                if (data.addNumber2 ==""||data.addNumber2 ==null){
                    data.addNumber2 ="0";
                    return;
                }

                data.addNumber = Double.parseDouble(data.addNumber2);
                calc(mapId,data.addNumber,player);
                data.resultNumber = true;

            }
            if (data.addNumber2.length() == 0) {
                data.addNumber2 = "0";
            }
                saveData(mapId,data);

        }

    }


}
