package com.hanielcota.floruitparkour.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBarUtil {

  public void sendActionBar(Player player, String message) {
    var craft = (CraftPlayer) player;
    craft.getHandle().playerConnection
        .sendPacket(new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
  }
}
