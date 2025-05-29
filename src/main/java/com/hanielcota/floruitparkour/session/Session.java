package com.hanielcota.floruitparkour.session;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** Representa a sessão de parkour de um jogador. */
@Getter
public class Session {
  private final Player player;
  private final long startTime;

  @Setter private Location lastCheckpoint;
  @Setter private long lastCheckpointTime;
  @Setter private long lastTriggerTime; // debounce de placas
  @Getter @Setter
  private int currentLevel = 1;

  public Session(Player player) {
    this.player = player;
    this.startTime = System.currentTimeMillis();
    this.lastTriggerTime = 0;
  }

  public long getElapsedTime() {
    return System.currentTimeMillis() - startTime;
  }
}
