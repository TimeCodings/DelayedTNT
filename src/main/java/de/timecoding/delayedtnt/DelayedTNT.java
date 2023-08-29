package de.timecoding.delayedtnt;

import de.timecoding.delayedtnt.command.DelayedCommand;
import de.timecoding.delayedtnt.command.TNTRainCommand;
import de.timecoding.delayedtnt.command.completer.DelayedCommandCompleter;
import de.timecoding.delayedtnt.config.ConfigHandler;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        //PluginCommand subCommand = this.getCommand("tntatcoords");
        //subCommand.setExecutor(new DelayedAtCoordsCommand(this));
        PluginCommand tntRain = this.getCommand("tntrain");
        tntRain.setExecutor(new TNTRainCommand(this));
        tntRain.setTabCompleter(new DelayedCommandCompleter(this));
        this.getServer().getPluginManager().registerEvents(this, this);
        this.fuse = this.getConfigHandler().getConfig().getDouble("Fuse");
        this.delay = this.getConfigHandler().getConfig().getDouble("DelayInSeconds");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§eDelayedTNT §cby TimeCode was disabled!");
    }

    private List<String> queue = new ArrayList<>();
    private HashMap<OfflinePlayer, HashMap<Integer, String>> playerQueue = new HashMap<>();
    private HashMap<Location, Integer> locationMap = new HashMap<>();
    private BukkitTask bukkitTask;

    public Integer getQueue() {
        return queue.size();
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public void addQueue(Integer queue, String text) {
        while (queue > 0){
            queue--;
            this.queue.add(text);
        }
        restart();
    }

    public HashMap<OfflinePlayer, HashMap<Integer, String>> getPlayerQueue() {
        return playerQueue;
    }

    public void setPlayerQueue(HashMap<OfflinePlayer, HashMap<Integer, String>> playerQueue) {
        this.playerQueue = playerQueue;
        restart();
    }

    public boolean hasPermission(CommandSender commandSender){
        return (!this.configHandler.getBoolean("Permission.Enabled") || commandSender.hasPermission(this.configHandler.getString("Permission.Perm")));
    }

    public void start(){
        if(!isStarted() && this.queue.size() > 0 || !isStarted() && this.playerQueue.size() > 0 || !isStarted() && this.getLocationMap().size() > 0){
            this.bukkitTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    for (Iterator<OfflinePlayer> iterator = playerQueue.keySet().iterator(); iterator.hasNext(); ) {
                        OfflinePlayer offlinePlayer = iterator.next();
                        if(offlinePlayer.isOnline()) {
                            HashMap<Integer, String> map = playerQueue.get(offlinePlayer);
                            Integer amount = playerQueue.get(offlinePlayer).keySet().stream().collect(Collectors.toList()).get(0);
                            String text = playerQueue.get(offlinePlayer).get(0);
                            playerQueue.remove(offlinePlayer);
                            if(map.size() <= 1) {
                                spawnTNT(offlinePlayer.getPlayer().getLocation(), fuse, text);
                            }else{
                                List<Integer> list = map.keySet().stream().collect(Collectors.toList());
                                spawnTNT(new Location(offlinePlayer.getPlayer().getWorld(), list.get(1), list.get(2), list.get(3)), fuse, text);
                            }
                            if (amount > 1) {
                                HashMap<Integer, String> hashMap = new HashMap<>();
                                hashMap.put((amount-1), text);
                                if(map.size() > 1){
                                    map.remove(amount);
                                    map.forEach((integer, s) -> hashMap.put(integer, s));
;                                }
                                playerQueue.put(offlinePlayer, hashMap);
                            }
                        }else{
                            playerQueue.remove(offlinePlayer);
                        }
                    }
                    if(locationMap.size() > 0){
                        AtomicBoolean executed = new AtomicBoolean(false);
                        try {
                            locationMap.forEach((location, integer) -> {
                                if (!executed.get()) {
                                    executed.set(true);
                                    locationMap.remove(location, integer);
                                    spawnTNT(location, fuse, null);
                                    integer--;
                                    if (integer > 0) {
                                        locationMap.put(location, integer);
                                    }
                                }
                            });
                        }catch (ConcurrentModificationException e){}
                    }
                    if(queue.size() > 0) {
                        String value = queue.get(0);
                        for(Player all : Bukkit.getOnlinePlayers()) {
                            spawnTNT(all.getLocation(), fuse, value);
                        }
                        queue.remove(0);
                    }
                    if(queue.size() == 0 && playerQueue.isEmpty() && locationMap.isEmpty()){
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

    public void spawnTNT(Location location, Double fuse, String text){
        Integer subtract = this.getConfigHandler().getInteger("Position.Subtract");
        Integer add = this.getConfigHandler().getInteger("Position.Add");
        Entity entity = location.getWorld().spawnEntity(location.getBlock().getLocation().subtract(0, subtract, 0).add(0, add, 0), EntityType.AREA_EFFECT_CLOUD);
        ((AreaEffectCloud)entity).setDuration(0);
        TNTPrimed tntPrimed = ((TNTPrimed)location.getWorld().spawnEntity(location.getBlock().getLocation().subtract(0, subtract, 0).add(0, add, 0), EntityType.PRIMED_TNT));
        if(text != null && !text.equalsIgnoreCase("")){
            tntPrimed.setCustomNameVisible(true);
            tntPrimed.setCustomName(text);
        }
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

    public HashMap<Location, Integer> getLocationMap() {
        return locationMap;
    }

    private boolean isStarted(){
        return (bukkitTask != null && !bukkitTask.isCancelled());
    }

}
