package de.timecoding.delayedtnt;

import de.timecoding.delayedtnt.command.DelayedCommand;
import de.timecoding.delayedtnt.command.completer.DelayedCommandCompleter;
import de.timecoding.delayedtnt.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;

public final class DelayedTNT extends JavaPlugin{

    private ConfigHandler configHandler;

    @Override
    public void onEnable() {
        this.configHandler = new ConfigHandler(this);
        Bukkit.getConsoleSender().sendMessage("§aLoading ConfigHandler...");
        this.configHandler.init();
        Bukkit.getConsoleSender().sendMessage("§eDelayedTNT §aby TimeCode was enabled!");
        PluginCommand command = this.getCommand("spawntnt");
        command.setExecutor(new DelayedCommand(this));
        command.setTabCompleter(new DelayedCommandCompleter(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§eDelayedTNT §cby TimeCode was disabled!");
    }

    private Integer queue = 0;
    private HashMap<OfflinePlayer, Integer> playerQueue = new HashMap<>();
    private BukkitTask bukkitTask;

    public Integer getQueue() {
        return queue;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public void setQueue(Integer queue) {
        this.queue = queue;
        if(!isStarted()){
            start();
        }
    }

    public HashMap<OfflinePlayer, Integer> getPlayerQueue() {
        return playerQueue;
    }

    public void setPlayerQueue(HashMap<OfflinePlayer, Integer> playerQueue) {
        this.playerQueue = playerQueue;
        if(!isStarted()){
            start();
        }
    }

    public boolean hasPermission(CommandSender commandSender){
        return (!this.configHandler.getBoolean("Permission.Enabled") || commandSender.hasPermission(this.configHandler.getString("Permission.Perm")));
    }

    public void start(){
        if(!isStarted() && this.queue > 0 || !isStarted() && this.playerQueue.size() > 0){
            Integer delay = this.getConfigHandler().getInteger("DelayInTicks");
            this.bukkitTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    playerQueue.keySet().forEach(offlinePlayer -> {
                        Integer amount = playerQueue.get(offlinePlayer);
                        playerQueue.remove(offlinePlayer);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getCommandLine(offlinePlayer.getPlayer()));
                        if(amount > 0){
                            playerQueue.put(offlinePlayer, (amount-1));
                        }
                    });
                    if(queue > 0) {
                        for(Player all : Bukkit.getOnlinePlayers()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getCommandLine(all));
                        }
                        setQueue((queue - 1));
                    }
                    if(queue <= 0 && playerQueue.isEmpty()){
                        stop();
                    }
                }
            }, delay ,delay);
        }
    }

    private String getCommandLine(Player player){
        return "execute at "+player.getName()+" run summon area_effect_cloud ~ ~1 ~ {Passengers:[{id:tnt,Fuse:"+getConfigHandler().getInteger("Fuze")+"}]}";
    }

    private void stop(){
        if(isStarted()){
            bukkitTask.cancel();
            bukkitTask = null;
        }
    }

    private boolean isStarted(){
        return (bukkitTask != null && !bukkitTask.isCancelled());
    }

}
