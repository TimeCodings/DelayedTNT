package de.timecoding.delayedtnt.command.completer;

import de.timecoding.delayedtnt.DelayedTNT;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class DelayedCommandCompleter implements TabCompleter {

    private DelayedTNT plugin;

    public DelayedCommandCompleter(DelayedTNT plugin){
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> list = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("spawntnt") && this.plugin.hasPermission(commandSender)){
            if(strings.length == 1){
                list.add("1");
                list.add("10");
                list.add("100");
                list.add("1000");
                list.add("help");
            }else if(strings.length == 2 || strings.length == 4) {
                Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
            }
            if(strings.length == 2 || strings.length == 3){
                list.add("10");
                list.add("20");
                list.add("30");
                list.add("40");
                list.add("50");
            }
            }
        return list;
    }
}
