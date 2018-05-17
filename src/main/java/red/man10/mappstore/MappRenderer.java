package red.man10.mappstore;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
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
import java.util.*;
import java.util.List;

import static java.lang.Math.sqrt;
import static org.bukkit.Bukkit.getServer;

//////////////////////////////////////////////////////////
//     MappRenderer
//                             created by takatronix.com
//
//     https://github.com/takatronix/MappStore/
//      MIT Licence
//////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////
//    (1)      Setup
//    プラグインのonEnable()で　MappRenderer.setup(this)
//
//      pluginsfolder/images/
//      の下に画像をおくと、自動読み込みされます
//      0000.png => key: "0000"
//
//      (2) onEnable()などで描画関数登録


public class MappRenderer extends MapRenderer implements Listener {


    //////////////////////////////////////////////
    //      Singleton
    private static MappRenderer sharedInstance = new MappRenderer();
    private MappRenderer() {
        Bukkit.getLogger().info("MappRenderer created..");
    }
    public static MappRenderer getInstance() {
        return sharedInstance;
    }
    public static VaultManager vaultManager;


    ///////////////////////////////////////////////
    //      描画関数インタフェース
    @FunctionalInterface
    public interface DrawFunction{
        boolean draw(String key,int mapId,Graphics2D g);
    }
    @FunctionalInterface
    public interface InitFunction{
        boolean onInit(String key,int mapId);
    }

    //      ボタンクリックイベント
    @FunctionalInterface
    public interface ButtonClickFunction{
        boolean onButtonClicked(String key,int mapId,Player player);
    }
    //      ボタンクリックイベント
    @FunctionalInterface
    public interface PlatePushFunction{
        boolean onPlatePush(String key,int mapId,Player player);
    }


    //      ジャンプイベント（プレイヤーがマップを持ってジャンプした)
    @FunctionalInterface
    public interface PlayerJumpFunction{
        boolean onPlayerJumped(String key,int mapId,Player player);
    }

    //      スニークイベント（プレイヤーがマップを持ってジャンプした)
    @FunctionalInterface
    public interface PlayerSneakFunction{
        boolean onPlayerSneaked(String key,int mapId,Player player,boolean isSneaking);
    }

    //      プレイヤーの向きが変更
    @FunctionalInterface
    public interface PlayerYawFunction{
        boolean onPlayerYawChanged(String key,int mapId,Player player,double angle,double velocity);
    }

    @FunctionalInterface
    public interface PlayerPitchFunction{
        boolean onPlayerPitchChanged(String key,int mapId,Player player,double angle,double velocity);
    }


    //     画面タッチ
    @FunctionalInterface
    public interface DisplayTouchFunction{
        boolean onDisplayTouch(String key,int mapId,Player player,int x,int y);
    }
    @EventHandler
    public void onItemInteract(PlayerInteractEntityEvent event){
        //           回転抑制用
        MappRenderer.onPlayerInteractEntityEvent(event);
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        //      イベントを通知してやる（ボタン検出用)
        MappRenderer.onPlayerInteractEvent(e);
    }
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {

        //      プレイヤーがマップを持っていなければ抜け　
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() != Material.MAP) {
            return;
        }

        int mapID = (int)item.getDurability();



        Boolean isSneaking = player.isSneaking();


