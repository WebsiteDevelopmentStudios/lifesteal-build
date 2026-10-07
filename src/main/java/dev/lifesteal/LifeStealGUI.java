package dev.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;

public final class LifeStealGUI implements InventoryHolder {
    private final LifeStealPlugin plugin; private final Map<Integer,UUID> targets=new HashMap<>(); private Inventory inventory;
    private LifeStealGUI(LifeStealPlugin plugin){this.plugin=plugin;}
    public static void open(LifeStealPlugin plugin,Player p){List<UUID> ids=plugin.data().eliminatedPlayers(p.getWorld());if(ids.isEmpty()){plugin.send(p,"no-eliminated");return;}LifeStealGUI h=new LifeStealGUI(plugin);int size=Math.min(54,Math.max(9,((ids.size()+8)/9)*9));h.inventory=Bukkit.createInventory(h,size,plugin.color("&5Revive Crystal"));for(int i=0;i<ids.size()&&i<size;i++){UUID id=ids.get(i);ItemStack head=new ItemStack(Material.PLAYER_HEAD);SkullMeta sm=(SkullMeta)head.getItemMeta();String name=plugin.data().playerName(p.getWorld(),id);sm.setDisplayName(plugin.color("&d"+name));sm.setOwningPlayer(Bukkit.getOfflinePlayer(id));head.setItemMeta(sm);h.targets.put(i,id);h.inventory.setItem(i,head);}p.openInventory(h.inventory);}
    @Override public Inventory getInventory(){return inventory;}
    public UUID target(int slot){return targets.get(slot);}
}