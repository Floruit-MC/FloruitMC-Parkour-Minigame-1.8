package com.hanielcota.floruitparkour.listener;

import com.hanielcota.floruitparkour.FloruitParkour;
import com.hanielcota.floruitparkour.config.ParkourItem;
import com.hanielcota.floruitparkour.session.Session;
import com.hanielcota.floruitparkour.session.SessionManager;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Listener principal do FloruitParkour. Controla início/final de parkour, checkpoints, iteração com
 * itens de parkour e limpeza de sessões.
 */
public class ParkourListener implements Listener {

  private final FloruitParkour plugin;
  private final SessionManager sessionMgr;

  public ParkourListener(FloruitParkour plugin) {
    this.plugin = plugin;
    this.sessionMgr = plugin.getSessionManager();
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();
    if (to == null || from == null) {
      return;
    }

    if (isSameBlock(from, to)) {
      return;
    }

    Player player = event.getPlayer();
    Block block = to.getBlock();
    Material type = block.getType();
    Session session = sessionMgr.getSession(player);

    long now = System.currentTimeMillis();
    if (session != null && now - session.getLastTriggerTime() < 1000) {
      return;
    }

    if (type == Material.IRON_PLATE) {
      session = handleIronPlate(player, session);
      if (session != null) {
        session.setLastTriggerTime(now);
      }
      return;
    }

    if (type == Material.GOLD_PLATE) {
      handleGoldPlate(player, session, block.getLocation());
      if (session != null) {
        session.setLastTriggerTime(now);
      }
    }
  }

  private boolean isSameBlock(Location a, Location b) {
    return a.getWorld().equals(b.getWorld())
        && a.getBlockX() == b.getBlockX()
        && a.getBlockY() == b.getBlockY()
        && a.getBlockZ() == b.getBlockZ();
  }

  private Session handleIronPlate(Player player, Session session) {
    if (session == null) {
      session = sessionMgr.startSession(player);
      giveStartItems(player);

      // Título ao iniciar
      String titleMsg = plugin.getConfigManager().getMessages().get("start");
      plugin.getTitleUtil().sendTitle(player, titleMsg, null, 10, 70, 20);

      // Som ao iniciar
      if (plugin.getConfigManager().isStartSoundEnabled()) {
        String soundName = plugin.getConfigManager().getStartSoundType();
        try {
          Sound sound = Sound.valueOf(soundName.toUpperCase());
          player.playSound(
              player.getLocation(),
              sound,
              plugin.getConfigManager().getStartSoundVolume(),
              plugin.getConfigManager().getStartSoundPitch());
        } catch (IllegalArgumentException ex) {
          plugin.getLogger().warning("Som inválido no config: " + soundName);
        }
      }

      return session;
    }

    if (session.getElapsedTime() < 2000) {
      return session;
    }

    // Finaliza parkour
    sessionMgr.stopSession(player);

    long seconds = session.getElapsedTime() / 1000;
    String finishMsg = plugin.getConfigManager().getMessages().get("finish");
    if (finishMsg != null) {
      player.sendMessage(finishMsg.replace("{time}", String.valueOf(seconds)));
    }

    plugin.getDbManager().saveResult(session);
    return null;
  }

  private void handleGoldPlate(Player player, Session session, Location checkpoint) {
    if (session == null) {
      return;
    }

    session.setLastCheckpoint(checkpoint);
    session.setLastCheckpointTime(System.currentTimeMillis());

    String cpMsg = plugin.getConfigManager().getMessages().get("checkpoint");
    if (cpMsg != null) {
      player.sendMessage(cpMsg);
    }
  }

  private void giveStartItems(Player player) {
    player.getInventory().clear();
    for (Map.Entry<Integer, ParkourItem> entry :
        plugin.getConfigManager().getStartItems().entrySet()) {
      player.getInventory().setItem(entry.getKey(), entry.getValue().toItemStack());
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!event.hasItem()) {
      return;
    }

    ItemStack item = event.getItem();
    if (item == null || !item.hasItemMeta()) {
      return;
    }

    String display = item.getItemMeta().getDisplayName();
    if (display == null) {
      return;
    }

    for (ParkourItem pi : plugin.getConfigManager().getStartItems().values()) {
      if (!Objects.equals(display, pi.getDisplayName())) {
        continue;
      }
      String cmd = pi.getCommand();
      if (!cmd.isEmpty()) {
        event.getPlayer().performCommand(cmd.replaceFirst("/", ""));
        event.setCancelled(true);
      }
      break;
    }
  }

  @EventHandler
  public void onPlayerDrop(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (item == null || !item.hasItemMeta()) {
      return;
    }

    String display = item.getItemMeta().getDisplayName();
    for (ParkourItem pi : plugin.getConfigManager().getStartItems().values()) {
      if (Objects.equals(display, pi.getDisplayName())) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    ItemStack item = event.getCurrentItem();
    if (item == null || !item.hasItemMeta()) {
      return;
    }

    String display = item.getItemMeta().getDisplayName();
    for (ParkourItem pi : plugin.getConfigManager().getStartItems().values()) {
      if (Objects.equals(display, pi.getDisplayName())) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getNewItems() == null) {
      return;
    }

    for (ItemStack item : event.getNewItems().values()) {
      if (item == null || !item.hasItemMeta()) {
        continue;
      }
      String display = item.getItemMeta().getDisplayName();

      for (ParkourItem pi : plugin.getConfigManager().getStartItems().values()) {
        if (Objects.equals(display, pi.getDisplayName())) {
          event.setCancelled(true);
          return;
        }
      }
    }
  }

  @EventHandler
  public void onItemDespawn(ItemDespawnEvent event) {
    Item entity = event.getEntity();
    if (entity == null) {
      return;
    }

    ItemStack stack = entity.getItemStack();
    if (stack == null || !stack.hasItemMeta()) {
      return;
    }

    String display = stack.getItemMeta().getDisplayName();
    for (ParkourItem pi : plugin.getConfigManager().getStartItems().values()) {
      if (Objects.equals(display, pi.getDisplayName())) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    sessionMgr.stopSession(event.getPlayer());
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    sessionMgr.stopSession(event.getEntity());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.getPlayer().getInventory().clear();
  }
}
