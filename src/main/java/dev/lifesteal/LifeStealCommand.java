package dev.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

public final class LifeStealCommand implements CommandExecutor,TabCompleter {
    private final LifeStealPlugin plugin;
    public LifeStealCommand(LifeStealPlugin plugin){this.plugin=plugin;}
    private boolean world(CommandSender s){if(s instanceof Player p&&!plugin.isLifeStealWorld(p.getWorld())){plugin.send(p,"only-world");return false;}return true;}
    private boolean perm(CommandSender s,String p){if(s.hasPermission("lifesteal.admin")||s.hasPermission(p))return true;s.sendMessage(plugin.msg("no-permission"));return false;}
    private Player target(String n){return Bukkit.getPlayerExact(n);}
    private Integer num(CommandSender s,String v){try{return Integer.parseInt(v);}catch(Exception e){s.sendMessage(plugin.msg("invalid-number"));return null;}}
    private UUID id(String x){try{return UUID.fromString(x);}catch(Exception ignored){}Player p=target(x);return p==null?null:p.getUniqueId();}
    private void give(Player p,ItemStack i){for(ItemStack x:p.getInventory().addItem(i).values())p.getWorld().dropItemNaturally(p.getLocation(),x);}
    @Override public boolean onCommand(CommandSender s,Command cmd,String label,String[] a){
        if(!world(s))return true;
        if(cmd.getName().equalsIgnoreCase("hearts")){if(!perm(s,"lifesteal.hearts"))return true;Player p=a.length>0?target(a[0]):s instanceof Player?(Player)s:null;if(p==null){s.sendMessage(plugin.msg("invalid-player"));return true;}if(!plugin.isLifeStealWorld(p.getWorld())){s.sendMessage(plugin.msg("only-world"));return true;}PlayerData d=plugin.getData(p);s.sendMessage(plugin.msg("hearts").replace("%player%",p.getName()).replace("%hearts%",String.valueOf(d.hearts)));return true;}
        if(cmd.getName().equalsIgnoreCase("withdrawheart")){if(!(s instanceof Player p))return true;if(!perm(s,"lifesteal.hearts"))return true;Integer n=a.length==0?1:num(s,a[0]);if(n==null||n<1)return true;PlayerData d=plugin.getData(p);int min=Math.max(0,plugin.getConfig().getInt("hearts.minimum-withdraw",1));if(d.hearts-n<min){plugin.send(p,"not-enough-hearts");return true;}d.hearts-=n;plugin.setMaxHealth(p,d.hearts);give(p,plugin.items().heart(n));plugin.data().saveLifeStealData(p,d);plugin.send(p,"heart-withdrawn",Map.of("amount",String.valueOf(n)));return true;}
        if(cmd.getName().equalsIgnoreCase("revive")){if(!perm(s,"lifesteal.revive"))return true;if(a.length<1){s.sendMessage(plugin.msg("usage").replace("%usage%","/revive <player>"));return true;}World w=s instanceof Player?((Player)s).getWorld():plugin.mainWorld();UUID u=id(a[0]);if(u==null||w==null){s.sendMessage(plugin.msg("invalid-player"));return true;}PlayerData d=plugin.data().loadLifeStealData(u,w);if(!d.eliminated){s.sendMessage(plugin.msg("invalid-player"));return true;}int h=plugin.getConfig().getInt("items.revive-hearts",5);plugin.reviveTarget(w,u,h);s.sendMessage(plugin.msg("revived").replace("%player%",plugin.data().playerName(w,u)).replace("%hearts%",String.valueOf(h)));return true;}
        if(cmd.getName().equalsIgnoreCase("lifesteal"))return admin(s,a);
        return true;
    }
    private boolean admin(CommandSender s,String[] a){
        if(a.length==0){s.sendMessage(plugin.msg("usage").replace("%usage%","/lifesteal <subcommand>"));return true;}
        String sub=a[0].toLowerCase(Locale.ROOT);
        if(sub.equals("reload")){if(!perm(s,"lifesteal.reload"))return true;plugin.reloadPlugin();s.sendMessage(plugin.msg("reload"));return true;}
        if(sub.equals("list")){if(!perm(s,"lifesteal.list"))return true;if(!(s instanceof Player p)||!plugin.isLifeStealWorld(p.getWorld())){s.sendMessage(plugin.msg("only-world"));return true;}s.sendMessage(plugin.msg("list-header"));for(UUID u:plugin.data().eliminatedPlayers(p.getWorld()))s.sendMessage("§c- "+plugin.data().playerName(p.getWorld(),u)+" §7(eliminated)");for(Player q:Bukkit.getOnlinePlayers())if(plugin.isLifeStealWorld(q.getWorld())){PlayerData d=plugin.getData(q);s.sendMessage("§7- "+q.getName()+" §c"+d.hearts+" hearts");}return true;}
        if(a.length<2){s.sendMessage(plugin.msg("usage").replace("%usage%","/lifesteal <subcommand> <player> [amount]"));return true;}
        UUID u=id(a[1]);if(u==null){s.sendMessage(plugin.msg("invalid-player"));return true;}
        World w=s instanceof Player?((Player)s).getWorld():plugin.mainWorld();if(w==null){s.sendMessage(plugin.msg("only-world"));return true;}
        if(sub.equals("revive")){if(!perm(s,"lifesteal.revive"))return true;PlayerData d=plugin.data().loadLifeStealData(u,w);if(!d.eliminated){s.sendMessage(plugin.msg("invalid-player"));return true;}Integer h=a.length>2?num(s,a[2]):plugin.getConfig().getInt("items.revive-hearts",5);if(h==null)return true;plugin.reviveTarget(w,u,h);s.sendMessage(plugin.msg("revived").replace("%player%",plugin.data().playerName(w,u)).replace("%hearts%",String.valueOf(h)));return true;}
        if(!(sub.equals("giveheart")||sub.equals("giverevive")||sub.equals("sethearts")||sub.equals("addhearts")||sub.equals("removehearts")||sub.equals("eliminate")||sub.equals("reset")||sub.equals("hearts"))){s.sendMessage(plugin.msg("usage").replace("%usage%","/lifesteal <giveheart|giverevive|sethearts|addhearts|removehearts|revive|eliminate|reset|hearts|list|reload>"));return true;}
        if(!perm(s,"lifesteal."+sub))return true;
        Player p=Bukkit.getPlayer(u);if(p==null){s.sendMessage(plugin.msg("invalid-player"));return true;}
        PlayerData d=plugin.getData(p);
        if(sub.equals("giveheart")||sub.equals("giverevive")){Integer n=a.length>2?num(s,a[2]):1;if(n==null)return true;give(p,sub.equals("giveheart")?plugin.items().heart(n):plugin.items().revive(n));s.sendMessage(plugin.msg("admin-success"));return true;}
        Integer n=a.length>2?num(s,a[2]):1;if(n==null)return true;
        switch(sub){case "sethearts"->plugin.setHearts(p,n);case "addhearts"->plugin.addHearts(p,n);case "removehearts"->plugin.removeHearts(p,n);case "eliminate"->plugin.setHearts(p,0);case "reset"->plugin.setHearts(p,plugin.startingHearts());case "hearts"->s.sendMessage(plugin.msg("hearts").replace("%player%",p.getName()).replace("%hearts%",String.valueOf(d.hearts)));}
        plugin.data().saveLifeStealData(p,d);if(!sub.equals("hearts"))s.sendMessage(plugin.msg("admin-success"));return true;
    }
    @Override public List<String> onTabComplete(CommandSender s,Command cmd,String alias,String[] a){if(cmd.getName().equalsIgnoreCase("lifesteal")){List<String>x=List.of("giveheart","giverevive","sethearts","addhearts","removehearts","revive","eliminate","reset","hearts","list","reload");if(a.length==1)return x.stream().filter(v->v.startsWith(a[0].toLowerCase())).collect(Collectors.toList());if(a.length==2)return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(v->v.toLowerCase().startsWith(a[1].toLowerCase())).collect(Collectors.toList());}if((cmd.getName().equalsIgnoreCase("hearts")||cmd.getName().equalsIgnoreCase("revive"))&&a.length==1)return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(v->v.toLowerCase().startsWith(a[0].toLowerCase())).collect(Collectors.toList());return List.of();}
}