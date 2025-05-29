package com.hanielcota.floruitparkour.session;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.bukkit.entity.Player;

/** Gerencia todas as sessões ativas. */
@Getter
public class SessionManager {
  private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

  public Session startSession(Player p) {
    if (sessions.containsKey(p.getUniqueId())) return null;
    var s = new Session(p);
    sessions.put(p.getUniqueId(), s);
    return s;
  }

  public Session stopSession(Player p) {
    return sessions.remove(p.getUniqueId());
  }

  public Session getSession(Player p) {
    return sessions.get(p.getUniqueId());
  }

  public Collection<Session> getSessions() {
    return sessions.values();
  }
}
