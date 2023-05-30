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

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if(this.plugin.hasPermission(commandSender)) {
            if (strings.length == 1) {
                if (isInteger(strings[0])) {
                    if (Integer.parseInt(strings[0]) >= 1) {
                        Integer integer = Integer.parseInt(strings[0]);
                        this.plugin.setQueue(this.plugin.getQueue() + integer);
                        commandSender.sendMessage("§aSuccessfully delayed §e" + integer + " §cTNT!");
                    } else {
                        commandSender.sendMessage("§cPlease use a number over zero");
                    }
                } else {
                    commandSender.sendMessage("§cPlease use a number as an argument!");
                }
            } else if (strings.length == 0) {
                this.plugin.setQueue(this.plugin.getQueue() + 1);
                commandSender.sendMessage("§aSuccessfully delayed §e1 §cTNT!");
            } else if (strings.length == 2) {
                if (isInteger(strings[0])) {
                    if (Integer.parseInt(strings[0]) >= 1) {
                        Player target = Bukkit.getPlayer(strings[1]);
                        if (target != null && target.isOnline()) {
                            Integer integer = Integer.parseInt(strings[0]);
                            Integer playerQueue = 0;
                            if (this.plugin.getPlayerQueue().containsKey(target)) {
                                playerQueue = this.plugin.getPlayerQueue().get(target);
                                this.plugin.getPlayerQueue().remove(target);
                            }
                            this.plugin.getPlayerQueue().put(target, (playerQueue + integer));
                            this.plugin.start();
                            commandSender.sendMessage("§aSuccessfully delayed §e" + integer + " §cTNT §afor the player §e" + target.getName() + "!");
                        } else {
                            commandSender.sendMessage("§cThis player isn't online or does not exist!");
                        }
                    } else {
                        commandSender.sendMessage("§cPlease use a number over zero");
                    }
                } else {
                    commandSender.sendMessage("§cPlease use a number as an argument!");
                }
            } else {
                commandSender.sendMessage("§cPlease use: /spawntnt (<Amount>) (<PlayerName>)");
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

}
