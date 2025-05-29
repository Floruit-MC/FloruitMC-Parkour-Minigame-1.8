package com.hanielcota.floruitparkour;

import co.aikar.commands.PaperCommandManager;
import com.hanielcota.floruitparkour.commands.ParkourCommand;
import com.hanielcota.floruitparkour.config.ConfigManager;
import com.hanielcota.floruitparkour.course.CourseManager;
import com.hanielcota.floruitparkour.database.DBManager;
import com.hanielcota.floruitparkour.listener.ParkourListener;
import com.hanielcota.floruitparkour.scoreboard.ParkourBoardManager;
import com.hanielcota.floruitparkour.session.SessionManager;
import com.hanielcota.floruitparkour.util.ActionBarUtil;
import com.hanielcota.floruitparkour.util.TitleUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class FloruitParkour extends JavaPlugin {

  private ConfigManager configManager;
  private DBManager dbManager;
  private SessionManager sessionManager;
  private TitleUtil titleUtil;
  private ActionBarUtil actionBarUtil;
  private PaperCommandManager commandManager;
  private ParkourBoardManager boardManager;
  private CourseManager courseManager; // Opcional para expansão de múltiplos percursos

  @Override
  public void onEnable() {
    saveDefaultConfig();

    titleUtil = new TitleUtil();
    actionBarUtil = new ActionBarUtil();

    configManager = new ConfigManager(this);
    configManager.load();

    dbManager = new DBManager(this, configManager.getDatabaseConfig());
    dbManager.init();

    sessionManager = new SessionManager();

    courseManager = new CourseManager(this, configManager); // Para expansão futura
    courseManager.loadCourses();

    getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

    Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);

    boardManager = new ParkourBoardManager(this, sessionManager);
    getServer().getPluginManager().registerEvents(boardManager, this);
    boardManager.startUpdater();

    if (configManager.isActionBarEnabled()) {
      int interval = configManager.getActionBarInterval();
      Bukkit.getScheduler()
          .runTaskTimer(
              this,
              () ->
                  sessionManager
                      .getSessions()
                      .forEach(
                          session -> {
                            long secs = session.getElapsedTime() / 1000;
                            String raw = configManager.getActionBarMessage();
                            String msg = raw.replace("{time}", String.valueOf(secs));
                            actionBarUtil.sendActionBar(session.getPlayer(), msg);
                          }),
              0L,
              interval);
    }

    commandManager = new PaperCommandManager(this);
    commandManager.registerCommand(new ParkourCommand(this));
  }

  @Override
  public void onDisable() {
    dbManager.shutdown();
  }
}
