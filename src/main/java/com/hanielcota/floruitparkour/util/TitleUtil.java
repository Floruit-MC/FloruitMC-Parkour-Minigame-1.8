package com.hanielcota.floruitparkour.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleUtil {

  public void sendTitle(
      Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    var craft = (CraftPlayer) player;
    craft
        .getHandle()
        .playerConnection
        .sendPacket(
            new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut));
    if (title != null) {
      craft
          .getHandle()
          .playerConnection
          .sendPacket(
              new PacketPlayOutTitle(
                  PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title)));
    }
    if (subtitle != null) {
      craft
          .getHandle()
          .playerConnection
          .sendPacket(
              new PacketPlayOutTitle(
                  PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle)));
    }
  }
}
