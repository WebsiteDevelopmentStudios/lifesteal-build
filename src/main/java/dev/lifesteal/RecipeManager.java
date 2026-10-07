package dev.lifesteal;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
import java.util.List;

public final class RecipeManager {
    private final LifeStealPlugin plugin;
    public RecipeManager(LifeStealPlugin plugin){this.plugin=plugin;}
    public void register(){if(plugin.getConfig().getBoolean("recipes.heart.enabled",true))registerHeart();if(plugin.getConfig().getBoolean("recipes.revive-crystal.enabled",true))registerRevive();}
    private void registerHeart(){ItemStack result=plugin.items().heart(1);ShapedRecipe r=new ShapedRecipe(new NamespacedKey(plugin,"heart"),result);List<String> shape=plugin.getConfig().getStringList("recipes.heart.shape");r.shape(shape.toArray(new String[0]));addMaterials(r,"recipes.heart.ingredients");plugin.getServer().addRecipe(r);}
    private void registerRevive(){ItemStack result=plugin.items().revive(1);ShapedRecipe r=new ShapedRecipe(new NamespacedKey(plugin,"revive_crystal"),result);List<String> shape=plugin.getConfig().getStringList("recipes.revive-crystal.shape");r.shape(shape.toArray(new String[0]));addMaterials(r,"recipes.revive-crystal.ingredients");plugin.getServer().addRecipe(r);}
    private void addMaterials(ShapedRecipe r,String path){var c=plugin.getConfig().getConfigurationSection(path);if(c==null)return;for(String k:c.getKeys(false)){String v=c.getString(k);if(v==null)continue;if("HEART_ITEM".equalsIgnoreCase(v))r.setIngredient(k.charAt(0),new RecipeChoice.ExactChoice(plugin.items().heart(1)));else{Material m=Material.matchMaterial(v);if(m!=null)r.setIngredient(k.charAt(0),m);}}}
}