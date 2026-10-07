package dev.lifesteal;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import java.util.List;

public final class StateData {
    public ItemStack[] contents;
    public ItemStack[] armor;
    public ItemStack offhand;
    public ItemStack[] enderChest;
    public int level, totalExperience;
    public float exp;
    public double health, maxHealth;
    public int food, fireTicks, air;
    public float saturation, exhaustion;
    public List<PotionEffect> effects;
    public GameMode gameMode;
    public Location location;
    public StateData() {}
}