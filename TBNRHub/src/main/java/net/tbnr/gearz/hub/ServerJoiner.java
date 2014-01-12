package net.tbnr.gearz.hub;

import net.tbnr.gearz.hub.items.HubItem;
import net.tbnr.gearz.netcommand.BouncyUtils;
import net.tbnr.gearz.server.Server;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.util.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by rigor789 on 2014.01.12..
 */
public class ServerJoiner extends HubItem {

    private InventoryGUI mainGUI;
    private ServerJoinerCallback callback;

    public ServerJoiner(){
        super(true);
        this.callback = new ServerJoinerCallback();
        this.mainGUI = new InventoryGUI(getGameTypes(), "Server Selector", callback);
        updateGUIs();
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(TBNRHub.getInstance(), new ServerJoinerUpdater(this), 0, 20);
    }

    public void updateGameTypes(){
        mainGUI.updateContents(getGameTypes());
    }

    public void updateGUIs(){
        callback.updateGUIs();
    }

    private ArrayList<InventoryGUI.InventoryGUIItem> getGameTypes(){
        ArrayList<InventoryGUI.InventoryGUIItem> gameTypes = new ArrayList<>();
        for(String type : ServerManager.getUniqueGames()){
            gameTypes.add(new InventoryGUI.InventoryGUIItem(
                    new ItemStack(Material.ANVIL),
                    type
            ));
        }
        return gameTypes;
    }


    @Override
    public ItemStack getItem() {
        return new ItemStack(Material.ANVIL);
    }

    @Override
    public void rightClicked(Player player) {
        this.mainGUI.open(player);
    }

    /**
     * The callback to handle most of the things.
     */
    private class ServerJoinerCallback implements InventoryGUI.InventoryGUICallback {

        private HashMap<String, InventoryGUI> gameGUIs;

        public  ServerJoinerCallback(){
            this.gameGUIs = new HashMap<>();
        }

        public void updateGUIs(){
            this.gameGUIs = getGameGUIs();
            for(String type : this.gameGUIs.keySet()){
                InventoryGUI gameGUI = this.gameGUIs.get(type);
                gameGUI.updateContents(getServersForType(type));
            }
        }

        private HashMap<String, InventoryGUI> getGameGUIs() {
            for(String type : ServerManager.getUniqueGames()){
                if(gameGUIs.containsKey(type)) continue;
                gameGUIs.put(type, new InventoryGUI(null, type + " Servers", new SingleServerCallback(type)));
            }
            return gameGUIs;
        }

        private ArrayList<InventoryGUI.InventoryGUIItem> getServersForType(String type){
            ArrayList<InventoryGUI.InventoryGUIItem> servers = new ArrayList<>();
            for(Server server : ServerManager.getServersWithGame(type)){
                servers.add(new InventoryGUI.InventoryGUIItem(
                        new ItemStack(Material.WOOL),
                        server.getGame() + " #" + server.getNumber(),
                        Arrays.asList(
                                server.getStatusString(),
                                server.getPlayerCount() + " / " + server.getMaximumPlayers()
                        )
                ));
            }
            return servers;
        }

        @Override
        public void onItemSelect(InventoryGUI gui, InventoryGUI.InventoryGUIItem item, Player player) {
            gui.close(player);
            if(!gameGUIs.containsKey(item.getName())) return;
            gameGUIs.get(item.getName()).open(player);
        }

        @Override
        public void onGUIOpen(InventoryGUI gui, Player player) {
            player.sendMessage("Loading Server List");
        }

        @Override
        public void onGUIClose(InventoryGUI gui, Player player) {
            player.sendMessage("Server Selector Closed");
        }
    }

    private class ServerJoinerUpdater extends BukkitRunnable {

        private ServerJoiner joiner;

        public ServerJoinerUpdater(ServerJoiner joiner){
            this.joiner = joiner;
        }

        @Override
        public void run() {
            joiner.updateGameTypes();
            joiner.updateGUIs();
        }
    }

    private class  SingleServerCallback implements InventoryGUI.InventoryGUICallback {

        private String type;

        public SingleServerCallback(String type){
            this.type = type;
        }

        @Override
        public void onItemSelect(InventoryGUI gui, InventoryGUI.InventoryGUIItem item, Player player) {
            for(Server server : ServerManager.getServersWithGame(type)){
                if(server.getNumber() != item.getSlot()) continue;
                gui.close(player);
                if(server.isCanJoin()){
                    BouncyUtils.sendPlayerToServer(player, server.getBungee_name());
                }
            }
        }

        @Override
        public void onGUIOpen(InventoryGUI gui, Player player) {
            player.sendMessage("Lising servers for " + this.type);
        }

        @Override
        public void onGUIClose(InventoryGUI gui, Player player) {

        }
    }
}