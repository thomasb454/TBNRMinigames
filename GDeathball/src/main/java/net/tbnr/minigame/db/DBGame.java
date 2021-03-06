/*
 * Copyright (c) 2014.
 * CogzMC LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.tbnr.minigame.db;

import com.comphenix.protocol.utility.MinecraftReflection;
import net.tbnr.gearz.GearzPlugin;
import net.tbnr.gearz.arena.Arena;
import net.tbnr.gearz.effects.EnderBar;
import net.tbnr.gearz.game.GameCountdown;
import net.tbnr.gearz.game.GameCountdownHandler;
import net.tbnr.gearz.game.GameMeta;
import net.tbnr.gearz.network.GearzPlayerProvider;
import net.tbnr.manager.TBNRMinigame;
import net.tbnr.manager.TBNRPlayer;
import net.tbnr.manager.classes.TBNRAbstractClass;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rigor789 on 2013.12.19
 */
@GameMeta(
        shortName = "DB",
        longName = "Deathball",
        description = "All players have Speed 4, Jump 5 and take no fall damage. Players are armed with unlimited snowballs"+
		        " and aim to hit other players, reducing their lives. Players are out when they hit 0 points."+
		        " Secondary weapon is a stick with knock-back 3 that affects points the same as 5 snowballs."+
		        " The winner is the player with most points, the game will end early if only one player remains.",
        mainColor = ChatColor.DARK_AQUA,
        secondaryColor = ChatColor.DARK_PURPLE,
        author = "Rigi",
        version = "1.0",
        key = "deathball",
        maxPlayers = 24,
        minPlayers = 4,
        playerCountMode = GameMeta.PlayerCountMode.Any)
public final class DBGame extends TBNRMinigame implements GameCountdownHandler {

    private DBArena dbarena;
    private HashMap<TBNRPlayer, Integer> lives;
    private HashMap<TBNRPlayer, Integer> score;
    private GameCountdown countdown;

    public DBGame(List<TBNRPlayer> players, Arena arena, GearzPlugin<TBNRPlayer, TBNRAbstractClass> plugin, GameMeta meta, Integer id, GearzPlayerProvider<TBNRPlayer> playerProvider) {
        super(players, arena, plugin, meta, id, playerProvider);
        if (!(arena instanceof DBArena)) throw new RuntimeException("Invalid instance");
        this.dbarena = (DBArena) arena;
        this.lives = new HashMap<>();
        this.score = new HashMap<>();
    }

    @Override
    protected void gamePreStart() {
        for (TBNRPlayer player : getPlayers()) {
            lives.put(player, 75);
            score.put(player, 0);
        }
    }

    @Override
    protected void gameStarting() {
        updateScoreboard();
        countdown = new GameCountdown(60 * 5, this, this);
        countdown.start();
    }

    @Override
    protected void gameEnding() {

    }

    @Override
    protected boolean canBuild(TBNRPlayer player) {
        return false;
    }

    @Override
    protected boolean canPvP(TBNRPlayer attacker, TBNRPlayer target) {
        return false;
    }

    @Override
    protected boolean canUse(TBNRPlayer player) {
        return true;
    }

    @Override
    protected boolean canBreak(TBNRPlayer player, Block block) {
        return false;
    }

    @Override
    protected boolean canPlace(TBNRPlayer player, Block block) {
        return false;
    }

    @Override
    protected boolean canMove(TBNRPlayer player) {
        if (player.getPlayer().getLocation().getY() < 0) {
            player.getPlayer().teleport(playerRespawn(player));
            removeScore(player, 1);
        }
        return true;
    }

    @Override
    protected boolean canDrawBow(TBNRPlayer player) {
        return false;
    }

    @Override
    protected void playerKilled(TBNRPlayer dead, LivingEntity killer) {

    }

    @Override
    protected void playerKilled(TBNRPlayer dead, TBNRPlayer killer) {

    }

    @Override
    protected void mobKilled(LivingEntity killed, TBNRPlayer killer) {

    }

    @Override
    protected boolean canDropItem(TBNRPlayer player, ItemStack itemToDrop) {
        return false;
    }

    @Override
    protected Location playerRespawn(TBNRPlayer player) {
        return getArena().pointToLocation(dbarena.spawnPoints.random());
    }

    @Override
    protected boolean canPlayerRespawn(TBNRPlayer player) {
        return lives.containsKey(player);
    }

    @Override
    protected void removePlayerFromGame(TBNRPlayer player) {
        if (lives.containsKey(player)) lives.remove(player);
    }

