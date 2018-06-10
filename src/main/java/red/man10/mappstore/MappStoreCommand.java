package red.man10.mappstore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MappStoreCommand implements CommandExecutor {

    final String adminPermision = "red.man10.mapp.admin";
    final String getPermision = "red.man10.mapp.get";

    final String  permissionErrorString = "§4§lYou don't have permission.";



    private final MappStorePlugin plugin;

    //      コンストラクタ
    public MappStoreCommand(MappStorePlugin plugin) {
        this.plugin = plugin;
    }





    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if((sender instanceof Player) == false) {
            sender.sendMessage("Can't use in console.");
            return false;
        }


        Player p = (Player) sender;

        if(args.length == 0){
            showHelp(sender);
            return false;
        }

        String command = args[0];

        if(command.equalsIgnoreCase("send")) {
            if (args.length == 2) {
                plugin.sendMap(p,args[1]);
                return true;
            }

            p.sendMessage("/mapp send [mapid]");
            return false;
        }

        if(command.equalsIgnoreCase("get")){
            if(!p.hasPermission(getPermision)){
                p.sendMessage(permissionErrorString);
                return false;
            }

            if(args.length == 2){
                plugin.giveMap(p,args[1]);
                return true;
            }
            p.sendMessage("§e/mapp get [mAppkey]");
            return false;
        }
        if(command.equalsIgnoreCase("list")) {
            plugin.showList(p);
            return true;
        }
        return true;
    }

    void showHelp(CommandSender p){
        p.sendMessage("§e§l Minecraft Mapp App Store / created by takatronix.com");
        p.sendMessage("§e§l supported at 'man10.red' <-- Japanese Minecraft Server");

        p.sendMessage("§e/mapp list / show loaded mapp apps.");
        p.sendMessage("§e/mapp get [mappkey] / get mapp app");

    }
}