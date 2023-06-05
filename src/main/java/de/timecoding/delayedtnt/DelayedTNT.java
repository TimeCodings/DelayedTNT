package de.timecoding.delayedtnt;

import de.timecoding.delayedtnt.command.DelayedCommand;
import de.timecoding.delayedtnt.command.completer.DelayedCommandCompleter;
import de.timecoding.delayedtnt.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;

public final class DelayedTNT extends JavaPlugin{

    private ConfigHandler configHandler;

    private Integer fuse = 0;
    private Integer delay = 0;

    @Override
    public void onEnable() {
        this.configHandler = new ConfigHandler(this);
        Bukkit.getConsoleSender().sendMessage("§aLoading ConfigHandler...");
        this.configHandler.init();
        Bukkit.getConsoleSender().sendMessage("§eDelayedTNT §aby TimeCode was enabled!");
        PluginCommand command = this.getCommand("spawntnt");
        command.setExecutor(new DelayedCommand(this));
        command.setTabCompleter(new DelayedCommandCompleter(this));
        this.fuse = this.getConfigHandler().getInteger("Fuse");
        this.delay = this.getConfigHandler().getInteger("DelayInTicks");
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

    public void addQueue(Integer queue) {
        this.queue = (this.queue+queue);
        restart();
    }

    public HashMap<OfflinePlayer, Integer> getPlayerQueue() {
        return playerQueue;
    }

    public void setPlayerQueue(HashMap<OfflinePlayer, Integer> playerQueue) {
        this.playerQueue = playerQueue;
        restart();
    }

    public boolean hasPermission(CommandSender commandSender){
        return (!this.configHandler.getBoolean("Permission.Enabled") || commandSender.hasPermission(this.configHandler.getString("Permission.Perm")));
    }

    public void start(){
        if(!isStarted() && this.queue > 0 || !isStarted() && this.playerQueue.size() > 0){
            this.bukkitTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    playerQueue.keySet().forEach(offlinePlayer -> {
                        if(offlinePlayer.isOnline()) {
                            Integer amount = playerQueue.get(offlinePlayer);
                            playerQueue.remove(offlinePlayer);
                            spawnTNT(offlinePlayer, fuse);
                            if (amount > 0) {
                                playerQueue.put(offlinePlayer, (amount - 1));
                            }
                        }
                    });
                    if(queue > 0) {
                        for(Player all : Bukkit.getOnlinePlayers()) {
                            spawnTNT(all, (fuse*20));
                        }
                        queue = ((queue - 1));
                    }
                    if(queue <= 0 && playerQueue.isEmpty()){
                        stop();
                    }
                }
            }, 0 ,(delay*20));
        }
    }

    public void spawnTNT(OfflinePlayer offlinePlayer, Integer fuze){
        Entity entity = offlinePlayer.getPlayer().getWorld().spawnEntity(offlinePlayer.getPlayer().getLocation(), EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloud)entity).setDuration(0);
        TNTPrimed tntPrimed = ((TNTPrimed)offlinePlayer.getPlayer().getWorld().spawnEntity(offlinePlayer.getPlayer().getLocation().subtract(0, 1, 0), EntityType.PRIMED_TNT));
        tntPrimed.setFuseTicks(fuze);
        entity.addPassenger(((Entity) tntPrimed));
    }

    public void setFuse(Integer fuse) {
        this.fuse = fuse;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    private void stop(){
        if(isStarted()){
            bukkitTask.cancel();
            bukkitTask = null;
        }
    }

    public void restart(){
        if(isStarted()){
            stop();
            start();
        }else{
            start();
        }
    }

    private boolean isStarted(){
        return (bukkitTask != null && !bukkitTask.isCancelled());
    }

}