    @Override
    protected void onDamage(Entity damager, Entity target, EntityDamageByEntityEvent event) {
        if (!((damager instanceof Snowball) || (damager instanceof Player))) return;
        if (!(target instanceof Player)) return;
        TBNRPlayer target1 = resolvePlayer((Player) target);
        if (!isIngame(target1) || isSpectating(target1)) return;
        int value;
        TBNRPlayer attacker;
        if (damager instanceof Snowball) {
            Snowball snowball = (Snowball) damager;
            if (!(snowball.getShooter() instanceof Player)) return;
            attacker = resolvePlayer((Player) snowball.getShooter());
            if(attacker.equals(target1)) return;
            if (isSpectating(attacker)) return;
            value = 1;
            event.setCancelled(true);
            attacker.getPlayer().playSound(attacker.getPlayer().getLocation(), Sound.FIREWORK_BLAST2, 5f, 1f);
            attacker.getPlayer().sendMessage(getPluginFormat("formats.hit-player", true, new String[]{"<player>", target1.getUsername()}, new String[]{"<points>", value + ""}));
        } else {
            attacker = resolvePlayer((Player) damager);
            if (isSpectating(attacker)) return;
            if (attacker.getPlayer().getItemInHand().getType() != Material.STICK) return;
            value = 5;
            attacker.getPlayer().playSound(attacker.getPlayer().getLocation(), Sound.FIREWORK_BLAST, 5f, 1f);
            attacker.getPlayer().sendMessage(getPluginFormat("formats.hit-player-stick", true, new String[]{"<player>", target1.getUsername()}, new String[]{"<points>", value + ""}));
        }
        addScore(attacker, 1);
        removeScore(target1, value);
        fakeDeath(target1);
        updateScoreboard();
        checkGame();
    }

    @Override
    protected int xpForPlaying() {
        return 100;
    }

    @Override
    protected void activatePlayer(TBNRPlayer player) {
        player.getTPlayer().addInfinitePotionEffect(PotionEffectType.SPEED, 4);
        player.getTPlayer().addInfinitePotionEffect(PotionEffectType.JUMP, 5);
        player.getTPlayer().giveItem(Material.SNOW_BALL, 10);
        ItemStack stack = new ItemStack(Material.STICK, 1);
        stack = MinecraftReflection.getBukkitItemStack(stack);
        stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
        player.getPlayer().getInventory().addItem(stack);
    }

    @Override
    protected boolean allowHunger(TBNRPlayer player) {
        return false;
    }

    @Override
    protected boolean onFallDamage(TBNRPlayer player, EntityDamageEvent event) {
        return event.getCause() == EntityDamageEvent.DamageCause.FALL;
    }

    @Override
    protected void onSnowballThrow(TBNRPlayer player) {
        player.getTPlayer().giveItem(Material.SNOW_BALL, 1);
    }

    private void addScore(TBNRPlayer player, int scr) {
        int sc = score.get(player) + scr;
        score.put(player, sc);
    }

    private void removeScore(TBNRPlayer player, int scr) {
        int sc = lives.get(player);
        sc = sc - scr;
        if (sc <= 0) {
            lives.remove(player);
            broadcast(getPluginFormat("formats.out-of-game", true, new String[]{"<player>", player.getUsername()}));
            return;
        }
        player.getPlayer().sendMessage(getPluginFormat("formats.lost-points", true, new String[]{"<points>", scr + ""}));
        lives.put(player, sc);
    }

    private void checkGame() {
        if (lives.size() == 1) {
            onCountdownComplete(countdown);
        }
    }

    private void updateScoreboard() {
        for (TBNRPlayer player : getPlayers()) {
            if(!player.isValid()) continue;
            player.getTPlayer().resetScoreboard();
            player.getTPlayer().setScoreboardSideTitle(getPluginFormat("formats.scoreboard-title", false));
            for (TBNRPlayer player1 : lives.keySet()) {
                if(!player1.isValid()) continue;
                player.getTPlayer().setScoreBoardSide(player1.getUsername(), lives.get(player1));
            }
        }
    }

    private void updateEnderBar() {
        for (TBNRPlayer player : getPlayers()) {
            if(!player.isValid()) continue;
            EnderBar.setTextFor(player, getPluginFormat("formats.time", false, new String[]{"<time>", formatInt(countdown.getSeconds() - countdown.getPassed())}));
            EnderBar.setHealthPercent(player, ((float) countdown.getSeconds() - countdown.getPassed()) / (float) countdown.getSeconds());
        }
    }

    @Override
    public void onCountdownStart(Integer max, GameCountdown countdown) {
        broadcast(getPluginFormat("formats.game-started", false, new String[]{"<time>", max + ""}));
    }

    @Override
    public void onCountdownChange(Integer seconds, Integer max, GameCountdown countdown) {
        updateEnderBar();
    }

    @Override
    public void onCountdownComplete(GameCountdown countdown) {
        int maxScore = 0;
        TBNRPlayer winner = null;
        for (Map.Entry<TBNRPlayer, Integer> entry : lives.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue() * score.get(entry.getKey());
                winner = entry.getKey();
            }
        }
        if (winner != null) {
            broadcast(getPluginFormat("formats.winner", true, new String[]{"<name>", winner.getUsername()}));
            if (winner.getPlayer() != null) {
                if (winner.getPlayer().isOnline()) {
                    addGPoints(winner, 150);
                    getArena().getWorld().strikeLightningEffect(winner.getPlayer().getLocation());
                    addWin(winner);
                }
            }
        }
        finishGame();
    }

    private String formatInt(Integer integer) {
        if (integer < 60) return String.format("%02d", integer);
        else return String.format("%02d:%02d", (integer / 60), (integer % 60));
    }
}
