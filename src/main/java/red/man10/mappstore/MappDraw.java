package red.man10.mappstore;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MappDraw {


    /////////////////////////////////////
    //       イメージを描画
    static public Boolean drawImage( Graphics2D g,String imageKey,int x,int y,int w,int h){
        BufferedImage image = MappRenderer.image(imageKey);
        if(image == null){
            return false;
        }

        g.drawImage(image,x,y,w,h,null);

        return true;
    }
    //////////////////////////////////
    //袋文字 関数 / draw Outline String
    static public void drawOutlineString(Graphics2D g,String text, Color text_color, Color outline_color, int x, int y){

        int[] sx = { 1,1,1,0,0,-1,-1,-1};
        int[] sy = { 1,0,-1,1,-1,1,0,-1};
        g.setColor(outline_color);

        for(int i=0;i<8;i++){
            g.drawString(text,x+sx[i],y+sy[i]);
        }
        g.setColor(text_color);
        g.drawString(text, x, y);

    }
}
