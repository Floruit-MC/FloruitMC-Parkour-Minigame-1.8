package com.hanielcota.floruitparkour.config;

import com.hanielcota.floruitparkour.FloruitParkour;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
public class ConfigManager {
  private final FloruitParkour plugin;

  // itens de start: slot → ParkourItem
  private final Map<Integer, ParkourItem> startItems = new HashMap<>();

  // mensagens
  private final Map<String, String> messages = new HashMap<>();

  // DB
  private DatabaseConfig databaseConfig;

  // ActionBar
  private boolean actionBarEnabled;
  private String actionBarMessage;
  private int actionBarInterval;

  // Som ao iniciar
  private boolean startSoundEnabled;
  private String startSoundType;
  private float startSoundVolume;
  private float startSoundPitch;

  // Bungee leave
  private String leaveServer;

  @Getter private boolean scoreboardEnabled;
  @Getter private String scoreboardTitle;
  @Getter private List<String> scoreboardLines;

  public ConfigManager(FloruitParkour plugin) {
    this.plugin = plugin;
  }

  public void load() {
    loadMessages();
    loadItems();
    loadDatabaseConfig();
    loadActionBarSettings();
    loadStartSoundSettings();
    loadLeaveServer();

    loadScoreboardSettings();
  }

  private void loadScoreboardSettings() {
    var sec = plugin.getConfig().getConfigurationSection("scoreboard");
    if (sec == null) return;

    scoreboardEnabled = sec.getBoolean("enabled", true);
    scoreboardTitle =
        ChatColor.translateAlternateColorCodes('&', sec.getString("title", "&6&lParkour"));
    scoreboardLines = sec.getStringList("lines");
  }

  private void loadMessages() {
    var sec = plugin.getConfig().getConfigurationSection("messages");
    if (sec == null) return;
    sec.getKeys(false)
        .forEach(
            key -> {
              String raw = sec.getString(key, "");
              messages.put(key, ChatColor.translateAlternateColorCodes('&', raw));
            });
  }

  private void loadItems() {
    var sec = plugin.getConfig().getConfigurationSection("items.give-on-start");
    if (sec == null) return;
    sec.getKeys(false)
        .forEach(
            slotKey -> {
              try {
                int slot = Integer.parseInt(slotKey);
                var itemSec = sec.getConfigurationSection(slotKey);
                Material mat =
                    Material.valueOf(itemSec.getString("material", "STONE").toUpperCase());
                int amount = itemSec.getInt("amount", 1);
                String name = itemSec.getString("name", mat.name());
                String cmd = itemSec.getString("command", "");
                startItems.put(slot, new ParkourItem(mat, amount, name, cmd));
              } catch (Exception e) {
                plugin.getLogger().warning("Erro carregando item em slot " + slotKey);
              }
            });
  }

  private void loadDatabaseConfig() {
    var sec = plugin.getConfig().getConfigurationSection("database");
    if (sec == null) return;
    String host = sec.getString("host", "localhost");
    int port = sec.getInt("port", 3306);
    String db = sec.getString("name", "floruitparkour");
    String user = sec.getString("user", "root");
    String pass = sec.getString("password", "");
    databaseConfig = new DatabaseConfig(host, port, db, user, pass);
  }

  private void loadActionBarSettings() {
    var sec = plugin.getConfig().getConfigurationSection("actionbar");
    actionBarEnabled = sec != null && sec.getBoolean("enabled", true);
    actionBarMessage =
        (sec != null
            ? ChatColor.translateAlternateColorCodes(
                '&', sec.getString("message", "&e⏱ Tempo: &f{time}s"))
            : "&e⏱ Tempo: &f{time}s");
    actionBarInterval = sec != null ? sec.getInt("interval", 20) : 20;
  }

  public Location getPlateLocation(String nivel, String tipo) {
    String path = "parkour.levels." + nivel + "." + tipo.toLowerCase();
    if (!plugin.getConfig().contains(path + ".world")) return null;

    String world = plugin.getConfig().getString(path + ".world");
    int x = plugin.getConfig().getInt(path + ".x");
    int y = plugin.getConfig().getInt(path + ".y");
    int z = plugin.getConfig().getInt(path + ".z");

    return plugin.getServer().getWorld(world) == null
        ? null
        : new Location(plugin.getServer().getWorld(world), x, y, z);
  }

  public int getMaxLevel() {
    if (!plugin.getConfig().isConfigurationSection("parkour.levels")) return 0;
    return plugin.getConfig().getConfigurationSection("parkour.levels").getKeys(false).stream()
        .mapToInt(
            key -> {
              try {
                return Integer.parseInt(key);
              } catch (NumberFormatException e) {
                return 0;
              }
            })
        .max()
        .orElse(0);
  }

  private void loadStartSoundSettings() {
    var sec = plugin.getConfig().getConfigurationSection("start-sound");
    startSoundEnabled = sec != null && sec.getBoolean("enabled", true);
    startSoundType = sec != null ? sec.getString("type", "NOTE_PLING") : "NOTE_PLING";
    startSoundVolume = sec != null ? (float) sec.getDouble("volume", 1.0) : 1.0f;
    startSoundPitch = sec != null ? (float) sec.getDouble("pitch", 1.0) : 1.0f;
  }

  private void loadLeaveServer() {
    leaveServer = plugin.getConfig().getString("leave-server", "lobby");
  }
}
