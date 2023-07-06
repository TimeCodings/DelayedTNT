package de.timecoding.delayedtnt;

import de.timecoding.delayedtnt.command.DelayedCommand;
import de.timecoding.delayedtnt.command.completer.DelayedCommandCompleter;
import de.timecoding.delayedtnt.config.ConfigHandler;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public final class DelayedTNT extends JavaPlugin implements Listener {

    private ConfigHandler configHandler;

    private Double fuse = 0.0;
    private Double delay = 0.0;

    @Override
    public void onEnable() {
        this.configHandler = new ConfigHandler(this);
        Bukkit.getConsoleSender().sendMessage("§aLoading ConfigHandler...");
        this.configHandler.init();
        Bukkit.getConsoleSender().sendMessage("§eDelayedTNT §aby TimeCode was enabled!");
        PluginCommand command = this.getCommand("spawntnt");
        command.setExecutor(new DelayedCommand(this));
        command.setTabCompleter(new DelayedCommandCompleter(this));
        this.getServer().getPluginManager().registerEvents(this, this);
        this.fuse = this.getConfigHandler().getConfig().getDouble("Fuse");
        this.delay = this.getConfigHandler().getConfig().getDouble("DelayInSeconds");
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
                    for (Iterator<OfflinePlayer> iterator = playerQueue.keySet().iterator(); iterator.hasNext(); ) {
                        OfflinePlayer offlinePlayer = iterator.next();
                        if(offlinePlayer.isOnline()) {
                            Integer amount = playerQueue.get(offlinePlayer);
                            playerQueue.remove(offlinePlayer);
                            spawnTNT(offlinePlayer, fuse);
                            if (amount > 1) {
                                playerQueue.put(offlinePlayer, (amount - 1));
                            }
                        }else{
                            playerQueue.remove(offlinePlayer);
                        }
                    }
                    if(queue > 0) {
                        for(Player all : Bukkit.getOnlinePlayers()) {
                            spawnTNT(all, fuse);
                        }
                        queue = ((queue - 1));
                    }
                    if(queue <= 0 && playerQueue.isEmpty()){
                        stop();
                    }
                }
            }, 0 ,getDelay(this.delay));
        }
    }

    private Integer getDelay(Double doubleDelay){
        String[] split = doubleDelay.toString().split("\\.");
        if(!split[1].toString().startsWith("0")){
            return (Integer.parseInt(split[0])*20)+(int)(Double.valueOf(String.valueOf("0."+(split[1])))*20);
        }else{
            return (Integer.parseInt(split[0])*20);
        }
    }

    private Integer getFuse(Double fuse){
       return this.getDelay(fuse);
    }

    public void spawnTNT(OfflinePlayer offlinePlayer, Double fuse){
        Integer subtract = this.getConfigHandler().getInteger("Position.Subtract");
        Integer add = this.getConfigHandler().getInteger("Position.Add");
        Entity entity = offlinePlayer.getPlayer().getWorld().spawnEntity(offlinePlayer.getPlayer().getLocation().subtract(0, subtract, 0).add(0, add, 0), EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloud)entity).setDuration(0);
        TNTPrimed tntPrimed = ((TNTPrimed)offlinePlayer.getPlayer().getWorld().spawnEntity(offlinePlayer.getPlayer().getLocation().subtract(0, subtract, 0).add(0, add, 0), EntityType.PRIMED_TNT));
        tntPrimed.setFuseTicks(this.getFuse(this.fuse));
        entity.addPassenger(((Entity) tntPrimed));
        if(this.configHandler.getBoolean("Firework.Enabled") && !this.configHandler.getBoolean("Firework.OnExplode")) {
            detonateFirework(tntPrimed);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        if(this.configHandler.getBoolean("Firework.Enabled") && this.configHandler.getBoolean("Firework.OnExplode") && event.getEntity().getType() == EntityType.PRIMED_TNT) {
            detonateFirework((TNTPrimed) event.getEntity());
        }
    }

    public void detonateFirework(TNTPrimed tntPrimed){
        Location location = tntPrimed.getLocation();
        Firework firework = (Firework) location.getWorld().spawnEntity(getCenterLocation(tntPrimed.getLocation()), EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.setPower(this.configHandler.getInteger("Firework.Power"));
        this.getFireworkEffects().forEach(fireworkEffect -> {
            fireworkMeta.addEffect(fireworkEffect);
        });
        firework.setFireworkMeta(fireworkMeta);
        firework.detonate();
    }

    private Location getCenterLocation(Location location) {
        return location.add(0.5, 1, 0.5);
    }

    private List<FireworkEffect> getFireworkEffects(){
        List<Color> list = new ArrayList<>();
        List<String> stringList = this.configHandler.getStringList("Firework.Colors");
        List<FireworkEffect> fireworkEffects = new ArrayList<>();
        if(stringList != null){
            stringList.forEach(s -> {
                fireworkEffects.add(FireworkEffect.builder().withColor(getColor(s)).build());
            });
        }
        return fireworkEffects;
    }

    private Color getColor(String s){
        List<Color> list = new ArrayList<>();
        switch (s){
            case "RED":
                list.add(Color.RED);
                break;
            case "AQUA":
                list.add(Color.AQUA);
                break;
            case "BLUE":
                list.add(Color.BLUE);
                break;
            case "LIME":
                list.add(Color.LIME);
                break;
            case "OLIVE":
                list.add(Color.OLIVE);
                break;
            case "ORANGE":
                list.add(Color.ORANGE);
                break;
            case "PURPLE":
                list.add(Color.PURPLE);
                break;
            case "WHITE":
                list.add(Color.WHITE);
                break;
            case "BLACK":
                list.add(Color.BLACK);
                break;
            case "FUCHSIA":
                list.add(Color.FUCHSIA);
                break;
            case "GREY":
                list.add(Color.GRAY);
                break;
            case "GREEN":
                list.add(Color.GREEN);
                break;
            case "MAROON":
                list.add(Color.MAROON);
                break;
            case "NAVY":
                list.add(Color.NAVY);
                break;
            case "SILVER":
                list.add(Color.SILVER);
                break;
            case "YELLOW":
                list.add(Color.YELLOW);
                break;
            case "TEAL":
                list.add(Color.TEAL);
                break;
        }
        if(list.size() == 0) {
            return Color.GRAY;
        }else{
            return list.get(0);
        }
    }

    public void setFuse(Double fuse) {
        this.fuse = fuse;
    }

    public void setDelay(Double delay) {
        this.delay = delay;
    }

    public void stop(){
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
