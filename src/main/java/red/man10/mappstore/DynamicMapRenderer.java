package red.man10.mappstore;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.sqrt;
import static org.bukkit.Bukkit.getServer;


//////////////////////////////////////////////////////////
//     DynamicMapRenderer
//                             created by takatronix.com
//      MIT Licence
//////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////
//    (1)      Setup
//    プラグインのonEnable()で　DynamicMapRenderer.setup(this)
//
//      pluginsfolder/images/
//      の下に画像をおくと、自動読み込みされます
//      0000.png => key: "0000"
//
//      (2) onEnable()などで描画関数登録

/*

        /////////////////////////////////////////////////
        //      マップの近くのボタンが押された時の処理
        DynamicMapRenderer.registerButtonEvent("game", (String key, int mapId) -> {

            //    true -> 描画更新
            return true;
        });

        /////////////////////////////////////////////////
        //      描画関数登録
        DynamicMapRenderer.register( "game", 0, (String key,int mapId, Graphics2D g) -> {

            //      背景を黒に
            g.setColor(Color.BLACK);
            g.fillRect(0,0,128,128);

            //      イメージキーを指定する
            //      pluginFolder/image/item0.png
            String imageKey = "item0";

            //      画像を描画
            DynamicMapRenderer.drawImage(g,imageKey,15,25,80,80);
            //      trueならMapへ転送する
            return true;
        });

 */



public class DynamicMapRenderer extends MapRenderer implements Listener {


    //////////////////////////////////////////////
    //      Singleton
    private static DynamicMapRenderer sharedInstance = new DynamicMapRenderer();
    private DynamicMapRenderer() {
        Bukkit.getLogger().info("DynamicMapRenderer created..");
    }
    public static DynamicMapRenderer getInstance() {
        return sharedInstance;
    }

    ///////////////////////////////////////////////
    //      描画関数インタフェース
    @FunctionalInterface
    public interface DrawFunction{
        boolean draw(String key,int mapId,Graphics2D g);
    }

    //      ボタンクリックイベント
    @FunctionalInterface
    public interface ButtonClickFunction{
        boolean onButtonClicked(String key,int mapId);
    }

    //     画面タッチ
    @FunctionalInterface
    public interface DisplayTouchFunction{
        boolean onDisplayTouch(String key,int mapId,int x,int y);
    }
    @EventHandler
    public void onItemInteract(PlayerInteractEntityEvent event){
        //           回転抑制用
        DynamicMapRenderer.onPlayerInteractEntityEvent(event);
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        //      イベントを通知してやる（ボタン検出用)
        DynamicMapRenderer.onPlayerInteractEvent(e);
    }
    ///////////////////////////////////////////////
    //      "key" ->　関数　をハッシュマップに保存
    static HashMap<String,DrawFunction> drawFunctions = new HashMap<String,DrawFunction>();
    static HashMap<String,Integer> drawRefreshTimeMap = new HashMap<String,Integer>();

    //
    static HashMap<String,ButtonClickFunction> buttonFunctions = new HashMap<String,ButtonClickFunction>();
    static HashMap<String,DisplayTouchFunction> touchFunctions = new HashMap<String,DisplayTouchFunction>();

    //        描画検索用
    static ArrayList<DynamicMapRenderer> renderers = new ArrayList<DynamicMapRenderer>();
    //      描画関数をキーを登録
    //      key: キー func: 描画関数 refreshIntervalTick:自動更新周期(1tick=1/20秒) 0で自動更新しない
    public static void register(String key,int refreshIntervalTick,DrawFunction func){
        drawRefreshTimeMap.put(key,refreshIntervalTick);
        drawFunctions.put(key,func);
    }
    //     ボタンクリックイベントを追加
    public static void registerButtonEvent(String key,ButtonClickFunction func){
        buttonFunctions.put(key,func);
    }

    //     タッチイベントを追加
    public static void registerDisplayTouchEvent(String key,DisplayTouchFunction func){
        touchFunctions.put(key,func);
    }


