package com.hanielcota.floruitparkour.config;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class ParkourItem {
  private final Material material;
  private final int amount;
  private final String displayName;
  private final String command;

  public ParkourItem(Material material, int amount, String displayName, String command) {
    this.material = material;
    this.amount = amount;
    this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
    this.command = command;
  }

  public ItemStack toItemStack() {
    ItemStack item = new ItemStack(material, amount);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(displayName);
      item.setItemMeta(meta);
    }
    return item;
  }
}
