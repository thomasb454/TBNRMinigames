package net.tbnr.gearz.hub;

import net.tbnr.gearz.hub.items.HubItem;
import net.tbnr.gearz.hub.items.warpstar.WarpStar;
import net.tbnr.gearz.hub.items.warpstar.WarpStarConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by rigor789 on 2013.12.21..
 */
public class HubItems implements Listener {

    private final ArrayList<HubItem> items;

    private WarpStarConfig warpStarConfig = null;

    public HubItems() {
        items = new ArrayList<>();
        //items.add(new RuleBook());
        if (TBNRHub.getInstance().getConfig().getBoolean("warpstar")) {
            warpStarConfig = new WarpStarConfig();
            items.add(new WarpStar(warpStarConfig));
            items.add(new ServerJoiner());
        }
    }

    public void refreshWarpStar() {
        warpStarConfig.refresh();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (HubItem item : items) {
            if(item.getItem().getType() == Material.ANVIL) { // ONLY FOR DEBUG PURPOSES!
                if(!event.getPlayer().hasPermission("gearz.serverselector")) continue;
                if (shouldAdd(event.getPlayer(), item.getItem())) event.getPlayer().getInventory().addItem(item.getItem());
            }
            if (shouldAdd(event.getPlayer(), item.getItem())) event.getPlayer().getInventory().addItem(item.getItem());
        }
    }

    private boolean shouldAdd(Player player, ItemStack item) {
        return !player.getInventory().contains(item);
    }
}
