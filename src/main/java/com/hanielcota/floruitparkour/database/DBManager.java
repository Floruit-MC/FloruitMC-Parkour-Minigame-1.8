package com.hanielcota.floruitparkour.database;

import com.hanielcota.floruitparkour.FloruitParkour;
import com.hanielcota.floruitparkour.config.DatabaseConfig;
import com.hanielcota.floruitparkour.session.Session;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

/** Gerencia conexão e operações MySQL de forma assíncrona. */
@RequiredArgsConstructor
public class DBManager {
  private final FloruitParkour plugin;
  private final DatabaseConfig cfg;
  private HikariDataSource ds;

  public void init() {
    var hc = new HikariConfig();
    hc.setJdbcUrl(
        "jdbc:mysql://"
            + cfg.getHost()
            + ":"
            + cfg.getPort()
            + "/"
            + cfg.getDatabaseName()
            + "?useSSL=false");
    hc.setUsername(cfg.getUser());
    hc.setPassword(cfg.getPassword());
    hc.addDataSourceProperty("cachePrepStmts", "true");
    hc.addDataSourceProperty("prepStmtCacheSize", "250");
    hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    ds = new HikariDataSource(hc);
    createTable();
  }

  private void createTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS floruitparkour_results ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "player_uuid VARCHAR(36) NOT NULL,"
            + "time_ms BIGINT NOT NULL,"
            + "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ")";
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      ps.execute();
    } catch (SQLException e) {
      plugin.getLogger().severe("Erro criando tabela: " + e.getMessage());
    }
  }

  public void saveResult(Session s) {
    Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            () -> {
              String sql = "INSERT INTO floruitparkour_results (player_uuid, time_ms) VALUES (?,?)";
              try (Connection c = ds.getConnection();
                  PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, s.getPlayer().getUniqueId().toString());
                ps.setLong(2, s.getElapsedTime());
                ps.executeUpdate();
              } catch (SQLException e) {
                plugin.getLogger().severe("Erro salvando resultado: " + e.getMessage());
                String msg = plugin.getConfigManager().getMessages().get("dbError");
                if (msg != null) s.getPlayer().sendMessage(msg);
              }
            });
  }

  public void shutdown() {
    if (ds != null && !ds.isClosed()) {
      ds.close();
    }
  }
}
