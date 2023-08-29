package de.timecoding.delayedtnt.command;

import de.timecoding.delayedtnt.DelayedTNT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TNTRainCommand implements CommandExecutor {

    private String help = "§cPlease use the following command: §e/tntrain AMOUNT DELAY FUSE (PLAYERNAME)";
    private DelayedTNT plugin;

    public TNTRainCommand(DelayedTNT plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 4 || strings.length == 3){
            if(isInteger(strings[0]) && Integer.parseInt(strings[0]) >= 1){
                if(isDouble(strings[1]) && (int)Double.parseDouble(strings[1]) >= 0){
                    if(isDouble(strings[2]) && (int)Double.parseDouble(strings[2]) >= 0){
                        Integer tnt = Integer.parseInt(strings[0]);
                        Double delay = Double.parseDouble(strings[1]);
                        Double fuse = Double.parseDouble(strings[2]);
                        if(strings.length == 4) {
                            tntRain(commandSender, strings, tnt, delay, fuse);
                        }else if(strings.length == 3){
                            this.plugin.setDelay(delay);
                            this.plugin.setFuse(fuse);
                            if(commandSender instanceof Player){
                                tntRain(commandSender, strings, tnt, delay, fuse);
                            }else{
                                commandSender.sendMessage("§cYou need to specify a player in order to spawn a tntrain!");
                            }
                        }else{
                            commandSender.sendMessage(help);
                        }
                    }else{
                        commandSender.sendMessage("§cThe fuse-time must be a number over -1");
                    }
                }else{
                    commandSender.sendMessage("§cThe delay in seconds must be a number over -1");
                }
            }else{
                commandSender.sendMessage("§cThe TNT-Amount must be a number over 0");
            }
        }else{
            commandSender.sendMessage(help);
        }
        return false;
    }

    private void tntRain(CommandSender commandSender, String[] strings, Integer tnt, Double delay, Double fuse) {
        Player target = null;
        if(strings.length == 4){
            target = Bukkit.getPlayer(strings[3]);
        }else if(commandSender instanceof Player){
            target = (Player) commandSender;
        }
        if (target != null && target.isOnline()) {
            this.plugin.setDelay(delay);
            this.plugin.setFuse(fuse);
            if (messageEnabled()) {
                commandSender.sendMessage("§aSuccessfully started a §cTNTRain §awith §e" + tnt + " §cTNT§a, the delay §e" + delay + " §aand the fuse §e" + fuse + " §afor the player §e" + target.getName() + "!");
            }
            while (tnt > 0) {
                tnt--;
                startRain(target.getLocation());
            }
            plugin.restart();
        } else {
            commandSender.sendMessage("§cThis player isn't online or does not exist!");
        }
    }

    private boolean messageEnabled(){
        if(this.plugin.getConfigHandler().keyExists("DisableMessage")){
            return !(this.plugin.getConfigHandler().getBoolean("DisableMessage"));
        }
        return true;
    }

    public void startRain(Location from){
        int x = 5;
        if(plugin.getConfigHandler().keyExists("TNTRain.MaxBlocksInX")){
            x = plugin.getConfigHandler().getInteger("TNTRain.MaxBlocksInX");
        }
        int z = 5;
        if(plugin.getConfigHandler().keyExists("TNTRain.MaxBlocksInZ")){
            z = plugin.getConfigHandler().getInteger("TNTRain.MaxBlocksInZ");
        }
        Integer inx = getRandom(x);
        Integer inz = getRandom(z);
        Location location = from.clone().add(0, plugin.getConfigHandler().getInteger("TNTRain.AddY"), 0);
            if (inx < 0) {
                location = location.clone().subtract(inx, 0, 0);
            } else {
                location = location.clone().add(inx, 0, 0);
            }
            if (inz < 0) {
                location = location.clone().subtract(0, 0, inz);
            } else {
                location = location.clone().add(0, 0, inz);
            }
        this.plugin.getLocationMap().put(location, 1);
    }

    private Integer getRandom(Integer in){
        Random random = new Random();
        String string = "";
        if(random.nextBoolean()){
            string = "-";
        }
        return Integer.parseInt(string+String.valueOf((new Random().nextInt((in+1)))));
    }

    private boolean isInteger(String string){
        try {
            Integer.parseInt(string);
            return true;
        }catch (NumberFormatException exception){
            return false;
        }
    }

    private boolean isDouble(String string){
        try {
            Double.parseDouble(string);
            return true;
        }catch (NumberFormatException exception){
            return false;
        }
    }
}
