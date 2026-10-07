package dev.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class LifeStealPlugin extends JavaPlugin {
    private DataStore data; private ItemManager items; private Map<UUID,PlayerData> playerData=new HashMap<>(); private final Set<UUID> teleportSaved=new HashSet<>(); private List<String> worlds;
    @Override public void onEnable(){saveDefaultConfig();reloadConfig();data=new DataStore(this);items=new ItemManager(this);loadWorlds();getServer().getPluginManager().registerEvents(new LifeStealListener(this),this);LifeStealCommand c=new LifeStealCommand(this);getCommand("lifesteal").setExecutor(c);getCommand("lifesteal").setTabCompleter(c);getCommand("hearts").setExecutor(c);getCommand("withdrawheart").setExecutor(c);getCommand("revive").setExecutor(c);new RecipeManager(this).register();for(Player p:Bukkit.getOnlinePlayers())handleJoin(p);}
    @Override public void onDisable(){for(Player p:Bukkit.getOnlinePlayers())saveSide(p);}
    public void reloadPlugin(){reloadConfig();loadWorlds();}
    private void loadWorlds(){worlds=new ArrayList<>(getConfig().getStringList("worlds.enabled"));if(worlds.isEmpty())worlds.add("LifeSteal");}
    public boolean isLifeStealWorld(World w){return w!=null&&worlds.stream().anyMatch(n->n.equals(w.getName()));}
    public World mainWorld(){return Bukkit.getWorld(worlds.get(0));}
    public int startingHearts(){return getConfig().getInt("hearts.starting",10);}public int maxHearts(){return getConfig().getInt("hearts.max",20);}public Map<UUID,PlayerData> playerData(){return playerData;}public DataStore data(){return data;}public ItemManager items(){return items;}
    public PlayerData getData(Player p){return playerData.computeIfAbsent(p.getUniqueId(),u->data.loadLifeStealData(p));}
    public void saveSide(Player p){if(isLifeStealWorld(p.getWorld())){data.saveLifeStealState(p);data.saveLifeStealData(p,getData(p));}else data.saveOutsideState(p);}
    public void handleJoin(Player p){boolean ls=isLifeStealWorld(p.getWorld());World mw=mainWorld()!=null?mainWorld():p.getWorld();if(ls){PlayerData d=data.loadLifeStealData(p);playerData.put(p.getUniqueId(),d);StateData s=data.loadLifeStealState(p);data.applyState(p,s,s==null);data.setMarker(mw,p.getUniqueId(),"lifesteal");Bukkit.getScheduler().runTask(this,()->enforceElimination(p));}else{StateData s=data.loadOutsideState(p);data.applyState(p,s,s==null);data.setMarker(mw,p.getUniqueId(),"outside");}}
    public void handleTeleportSave(Player p,World from,World to){if(from!=null&&to!=null&&isLifeStealWorld(from)!=isLifeStealWorld(to)){if(isLifeStealWorld(from)){data.saveLifeStealState(p);data.saveLifeStealData(p,getData(p));}else data.saveOutsideState(p);teleportSaved.add(p.getUniqueId());}}
    public void handleChangedWorld(Player p,World from,World to){boolean fromLs=isLifeStealWorld(from),toLs=isLifeStealWorld(to);if(fromLs!=toLs){if(!teleportSaved.remove(p.getUniqueId())){if(fromLs){data.saveLifeStealState(p);data.saveLifeStealData(p,getData(p));}else data.saveOutsideState(p);}World mw=mainWorld()!=null?mainWorld():to;if(toLs){PlayerData d=data.loadLifeStealData(p);playerData.put(p.getUniqueId(),d);StateData s=data.loadLifeStealState(p);data.applyState(p,s,s==null);data.setMarker(mw,p.getUniqueId(),"lifesteal");Bukkit.getScheduler().runTask(this,()->{if(getConfig().getBoolean("state.teleport-to-saved-location-on-return",true)){StateData x=data.loadLifeStealState(p);if(x!=null&&x.location!=null&&x.location.getWorld()!=null)p.teleport(x.location);}enforceElimination(p);});}else{StateData s=data.loadOutsideState(p);data.applyState(p,s,s==null);data.setMarker(mw,p.getUniqueId(),"outside");if(getConfig().getBoolean("state.teleport-to-saved-location-outside",false)&&s!=null&&s.location!=null&&s.location.getWorld()!=null)Bukkit.getScheduler().runTask(this,()->p.teleport(s.location));}}}
    public void enforceElimination(Player p){if(!isLifeStealWorld(p.getWorld()))return;PlayerData d=getData(p);if(!d.eliminated)return;String mode=getConfig().getString("elimination.mode","spectator");if("world".equalsIgnoreCase(mode)){World w=Bukkit.getWorld(getConfig().getString("elimination.world",getConfig().getString("elimination.respawn-world","LifeSteal")));if(w!=null)p.teleport(w.getSpawnLocation());p.setGameMode(GameMode.SURVIVAL);}else p.setGameMode(GameMode.SPECTATOR);}
    public boolean reviveTarget(World world,UUID id,int hearts){Player online=Bukkit.getPlayer(id);PlayerData d=data.loadLifeStealData(id,world);d.hearts=Math.max(1,Math.min(maxHearts(),hearts));d.eliminated=false;data.saveLifeStealData(id,online!=null?online.getName():data.playerName(world,id),world,d);if(online!=null){playerData.put(id,d);if(online.getWorld()!=world){saveSide(online);Bukkit.getScheduler().runTask(this,()->online.teleport(world.getSpawnLocation()));}else{setMaxHealth(online,d.hearts);online.setGameMode(GameMode.SURVIVAL);online.setHealth(Math.min(online.getHealth(),d.hearts*2.0));}online.sendMessage(msg("you-revived").replace("%hearts%",String.valueOf(d.hearts)));}return true;}
    public void setHearts(Player p,int hearts){PlayerData d=getData(p);d.hearts=Math.max(0,Math.min(maxHearts(),hearts));d.eliminated=d.hearts<=0;setMaxHealth(p,d.hearts);if(d.eliminated)enforceElimination(p);}
    public void addHearts(Player p,int amount){setHearts(p,getData(p).hearts+amount);}public void removeHearts(Player p,int amount){setHearts(p,getData(p).hearts-amount);}
    public void setMaxHealth(Player p,int hearts){double hp=Math.max(1,hearts*2.0);p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);if(p.getHealth()>hp)p.setHealth(hp);}
    public String msg(String key){return color(getConfig().getString("messages.prefix","")+getConfig().getString("messages."+key,key));}
    public void send(Player p,String key){p.sendMessage(msg(key));}public void send(Player p,String key,Map<String,String> vars){String s=msg(key);for(var e:vars.entrySet())s=s.replace("%"+e.getKey()+"%",e.getValue());p.sendMessage(s);}
    public String color(String s){return s==null?"":s.replace('&','§');}
}