    //     キー
    String key = null;
    int    mapId = -1;
    //   オフスクリーンバッファを作成する
    //   高速化のためこのバッファに描画し、マップへ転送する
    BufferedImage bufferedImage = new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);

    //      画面リフレッシュサイクル:tick = 1/20秒
    public int refreshInterval = 0;

    //      一度だけ更新する
    public boolean refreshOnce = false;
    //      マップへ転送する
    public boolean updateMapFlag = false;
    //      描画時間
    public long drawingTime = 0;
    //      描画した回数
    public int updateCount = 0;
    //      bukkitからrenderコールされた回数
    public int renderCount = 0;
    //      デバッグ表示フラグ
    public boolean debugMode = true;


    //////////////////////////////////////
    //      描画関数&速度測定
    //////////////////////////////////////
    void draw(){
        //      関数をキーで取り出し実行
        DrawFunction func = drawFunctions.get(key);
        if(func != null){
            long startTime = System.nanoTime();
            //      描画関数をコール
            if(func.draw(key,mapId, bufferedImage.createGraphics())){
                updateMapFlag = true;
            }
            this.drawingTime =  System.nanoTime() - startTime;
           // Bukkit.getLogger().info("drawtime:"+key + ":"+drawingTime);
        }
    }


    int tickRefresh = 0;

    /////////////////////////////////
    //      Tickイベント
    //      描画更新があれば反映
    public void onTick(){

        if (refreshOnce){
            refreshOnce = false;
            draw();
        }

        this.refreshInterval =  drawRefreshTimeMap.getOrDefault(key,0);
        if(refreshInterval == 0){
            return ;
        }
        tickRefresh ++;
        //      インターバル期間をこえていたら画面更新
        if(tickRefresh >= refreshInterval) {
            draw();
            tickRefresh = 0;
        }

    }

    //////////////////////////////////////////////////////////////////////
    //    このイベントは本人がマップを持った場合1tick
    //    他者がみる場合は1secの周期でよばれるため高速描写する必要がある
    //    実際の画像はbufferdImageに作成し、このイベントで転送する
    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {

        //     オフスクリーンバッファからコピー
        if(updateMapFlag){
           // Bukkit.getLogger().info("rendering:"+this.key);
            canvas.drawImage(0,0,bufferedImage);
            updateMapFlag  = false;
            if(debugMode){
                //      描画回数を表示(debug)
                canvas.drawText( 4,4, MinecraftFont.Font, key + "/map:" + mapId);
                canvas.drawText(4, 14, MinecraftFont.Font, "update:"+updateCount +"/"+this.refreshInterval+"tick");
                canvas.drawText( 4,24, MinecraftFont.Font, "render:"+drawingTime+"ns");
            }
            updateCount++;
        }

        renderCount++;
    }

    static public boolean onPlayerInteractEntityEvent(PlayerInteractEntityEvent e){

        Entity ent = e.getRightClicked();
        if(ent instanceof ItemFrame){
            //  クリックしたアイテムフレームのアイテムがマップでなければ抜け
            ItemFrame frame = (ItemFrame) ent;
            ItemStack item = frame.getItem();
            if(item.getType() != Material.MAP) {
                return false;
            }

            //      DurabilityにいれてあるのがマップID
            int mapId = (int)item.getDurability();
            String key = findKey(mapId);
            if(key == null){
                return false;
            }
            Player player = e.getPlayer();

            //      たたいたブロック面
            BlockFace face = frame.getAttachedFace();

            //      叩いたブロック
            Block block = ent.getLocation().getBlock().getRelative(frame.getAttachedFace());
            Bukkit.getLogger().info(block.toString());
            World world = e.getPlayer().getWorld();

            //      叩いたブロックのBB
            BoundingBox bb = new BoundingBox(block);

            //     視線からのベクトルを得る
            RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());

            //      ディスプレイの　左上、右上をもとめる
            Vector topLeft = block.getLocation().toVector();
            Vector bottomRight = block.getLocation().toVector();
            topLeft.setY(topLeft.getY() + 1);
            if(face == BlockFace.WEST){
                topLeft.setZ( topLeft.getZ() + 1);
                topLeft.setX( topLeft.getX() + 1);
                bottomRight.setX(bottomRight.getX() +1);
            }
            if(face == BlockFace.SOUTH){
                topLeft.setX( topLeft.getX() + 1);
            }
            if(face == BlockFace.EAST){
                bottomRight.setZ(bottomRight.getZ() +1);
            }
            if(face == BlockFace.NORTH){
                bottomRight.setZ(bottomRight.getZ() +1);
                bottomRight.setX(bottomRight.getX() +1);
                topLeft.setZ( topLeft.getZ() + 1);
            }


            world.playEffect(topLeft.toLocation(world), Effect.COLOURED_DUST,0);
            world.playEffect(bottomRight.toLocation(world), Effect.COLOURED_DUST,0);

            //      視線とブロックの交差点
            Vector hit = rayTrace.positionOfIntersection(bb,3,0.01);
            if(hit != null){
                //      タッチした場所を光らす
                world.playEffect(hit.toLocation(world), Effect.COLOURED_DUST,0);

                double aDis = hit.distance(topLeft);
                Vector left = topLeft.setY(hit.getY());
                double xDis = hit.distance(left);
                double dx = (double)128 * xDis;

                double y = sqrt(aDis*aDis - xDis*xDis);
                double dy = (double)128 * y;


                int px = (int)dx;
                int py = (int)dy;

                //      タッチイベントを通知
                DisplayTouchFunction func =  touchFunctions.get(key);
                if(func != null){
                    if(func.onDisplayTouch(key,mapId,px,py)){
                        refresh(key);
                    }
                }

            }
            //      回転イベントをキャンセル
            e.setCancelled(true);
           return true;
        }
        return false;
    }
    ///////////////////////////////////////
    //      ボタンイベントを検出する
    static public int onPlayerInteractEvent(PlayerInteractEvent e){

        //      右ボタン以外は無視
        if(e.getAction()!=Action.RIGHT_CLICK_BLOCK) {
            return  -1;
        }
        //
        if(e.getClickedBlock()==null){
            return -1;
        }

        Block clickedBlock = e.getClickedBlock();
        Location loc = clickedBlock.getLocation();
        if(clickedBlock.getType()== Material.WOOD_BUTTON || clickedBlock.getType()== Material.STONE_BUTTON) {

            //     クリックしたボタンの近くのエンティティを集める
            Collection<Entity> entities = getNearbyEntities(loc,1);

            for (Entity en : entities) {
                //     アイテムフレーム以外は無視
                if (en instanceof ItemFrame != true) {
                    continue;
                }
                //     アイテムフレームにあるのはマップか？
                ItemFrame frame = (ItemFrame) en;
                ItemStack item = frame.getItem();
                if(item.getType() != Material.MAP) {
                    continue;
                }

                //      DurabilityにいれてあるのがマップID
                int mapId = (int)item.getDurability();
                String key = findKey(mapId);
                if(key == null){
                    continue;
                }


                //      ボタン用メソッドをコール
                ButtonClickFunction func = buttonFunctions.get(key);
                if(func != null){
                    Bukkit.getLogger().info("ボタンが押された => map key = "+key);
                    if(func.onButtonClicked(key,mapId)){
                        refresh(key);
                    }
                }
            }
        }
        return -1;
    }
    //          近くのエンティティを集める　
    public static List<Entity> getNearbyEntities(Location where, int range) {
        List<Entity> found = new ArrayList<Entity>();

        for (Entity entity : where.getWorld().getEntities()) {
            if (isInBorder(where, entity.getLocation(), range)) {
                found.add(entity);
            }
        }
        return found;
    }
    public static boolean isInBorder(Location center, Location notCenter, int range) {
        int x = center.getBlockX(), z = center.getBlockZ();
        int x1 = notCenter.getBlockX(), z1 = notCenter.getBlockZ();

        if (x1 >= (x + range) || z1 >= (z + range) || x1 <= (x - range) || z1 <= (z - range)) {
            return false;
        }
        return true;
    }


    /////////////////////////////////
    //          初期化
    /////////////////////////////////
    static public void setup(JavaPlugin plugin){

        DynamicMapRenderer instance = DynamicMapRenderer.getInstance();
        getServer().getPluginManager().registerEvents (instance,plugin);

        loadImages(plugin);
        setupMaps(plugin);
    }



    //////////////////////////////////////////////////////////////////////
    ///    サーバーシャットダウンでレンダラはは初期化されてしまうので
    ///    再起動後にマップを作成する必要がある　
    ///    プラグインのonEnable()で　DynamicMapRenderer.setupMaps(this)
    //     で初期化して設定をロードすること
    static void setupMaps(JavaPlugin plugin) {

        Configuration config = plugin.getConfig();
        if (config.getStringList("Maps").size() == 0) {
            return;
        }
        List<String> mlist = config.getStringList("Maps");
        List<String> nmlist = new ArrayList<String>();
        renderers.clear();

        for (String ids : mlist) {

            //      mapId,keyのデータを取得
            String[] split = ids.split(",");
            int id = Integer.parseInt(split[0]);
            String  key = ids;
            if(split.length == 2){
                 key = split[1];
            }

            //     mapIDから新規にマップを作成する
            MapView map = Bukkit.getMap((short) id);
            if (map == null) {
                map = Bukkit.createMap(Bukkit.getWorlds().get(0));
            }
            for (MapRenderer mr : map.getRenderers()) {
                map.removeRenderer(mr);
            }

            DynamicMapRenderer renderer = new DynamicMapRenderer();

            renderer.refreshOnce = true;
            renderer.refreshInterval = drawRefreshTimeMap.getOrDefault(key,0);
            renderer.key = key;
            renderer.mapId = id;
            renderer.initialize(map);

            //     レンダラを追加
            map.addRenderer(renderer);

            //     描画用に保存
            renderers.add(renderer);

            Bukkit.getLogger().info("setupMap: key:"+key + "id:"+id);
            nmlist.add(ids);
        }

        //      マップを保存し直す
        config.set("Maps", nmlist);
        plugin.saveConfig();

        ////////////////////////////////
        //      タイマーを作成する
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                DynamicMapRenderer.onTimerTick();;
            }
        }, 0, 1);

    }

    //////////////////////////////////////////
    /// 　   描画用マップを取得する
    ///     key : 描画を切り替えるためのキー
   static public ItemStack getMapItem(JavaPlugin plugin,String key) {


       if(drawFunctions.get(key) == null){
           return null;
       }


        Configuration config = plugin.getConfig();

        List<String> mlist = config.getStringList("Maps");

        ItemStack m = new ItemStack(Material.MAP);
        MapView map = Bukkit.createMap(Bukkit.getWorlds().get(0));

        //      mapID,keyのフォーマットで必要データを保存;
       int mapId = (int) map.getId();
        mlist.add(mapId + "," + key);

        //      設定データ保存
        config.set("Maps", mlist);
        plugin.saveConfig();

        for (MapRenderer mr : map.getRenderers()) {
            map.removeRenderer(mr);
        }

       DynamicMapRenderer renderer = new DynamicMapRenderer();
       renderer.key = key;
       renderer.refreshOnce = true;
       renderer.mapId = mapId;
       map.addRenderer(renderer);

       ItemMeta im = m.getItemMeta();
       im.addEnchant(Enchantment.DURABILITY, 1, true);
       m.setItemMeta(im);
       m.setDurability(map.getId());

       //       識別用に保存
       renderers.add(renderer);

       return m;
    }

    //      mapIdからキーを検索
    static String findKey(int mapId){
        for(DynamicMapRenderer renderer:renderers){
            if(renderer.mapId == mapId){
                return renderer.key;
            }
        }
        return null;
    }


    //      描画する
    //      一致したキーの数を返す
    static int refresh(String key){

        if(key == null){
            return 0;
        }
        int ret = 0;
        for(DynamicMapRenderer renderer:renderers){
            if(renderer.key.equals(key)){
                renderer.refreshOnce = true;
                ret++;
            }
        }

        return ret;
    }

    static  void onTimerTick() {
        for(DynamicMapRenderer renderer:renderers){
            renderer.onTick();
        }
    }
    static public void updateAll() {

        Bukkit.getLogger().info("UpdateAll");
        for(DynamicMapRenderer renderer:renderers){
            renderer.refreshOnce = true;
        }

        return ;
    }

    static public Graphics2D getGraphics(int mapId){
        for(DynamicMapRenderer renderer:renderers){
            if(renderer.mapId == mapId){
                return renderer.bufferedImage.createGraphics();
            }
        }
        return null;
    }

    //      イメージマップ　
    static HashMap<String,BufferedImage> imageMap =  new HashMap<String,BufferedImage>();


    ///////////////////////////////////////////////////
    //      プラグインフォルダの画像を読み込む
    static protected int loadImages(JavaPlugin plugin) {

        imageMap.clear();
        int ret = 0;
        File folder = new File(plugin.getDataFolder(), File.separator + "images");

        File[] files = folder.listFiles();
        if(files == null){
            Bukkit.getLogger().info("There is no images.");
            return 0;
        }
        for (File f : files) {
            if (f.isFile()){
                String filename = f.getName();

                if(filename.substring(0,1).equalsIgnoreCase(".")){
                    continue;
                }

                String key = filename.substring(0,filename.lastIndexOf('.'));
                BufferedImage image = null;

                try {
                    image = ImageIO.read(new File(f.getAbsolutePath()));
                    imageMap.put(key,image);
                    Bukkit.getLogger().info((key)+" registered.");
                    ret++;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    /////////////////////////////////////
    //       イメージを描画
    static Boolean drawImage( Graphics2D g,String imageKey,int x,int y,int w,int h){
        BufferedImage image = DynamicMapRenderer.image(imageKey);
        if(image == null){
            return false;
        }

        g.drawImage(image,x,y,w,h,null);

        return true;
    }

    /////////////////////////////////////
    //      キャッシュからイメージ取りだし
    static BufferedImage image(String  key){
        return imageMap.get(key);
    }
}