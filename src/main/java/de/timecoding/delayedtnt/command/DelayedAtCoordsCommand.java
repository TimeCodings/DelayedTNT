package de.timecoding.delayedtnt.command;

import de.timecoding.delayedtnt.DelayedTNT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DelayedAtCoordsCommand implements CommandExecutor {

    private DelayedTNT plugin;

    public DelayedAtCoordsCommand(DelayedTNT plugin){
        this.plugin = plugin;
    }

    private String syntax = "§cPlease use the right command: /tntatcoords X Y Z WORLD AMOUNT DELAY FUSE PLAYERORNONE";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 7 || strings.length == 8){
            if(isValid(strings[0]) && isValid(strings[1]) && isValid(strings[2])){
                AtomicBoolean validWorld = new AtomicBoolean(false);
                Bukkit.getWorlds().forEach(world -> {
                    if(world.getName().equalsIgnoreCase(strings[3]) || validPlayer(strings[7])){
                        validWorld.set(true);
                    }
                });
                if(validWorld.get()){
                    if(isInteger(strings[4])){
                        if(isDouble(strings[5]) && (int)Double.parseDouble(strings[5]) > 0){
                            if(isDouble(strings[6]) && (int)Double.parseDouble(strings[6]) > 0){
                                Integer tnt = Integer.parseInt(strings[4]);
                                Double delay = Double.parseDouble(strings[5]);
                                Double fuse = Double.parseDouble(strings[6]);
                                if(strings.length == 8 && validPlayer(strings[7])) {
                                    Location location = this.getLocation(strings[0], strings[1], strings[2], true, Bukkit.getPlayer(strings[7]));
                                    Player target = Bukkit.getPlayer(strings[7]);
                                    this.plugin.setDelay(delay);
                                    this.plugin.setFuse(fuse);
                                    Integer playerQueue = 0;
                                    if (this.plugin.getPlayerQueue().containsKey(target)) {
                                        playerQueue = this.plugin.getPlayerQueue().get(target).keySet().stream().collect(Collectors.toList()).get(0);
                                        this.plugin.getPlayerQueue().remove(target);
                                    }
                                    HashMap<Integer, String> hashMap = new HashMap<>();
                                    hashMap.put((playerQueue + tnt), null);
                                    hashMap.put((location.getBlockX()), null);
                                    hashMap.put((location.getBlockY()), null);
                                    hashMap.put((location.getBlockZ()), null);
                                    this.plugin.getPlayerQueue().put(target, hashMap);
                                    this.plugin.restart();
                                    if (messageEnabled()) {
                                        commandSender.sendMessage("§aSuccessfully delayed §e" + tnt + " §cTNT §awith the delay §e" + delay + " §aand the fuse §e" + fuse + " §afor the player §e" + target.getName() + " §aat the coordinates §7"+location.getBlockX()+" "+location.getBlockY()+" "+location.getBlockZ()+"!");
                                    }
                                }else if(strings.length == 7){
                                    Location location = getLocation(strings[0], strings[1], strings[2], false, null);
                                    this.plugin.setDelay(delay);
                                    this.plugin.setFuse(fuse);
                                    this.plugin.getLocationMap().put(location, tnt);
                                    this.plugin.restart();
                                    if(messageEnabled()) {
                                        commandSender.sendMessage("§aSuccessfully delayed §e" + tnt + " §cTNT §awith the delay §e" + delay + " §aand the fuse §e" + fuse + " §aat the coords §7"+"!");
                                    }
                                }else {
                                    commandSender.sendMessage("§cThe player §e"+strings[7]+" §cisn't online right now!");
                                }
                            }else{
                                commandSender.sendMessage("§cThe delay must be a (decimal)-number");
                            }
                        }else{
                            commandSender.sendMessage("§cThe Fuse-Time must be a (decimal)-number");
                        }
                    }else{
                        commandSender.sendMessage("§cThe Amount must be a number");
                    }
                }
            }else{
                commandSender.sendMessage("§cThe coordinates used in the command must be correct");
            }
        }else{
            commandSender.sendMessage(syntax);
        }
        return false;
    }

    public boolean validPlayer(String player){
        return (Bukkit.getPlayer(player) != null && Bukkit.getPlayer(player).isOnline());
    }

    private boolean isValid(String string){
        string = string.replace("+", "").replace("--", "-");
        return (isInteger(string) || isDouble(string));
    }

    private boolean messageEnabled(){
        if(this.plugin.getConfigHandler().keyExists("DisableMessage")){
            return !(this.plugin.getConfigHandler().getBoolean("DisableMessage"));
        }
        return true;
    }

    private Location getLocation(String s1, String s2, String s3, boolean fromPlayer, Player player){
        if(fromPlayer){
            Double d1 = 0.0;
            if(s1.startsWith("+") || !s1.startsWith("-")){
                d1 = player.getLocation().getX()+Double.parseDouble(s1.replace("+", ""));
            }else{
                d1 = player.getLocation().getX()-Double.parseDouble(s1.replace("-", ""));
            }
            Double d2 = 0.0;
            if(s2.startsWith("+") || !s2.startsWith("-")){
                d2 = player.getLocation().getY()+Double.parseDouble(s2.replace("+", ""));
            }else{
                d2 = player.getLocation().getY()-Double.parseDouble(s2.replace("-", ""));
            }
            Double d3 = 0.0;
            if(s3.startsWith("+") || !s3.startsWith("-")){
                d3 = player.getLocation().getZ()+Double.parseDouble(s3.replace("+", ""));
            }else{
                d3 = player.getLocation().getZ()-Double.parseDouble(s3.replace("-", ""));
            }
            return new Location(player.getWorld(), d1, d2, d3);
        }else{
           return new Location(Bukkit.getWorld("world"), Double.parseDouble(s1), Double.parseDouble(s2), Double.parseDouble(s3));
        }
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
