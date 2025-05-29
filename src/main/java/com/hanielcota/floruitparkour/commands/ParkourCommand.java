package com.hanielcota.floruitparkour.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.hanielcota.floruitparkour.FloruitParkour;
import com.hanielcota.floruitparkour.session.Session;
import com.hanielcota.floruitparkour.util.ProxyServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@CommandAlias("parkour")
@Description("Comandos do Parkour")
public class ParkourCommand extends BaseCommand {

  private final FloruitParkour plugin;

  public ParkourCommand(FloruitParkour plugin) {
    this.plugin = plugin;
  }

  @Subcommand("leave")
  @CommandPermission("parkour.use")
  @Description("Sai do parkour e retorna ao servidor configurado")
  public void onLeave(Player player) {
    Session session = plugin.getSessionManager().stopSession(player);

    if (session == null) {
      sendMessage(player, "not-in-parkour");
      return;
    }

    player.getInventory().clear();
    sendMessage(player, "left-parkour");

    new ProxyServer(plugin).sendToServer(player, plugin.getConfigManager().getLeaveServer());
  }

  @Subcommand("restart")
  @CommandPermission("parkour.use")
  @Description("Volta ao último checkpoint alcançado")
  public void onRestart(Player player) {
    Session session = plugin.getSessionManager().getSession(player);

    if (session == null) {
      sendMessage(player, "not-in-parkour");
      return;
    }

    Location cp = session.getLastCheckpoint();
    if (cp == null) {
      sendMessage(player, "no-checkpoint");
      return;
    }

    player.teleport(cp);
    sendMessage(player, "checkpoint-restart");
  }

  @Subcommand("checkpoint")
  @CommandPermission("parkour.use")
  @Description("Teleporta para o último checkpoint")
  public void onCheckpoint(Player player) {
    Session session = plugin.getSessionManager().getSession(player);

    if (session == null) {
      sendMessage(player, "not-in-parkour");
      return;
    }

    Location cp = session.getLastCheckpoint();
    if (cp == null) {
      sendMessage(player, "no-checkpoint");
      return;
    }

    player.teleport(cp);
    sendMessage(player, "checkpoint-teleport");
  }

  @Subcommand("status")
  @CommandPermission("parkour.use")
  @Description("Exibe o tempo atual da sua sessão")
  public void onStatus(Player player) {
    Session session = plugin.getSessionManager().getSession(player);

    if (session == null) {
      sendMessage(player, "not-in-parkour");
      return;
    }

    long secs = session.getElapsedTime() / 1000;

    String msg =
        plugin
            .getConfigManager()
            .getMessages()
            .getOrDefault("status", "&eTempo atual: &f{time}s")
            .replace("{time}", String.valueOf(secs));

    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
  }


  @Subcommand("set")
  @Syntax("<nivel> <start|checkpoint>")
  @Description("Define a placa de início ou checkpoint para o nível.")
  public void onSet(Player player, String nivel, String tipo) {
    Set<Material> transparent = new HashSet<>();
    for (Material mat : Material.values()) {
      if (mat.isTransparent()) transparent.add(mat);
    }

    Block target = player.getTargetBlock(transparent, 5);
    if (target == null) {
      player.sendMessage(ChatColor.RED + "Olhe para uma placa de pressão válida (até 5 blocos)!");
      return;
    }

    String plateType = target.getType().toString().toLowerCase();
    if (!plateType.contains("plate")) {
      player.sendMessage(ChatColor.RED + "Precisa ser uma placa de pressão.");
      return;
    }

    Location loc = target.getLocation();
    String path = "parkour.levels." + nivel + "." + tipo.toLowerCase();

    plugin.getConfig().set(path + ".world", loc.getWorld().getName());
    plugin.getConfig().set(path + ".x", loc.getBlockX());
    plugin.getConfig().set(path + ".y", loc.getBlockY());
    plugin.getConfig().set(path + ".z", loc.getBlockZ());

    plugin.saveConfig();
    player.sendMessage(ChatColor.GREEN + "Placa de " + tipo + " do nível " + nivel + " salva com sucesso!");
  }

  private void sendMessage(Player player, String key) {
    String msg = plugin.getConfigManager().getMessages().get(key);
    if (msg != null) {
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
  }
}
