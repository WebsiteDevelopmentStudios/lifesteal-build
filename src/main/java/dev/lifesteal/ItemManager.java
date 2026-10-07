package dev.lifesteal;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;

public final class ItemManager {
    public static final String HEART = "heart";
    public static final String REVIVE = "revive";
    private final LifeStealPlugin plugin;
    private final NamespacedKey typeKey;
    public ItemManager(LifeStealPlugin plugin) { this.plugin=plugin; typeKey=new NamespacedKey(plugin,"lifesteal_item"); }
    public ItemStack heart(int amount) { ItemStack i=make(HEART,"items.heart"); i.setAmount(Math.max(1,Math.min(64,amount))); return i; }
    public ItemStack revive(int amount) { ItemStack i=make(REVIVE,"items.revive-crystal"); i.setAmount(Math.max(1,Math.min(64,amount))); return i; }
    private ItemStack make(String type,String path) {
        ConfigurationSection c=plugin.getConfig().getConfigurationSection(path);
        Material m=Material.matchMaterial(c==null?"NETHER_STAR":c.getString("material","NETHER_STAR")); if(m==null)m=Material.NETHER_STAR;
        ItemStack i=new ItemStack(m); ItemMeta meta=i.getItemMeta();
        meta.setDisplayName(color(c==null?type:c.getString("name",type)));
        List<String> lore=c==null?List.of():c.getStringList("lore"); List<String> out=new ArrayList<>(); for(String s:lore)out.add(color(s)); meta.setLore(out);
        int cmd=c==null?0:c.getInt("custom-model-data",0); if(cmd>0)meta.setCustomModelData(cmd);
        meta.getPersistentDataContainer().set(typeKey,PersistentDataType.STRING,type); i.setItemMeta(meta); return i;
    }
    public boolean is(ItemStack item,String type){if(item==null||item.getType().isAir()||!item.hasItemMeta())return false;return type.equals(item.getItemMeta().getPersistentDataContainer().get(typeKey,PersistentDataType.STRING));}
    public boolean isHeart(ItemStack i){return is(i,HEART);} public boolean isRevive(ItemStack i){return is(i,REVIVE);} public boolean isLifeStealItem(ItemStack i){return isHeart(i)||isRevive(i);}
    private String color(String s){return s==null?"":s.replace('&','§');}
}