        String key = findKey(mapID);
        if(key == null){
            return;
        }
        PlayerSneakFunction func =  sneakFunctions.get(key);
        if(func != null){
            if(func.onPlayerSneaked(key,mapID,player,isSneaking)){
                refresh(key);
            }
        }

    }

    static HashMap<Player,Vector> userMovingVec = new HashMap<>();

    @EventHandler    public void onMove(PlayerMoveEvent e) {

        //      プレイヤーがマップを持っていなければ抜け　
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() != Material.MAP) {
            return;
        }

        int mapID = (int)item.getDurability();


        Vector lastMovingVec = userMovingVec.get(player);
        Vector movingVec = e.getFrom().toVector().subtract(e.getTo().toVector());
        userMovingVec.put(player,movingVec);



        if(lastMovingVec == null){
            return;
        }

        ////////////////////////////////////
        //      ジャンプした瞬間
        ////////////////////////////////////
        if(lastMovingVec.getY() == 0 && movingVec.getY() < 0){

            String key = findKey(mapID);
            if(key == null){
                return;
            }
            //      ジャンプイベントを通知
            PlayerJumpFunction func =  jumpFunctions.get(key);
            if(func != null){
                if(func.onPlayerJumped(key,mapID,player)){
                    refresh(key);
                }
            }
        }

    }

    ///////////////////////////////////////////////
    //      "key" ->　関数　をハッシュマップに保存
    static HashMap<String,DrawFunction> drawFunctions = new HashMap<>();
    static HashMap<String,Integer> drawRefreshTimeMap = new HashMap<>();


    //      初期化関数登録
    static HashMap<String,InitFunction> initfunctions = new HashMap<>();
    public static void init(String key,InitFunction func){
        initfunctions.put(key,func);


    }


    //
    static HashMap<String,ButtonClickFunction> buttonFunctions = new HashMap<>();
    static HashMap<String,DisplayTouchFunction> touchFunctions = new HashMap<>();

    //        描画検索用
    static ArrayList<MappRenderer> renderers = new ArrayList<MappRenderer>();
    //      描画関数をキーを登録
    //      key: キー func: 描画関数 refreshIntervalTick:自動更新周期(1tick=1/20秒) 0で自動更新しない
    public static void draw(String key,int refreshIntervalTick,DrawFunction func){
        drawRefreshTimeMap.put(key,refreshIntervalTick);
        drawFunctions.put(key,func);
    }
    //     ボタンクリックイベントを追加
    public static void buttonEvent(String key,ButtonClickFunction func){
        buttonFunctions.put(key,func);
    }

    //    plateイベントを追加
    static HashMap<String,PlatePushFunction> plateFunctions = new HashMap<>();
    public static void plateEvent(String key,PlatePushFunction func){
        plateFunctions.put(key,func);
    }


    //     タッチイベントを追加
    public static void displayTouchEvent(String key,DisplayTouchFunction func){
        touchFunctions.put(key,func);
    }

    //    PlayerJumpイベントを追加
    static HashMap<String,PlayerJumpFunction> jumpFunctions = new HashMap<>();
    public static void playerJumpEvent(String key,PlayerJumpFunction func){
        jumpFunctions.put(key,func);
    }
    //    PlayerSneakイベントを追加
    static HashMap<String,PlayerSneakFunction> sneakFunctions = new HashMap<>();
    public static void playerSneakEvent(String key,PlayerSneakFunction func){
        sneakFunctions.put(key,func);
    }
    //    Directionイベントを追加
    static HashMap<String,PlayerPitchFunction> pitchFunctions = new HashMap<>();
    public static void playerPitchEvent(String key,PlayerPitchFunction func){
        pitchFunctions.put(key,func);
    }
    static HashMap<String,PlayerYawFunction> yawFunctions = new HashMap<>();
    public static void playerYawEvent(String key,PlayerYawFunction func){
        yawFunctions.put(key,func);
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
    public boolean updateMapOnce = false;
    //      描画時間
    public long drawingTime = 0;
    //      描画した回数
    public int updateCount = 0;
    //      bukkitからrenderコールされた回数
    public int renderCount = 0;
    //      デバッグ表示フラグ
    static public boolean debugMode = false;


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
                updateMapOnce = true;
            }
            this.drawingTime =  System.nanoTime() - startTime;
            //Bukkit.getLogger().info("drawtime:"+key + ":"+drawingTime);
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
        if(updateMapOnce){
            canvas.drawImage(0,0,bufferedImage);
            updateMapOnce  = false;
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
            //Bukkit.getLogger().info(block.toString());
            World world = e.getPlayer().getWorld();

            //      叩いたブロックのBB
            BoundingBox bb = new BoundingBox(block);


            double rayDistance = 3;
            double rayAccuracy = 0.01;

            //     視線からのベクトルを得る
            RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
            //     ベクトル表示
            if(debugMode){
               // rayTrace.highlight(player.getWorld(),rayDistance,rayAccuracy);
            }

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


            if(debugMode){
                world.playEffect(topLeft.toLocation(world), Effect.COLOURED_DUST,0);
                world.playEffect(bottomRight.toLocation(world), Effect.COLOURED_DUST,0);
            }

            //      視線とブロックの交差点
            Vector hit = rayTrace.positionOfIntersection(bb,rayDistance,rayAccuracy);
            if(hit != null){
                //      タッチした場所を光らす
              //  world.playEffect(hit.toLocation(world), Effect.COLOURED_DUST,0);

                double aDis = hit.distance(topLeft);
                Vector left = topLeft.setY(hit.getY());
                double xDis = hit.distance(left);
                double y = sqrt(aDis*aDis - xDis*xDis);
                double dx = (double)128 * xDis;
                double dy = (double)128 * y;
              //  dx -= 4;
              //  dy -= 4;

                int px = (int)dx;
                int py = (int)dy;

               // player.sendMessage(px+","+py);

                //      タッチイベントを通知
                DisplayTouchFunction func =  touchFunctions.get(key);
                if(func != null){
                    if(func.onDisplayTouch(key,mapId,player,px,py)){
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



        Block clickedBlock = e.getClickedBlock();
        if(clickedBlock == null){
            return -1;
        }
        Location loc = clickedBlock.getLocation();

        /////////////////////////////////////////////////////
        //      プレートを踏んだ
        if(clickedBlock.getType()== Material.STONE_PLATE
                || clickedBlock.getType()== Material.GOLD_PLATE
                || clickedBlock.getType()== Material.IRON_PLATE
                ){

            // Bukkit.getLogger().info("踏んだ ");

             Collection<Entity> entities = getNearbyEntities(loc,2);


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
                PlatePushFunction func = plateFunctions.get(key);
                if(func != null){
                    Bukkit.getLogger().info("プレートが踏まれた => map key = "+key);
                    if(func.onPlatePush(key,mapId,e.getPlayer())){
                        refresh(key);
                    }
                }
            }


            return -1;
        }

        //      右ボタン以外は無視
        if(e.getAction()!=Action.RIGHT_CLICK_BLOCK) {
            return  -1;
        }
        //
        if(e.getClickedBlock()==null){
            return -1;
        }


        if(     clickedBlock.getType()== Material.WOOD_BUTTON
                || clickedBlock.getType()== Material.STONE_BUTTON
                /*
                ||   clickedBlock.getType()== Material.STONE_PLATE
                || clickedBlock.getType()== Material.GOLD_PLATE
                || clickedBlock.getType()== Material.IRON_PLATE
*/
                ) {



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
                    if(func.onButtonClicked(key,mapId,e.getPlayer())){
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


    static JavaPlugin plugin = null;
    /////////////////////////////////
    //          初期化
    /////////////////////////////////
    static public void setup(JavaPlugin plugin){

        MappRenderer instance = MappRenderer.getInstance();
        getServer().getPluginManager().registerEvents (instance,plugin);


        loadImages(plugin);
        setupMaps(plugin);
        MappRenderer.plugin = plugin;
        vaultManager = new VaultManager(plugin);
    }



    //////////////////////////////////////////////////////////////////////
    ///    サーバーシャットダウンでレンダラはは初期化されてしまうので
    ///    再起動後にマップを作成する必要がある　
    ///    プラグインのonEnable()で　MappRenderer.setupMaps(this)
    //     で初期化して設定をロードすること
    static void setupMaps(JavaPlugin plugin) {

        Configuration config = plugin.getConfig();
        if (config.getStringList("Maps").size() == 0) {
            return;
        }
        List<String> mlist = config.getStringList("Maps");
        List<String> nmlist = new ArrayList<String>();
        renderers.clear();


        Bukkit.getLogger().info("setupMaps --------------------------");
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

            MappRenderer renderer = new MappRenderer();

            renderer.updateMapOnce = true;
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
                MappRenderer.onTimerTick();;
            }
        }, 0, 1);

    }

    static public List<String>getAppList(){
        List<String> list = new ArrayList<>(drawFunctions.keySet());
       return list;
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

       MappRenderer renderer = new MappRenderer();
       renderer.key = key;
       renderer.refreshOnce = true;
       renderer.updateMapOnce = true;
       renderer.mapId = mapId;
       map.addRenderer(renderer);




       ItemMeta im = m.getItemMeta();
       im.addEnchant(Enchantment.DURABILITY, 1, true);
       m.setItemMeta(im);
       m.setDurability(map.getId());

       //       識別用に保存
       renderers.add(renderer);
       //      初期化を呼ぶ　
       InitFunction func = initfunctions.get(key);
       if(func != null) {
           if(func.onInit(key,mapId)){
               refresh(key);
           }
       }

       return m;
    }

    //      mapIdからキーを検索
    static String findKey(int mapId){
        for(MappRenderer renderer:renderers){
            if(renderer.mapId == mapId){
                return renderer.key;
            }
        }
        return null;
    }


    //      描画する
    //      一致したキーの数を返す
    static public int refresh(String key){

        if(key == null){
            return 0;
        }
        int ret = 0;
        for(MappRenderer renderer:renderers){
            if(renderer.key.equals(key)){
                renderer.refreshOnce = true;
                ret++;
            }
        }

        return ret;
    }
    //      描画する
    //      一致したキーの数を返す
    static public int updateMap(String key){

        if(key == null){
            return 0;
        }
        int ret = 0;
        for(MappRenderer renderer:renderers){
            if(renderer.key.equals(key)){
                renderer.updateMapOnce = true;
                ret++;
            }
        }

        return ret;
    }

    static public void initAllMaps() {

        Bukkit.getLogger().info("initAllMap");
        for(MappRenderer renderer:renderers){
            InitFunction func = initfunctions.get(renderer.key);
            if(func == null){
                continue;
            }
            //      初期化
            func.onInit(renderer.key,renderer.mapId);

        }

    }



    static HashMap<Player,Location> lastLocationMap = new HashMap<>();
    static HashMap<Player,Double> lastPitchMap = new HashMap<>();
    static HashMap<Player,Double> lastYawMap = new HashMap<>();

    static  void onTimerTick() {

        ///////////////////////////////////////////////////
        //      向きの違いから検出しマウスのベロシティを求める
        for(Player p:Bukkit.getOnlinePlayers()){
            ItemStack item = p.getInventory().getItemInMainHand();
            if(item.getType() != Material.MAP){
                continue;
            }
            int mapID = item.getDurability();
            String key = findKey(mapID);
            if(key == null){
                continue;
            }

            Location lastLocation = lastLocationMap.get(p);


            Location location = p.getLocation();
            lastLocationMap.put(p,location);
            if(lastLocation == null){
                continue;
            }


            double pitch1 = location.getPitch();
            double pitch2 = lastLocation.getPitch();
            double pitchVelocity = pitch1 - pitch2;


            Double lastPitch = lastPitchMap.getOrDefault(p,(Double)0.0);
            if(pitchVelocity != lastPitch){
                PlayerPitchFunction func = pitchFunctions.get(key);
                if(func != null){
                    if(func.onPlayerPitchChanged(key,mapID,p,pitch1,pitchVelocity)){
                        refresh(key);
                    }
                }

                lastPitchMap.put(p,(Double)pitchVelocity);
            }




            //      Ya左右の向き
            double yaw1 = location.getYaw();
            double yaw2 = lastLocation.getYaw();
            double yaw1Normalized = (yaw1 < 0) ? yaw1 + 360 : yaw1;
            double yaw2Normalized = (yaw2 < 0) ? yaw2 + 360 : yaw2;
            double velocity = yaw1Normalized - yaw2Normalized;
            if(velocity > 300){
                velocity = yaw2Normalized + (360 - yaw1Normalized);
            }else if (velocity < -300){
                velocity = yaw1Normalized - (360 - yaw2Normalized);
            }

            Double lastVelocity = lastYawMap.getOrDefault(p,(Double)0.0);
            if(lastVelocity != velocity){
                PlayerYawFunction func = yawFunctions.get(key);
                if(func != null){
                    if(func.onPlayerYawChanged(key,mapID,p,yaw1Normalized,velocity)){
                        refresh(key);
                    }
                }

                lastYawMap.put(p,(Double)velocity);
            }
        }

        //      マップごとのTick処理
        for(MappRenderer renderer:renderers){
            renderer.onTick();
        }




    }
    static public void updateAll() {

        Bukkit.getLogger().info("UpdateAll");
        for(MappRenderer renderer:renderers){
            renderer.refreshOnce = true;
            renderer.updateMapOnce = true;
        }

        return ;
    }

    
    static public Graphics2D getGraphics(int mapId){
        for(MappRenderer renderer:renderers){
            if(renderer.mapId == mapId){
                return renderer.bufferedImage.createGraphics();
            }
        }
        Bukkit.getLogger().warning("mapID"+mapId+"がみつからない！");
        return null;
    }

    //      イメージマップ　
    static HashMap<String,BufferedImage> imageMap =  new HashMap<String,BufferedImage>();



    static public int listFolder(String directoryName,boolean subDir, ArrayList<File> files) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                if(subDir){
                    listFolder(file.getAbsolutePath(), subDir,files);
                }
            }
        }
        return files.size();
    }

    public static FileConfiguration getAppConfig(String appName){
        String path = plugin.getDataFolder()+"/"+appName+".yml";
        FileConfiguration data = YamlConfiguration.loadConfiguration(new File(path));
        return data;
    }
    public static boolean saveAppConfig(String appName,FileConfiguration config){
        String path = plugin.getDataFolder()+"/"+appName+".yml";
        try{
            config.save(path);
        }catch (Exception e){
            return false;
        }

        return true;
    }


    ///////////////////////////////////////////////////
    //      プラグインフォルダの画像を読み込む
    static protected int loadImages(JavaPlugin plugin) {

        imageMap.clear();
        int ret = 0;
        File folder = new File(plugin.getDataFolder(), File.separator + "images");

        ArrayList<File> files = new  ArrayList<File>();
        listFolder(plugin.getDataFolder()+"/images",true,files);

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
    //      キャッシュからイメージ取りだし
    static BufferedImage image(String  key){
        return imageMap.get(key);
    }
}