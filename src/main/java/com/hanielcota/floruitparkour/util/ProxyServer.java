package com.hanielcota.floruitparkour.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class ProxyServer {

  private JavaPlugin plugin;

  public void sendToServer(Player player, String serverName) {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(b);

    try {
      out.writeUTF("Connect");
      out.writeUTF(serverName);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
  }
}
