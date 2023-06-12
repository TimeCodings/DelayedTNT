package de.timecoding.delayedtnt.command;

import de.timecoding.delayedtnt.DelayedTNT;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelayedCommand implements CommandExecutor {

    private DelayedTNT plugin;

    public DelayedCommand(DelayedTNT plugin){
        this.plugin = plugin;
    }

    private String help = "§fCommands: \n §e/tnt <Amount> - Spawns multiple TNT with the delay set in the config.yml \n /tnt <Amount> <Player> - Spawns multiple TNT for a specific player \n /tnt <Amount> <Delay> <Fuse> - Spawns multiple TNT with a custom delay and fuse \n /tnt <Amount> <Delay> <Fuse> <Player> - Spawns multiple TNT with a custom delay and fuse for a specific player";

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if(this.plugin.hasPermission(commandSender)) {
            if (strings.length == 1) {
                if(!strings[0].equalsIgnoreCase("help") && !strings[0].equalsIgnoreCase("reload")) {
                    if (isInteger(strings[0])) {
                        if (Integer.parseInt(strings[0]) >= 1) {
                            Integer integer = Integer.parseInt(strings[0]);
                            this.plugin.addQueue(this.plugin.getQueue() + integer);
                            commandSender.sendMessage("§aSuccessfully delayed §e" + integer + " §cTNT!");
                            this.plugin.restart();
                        } else {
                            commandSender.sendMessage("§cPlease use a number over zero");
                        }
                    } else {
                        commandSender.sendMessage("§cPlease use a number as an argument!");
                    }
                }else if(strings[0].equalsIgnoreCase("help")){
                    commandSender.sendMessage(help);
                }else if(strings[0].equalsIgnoreCase("reload")){
                    this.plugin.getConfigHandler().reload();
                    commandSender.sendMessage("§aSuccessfully reloaded the §econfig.yml");
                }else{
                    commandSender.sendMessage("§c/tnt help");
                }
            } else if (strings.length == 0) {
                this.plugin.addQueue(this.plugin.getQueue() + 1);
                this.plugin.restart();
                commandSender.sendMessage("§aSuccessfully delayed §e1 §cTNT!");
                commandSender.sendMessage("§eIf you wanted to open the help try §f/tnt help");
            } else if (strings.length == 2) {
                if (isInteger(strings[0])) {
                    if (Integer.parseInt(strings[0]) >= 1) {
                        Player target = Bukkit.getPlayer(strings[1]);
                        if (target != null && target.isOnline()) {
                            this.plugin.stop();
                            Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
                                @Override
                                public void run() {
                                    Integer integer = Integer.parseInt(strings[0]);
                                    Integer playerQueue = 0;
                                    if (plugin.getPlayerQueue().containsKey(target)) {
                                        playerQueue = plugin.getPlayerQueue().get(target);
                                        plugin.getPlayerQueue().remove(target);
                                    }
                                    plugin.getPlayerQueue().put(target, (playerQueue + integer));
                                    plugin.restart();
                                    commandSender.sendMessage("§aSuccessfully delayed §e" + integer + " §cTNT §afor the player §e" + target.getName() + "!");
                                }
                            }, 20);
                        } else {
                            commandSender.sendMessage("§cThis player isn't online or does not exist!");
                        }
                    } else {
                        commandSender.sendMessage("§cPlease use a number over zero");
                    }
                } else {
                    commandSender.sendMessage("§cPlease use a number as an argument!");
                }
            } else if(strings.length == 3) {
                if(isInteger(strings[0]) && Integer.parseInt(strings[0]) >= 1){
                    if(isDouble(strings[1]) && (int)Double.parseDouble(strings[1]) >= 0){
                        if(isInteger(strings[2]) && Integer.parseInt(strings[2]) >= 0){
                            Integer tnt = Integer.parseInt(strings[0]);
                            Double delay = Double.parseDouble(strings[1]);
                            Integer fuse = Integer.parseInt(strings[2]);
                            this.plugin.setDelay(delay);
                            this.plugin.setFuse(fuse);
                            this.plugin.addQueue(tnt);
                            this.plugin.restart();
                            commandSender.sendMessage("§aSuccessfully delayed §e" + tnt + " §cTNT §awith the delay §e"+delay+" §aand the fuse §e"+fuse+"!");
                        }else{
                            commandSender.sendMessage("§cThe fuse-time must be a number over -1");
                        }
                    }else{
                        commandSender.sendMessage("§cThe delay in seconds must be a number over -1");
                    }
                }else{
                    commandSender.sendMessage("§cThe TNT-Amount must be a number over 0");
                }
            }else if(strings.length == 4) {
                if(isInteger(strings[0]) && Integer.parseInt(strings[0]) >= 1){
                    if(isDouble(strings[1]) && (int)Double.parseDouble(strings[1]) >= 0){
                        if(isInteger(strings[2]) && Integer.parseInt(strings[2]) >= 0){
                            Player target = Bukkit.getPlayer(strings[3]);
                            if (target != null && target.isOnline()) {
                                Integer tnt = Integer.parseInt(strings[0]);
                                Double delay = Double.parseDouble(strings[1]);
                                Integer fuse = Integer.parseInt(strings[2]);
                                this.plugin.setDelay(delay);
                                this.plugin.setFuse(fuse);
                                Integer playerQueue = 0;
                                if (this.plugin.getPlayerQueue().containsKey(target)) {
                                    playerQueue = this.plugin.getPlayerQueue().get(target);
                                    this.plugin.getPlayerQueue().remove(target);
                                }
                                this.plugin.getPlayerQueue().put(target, (playerQueue + tnt));
                                this.plugin.restart();
                                commandSender.sendMessage("§aSuccessfully delayed §e" + tnt + " §cTNT §awith the delay §e" + delay + " §aand the fuse §e" + fuse + " §afor the player §e"+target.getName()+"!");
                            } else {
                                commandSender.sendMessage("§cThis player isn't online or does not exist!");
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
        }else{
            commandSender.sendMessage("§cYou do not have the permission to use that command!");
        }
        return false;
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
