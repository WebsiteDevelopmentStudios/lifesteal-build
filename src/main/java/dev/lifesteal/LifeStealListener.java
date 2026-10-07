package dev.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public final class LifeStealListener implements Listener {
    private final LifeStealPlugin plugin;
    public LifeStealListener(LifeStealPlugin plugin){this.plugin=plugin;}
    @EventHandler public void join(PlayerJoinEvent e){plugin.handleJoin(e.getPlayer());}
    @EventHandler public void quit(PlayerQuitEvent e){plugin.saveSide(e.getPlayer());}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void teleport(PlayerTeleportEvent e){if(e.getFrom().getWorld()!=e.getTo().getWorld())plugin.handleTeleportSave(e.getPlayer(),e.getFrom().getWorld(),e.getTo().getWorld());}
    @EventHandler public void changed(PlayerChangedWorldEvent e){plugin.handleChangedWorld(e.getPlayer(),e.getFrom(),e.getPlayer().getWorld());}
    @EventHandler public void interact(PlayerInteractEvent e){Player p=e.getPlayer();if(!plugin.isLifeStealWorld(p.getWorld()))return;if(e.getAction()!=Action.RIGHT_CLICK_AIR&&e.getAction()!=Action.RIGHT_CLICK_BLOCK)return;ItemStack item=p.getInventory().getItemInMainHand();if(plugin.items().isHeart(item)){e.setCancelled(true);PlayerData d=plugin.getData(p);if(d.eliminated){plugin.send(p,"eliminated");return;}if(d.hearts>=plugin.maxHearts()){plugin.send(p,"max-hearts");return;}d.hearts++;plugin.setMaxHealth(p,d.hearts);item.setAmount(item.getAmount()-1);plugin.send(p,"heart-gained",Map.of("hearts",String.valueOf(d.hearts)));plugin.data().saveLifeStealData(p,d);}else if(plugin.items().isRevive(item)){e.setCancelled(true);LifeStealGUI.open(plugin,p);}}
    @EventHandler public void click(InventoryClickEvent e){if(e.getWhoClicked() instanceof Player p&&e.getView().getTopInventory().getHolder() instanceof LifeStealGUI gui){if(!plugin.isLifeStealWorld(p.getWorld())){p.closeInventory();return;}e.setCancelled(true);if(e.getClickedInventory()!=e.getView().getTopInventory())return;UUID id=gui.target(e.getRawSlot());if(id==null)return;p.closeInventory();PlayerData d=plugin.data().loadLifeStealData(id,p.getWorld());if(!d.eliminated)return;int h=plugin.getConfig().getInt("items.revive-hearts",5);plugin.reviveTarget(p.getWorld(),id,h);ItemStack crystal=p.getInventory().getItemInMainHand();if(plugin.items().isRevive(crystal)){crystal.setAmount(crystal.getAmount()-1);plugin.send(p,"crystal-used");}else{ItemStack off=p.getInventory().getItemInOffHand();if(plugin.items().isRevive(off)){off.setAmount(off.getAmount()-1);plugin.send(p,"crystal-used");}}}}
    @EventHandler public void drag(InventoryDragEvent e){if(e.getView().getTopInventory().getHolder() instanceof LifeStealGUI)e.setCancelled(true);}
    @EventHandler public void craft(PrepareItemCraftEvent e){if(!(e.getView().getPlayer() instanceof Player p))return;boolean custom=false;for(ItemStack i:e.getInventory().getMatrix())if(plugin.items().isLifeStealItem(i)){custom=true;break;}ItemStack r=e.getInventory().getResult();if(custom||(plugin.items().isLifeStealItem(r)&&!plugin.isLifeStealWorld(p.getWorld())))e.getInventory().setResult(null);}
    @EventHandler public void death(PlayerDeathEvent e){Player victim=e.getPlayer();if(!plugin.isLifeStealWorld(victim.getWorld()))return;PlayerData vd=plugin.getData(victim);Player killer=victim.getKiller();int cost;if(killer!=null&&plugin.isLifeStealWorld(killer.getWorld())){cost=Math.max(0,plugin.getConfig().getInt("hearts.steal-on-kill",1));if(cost>0){vd.hearts=Math.max(0,vd.hearts-cost);if(vd.hearts==0)vd.eliminated=true;PlayerData kd=plugin.getData(killer);if(!kd.eliminated){if(kd.hearts>=plugin.maxHearts()){e.getDrops().add(plugin.items().heart(cost));plugin.send(killer,"killer-max");}else{int gain=Math.min(cost,plugin.maxHearts()-kd.hearts);kd.hearts+=gain;plugin.setMaxHealth(killer,kd.hearts);if(gain<cost)e.getDrops().add(plugin.items().heart(cost-gain));plugin.send(killer,"killed",Map.of("victim",victim.getName()));}kd.kills++;plugin.data().saveLifeStealData(killer,kd);}}}else{cost=Math.max(0,plugin.getConfig().getInt("hearts.natural-death-cost",1));if(cost>0){vd.hearts=Math.max(0,vd.hearts-cost);if(vd.hearts==0)vd.eliminated=true;plugin.send(victim,"lost-heart",Map.of("hearts",String.valueOf(vd.hearts)));}}plugin.data().saveLifeStealData(victim,vd);if(vd.eliminated)plugin.send(victim,"eliminated");}
    @EventHandler public void respawn(PlayerRespawnEvent e){Player p=e.getPlayer();if(!plugin.isLifeStealWorld(p.getWorld()))return;PlayerData d=plugin.getData(p);if(!d.eliminated)return;String mode=plugin.getConfig().getString("elimination.mode","spectator");if("world".equalsIgnoreCase(mode)){World w=Bukkit.getWorld(plugin.getConfig().getString("elimination.world",plugin.getConfig().getString("elimination.respawn-world","LifeSteal")));if(w!=null)e.setRespawnLocation(w.getSpawnLocation());}else{World w=plugin.mainWorld();if(w!=null)e.setRespawnLocation(w.getSpawnLocation());}Bukkit.getScheduler().runTask(plugin,()->plugin.enforceElimination(p));}
    @EventHandler public void pickup(org.bukkit.event.entity.EntityPickupItemEvent e){if(e.getEntity() instanceof Player p&&!plugin.isLifeStealWorld(p.getWorld())&&plugin.items().isLifeStealItem(e.getItem().getItemStack()))e.setCancelled(true);}
}