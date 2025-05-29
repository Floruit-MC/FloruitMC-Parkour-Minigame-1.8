package com.hanielcota.floruitparkour.course;

import com.hanielcota.floruitparkour.FloruitParkour;
import com.hanielcota.floruitparkour.config.ConfigManager;
import java.util.*;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class CourseManager {
  private final FloruitParkour plugin;
  private final ConfigManager config;
  private final Map<String, Course> courses = new LinkedHashMap<>();

  public CourseManager(FloruitParkour plugin, ConfigManager config) {
    this.plugin = plugin;
    this.config = config;
  }

  public void loadCourses() {
    ConfigurationSection root = plugin.getConfig().getConfigurationSection("courses");
    if (root == null) return;

    for (String name : root.getKeys(false)) {
      ConfigurationSection courseSec = root.getConfigurationSection(name);
      if (courseSec == null) continue;

      ConfigurationSection stagesSec = courseSec.getConfigurationSection("stages");
      if (stagesSec == null) continue;

      List<ParkourStage> stages = new ArrayList<>();

      for (String stageKey : stagesSec.getKeys(false)) {
        int stageNum;
        try {
          stageNum = Integer.parseInt(stageKey);
        } catch (NumberFormatException e) {
          plugin.getLogger().warning("Número inválido de estágio: " + stageKey);
          continue;
        }

        ConfigurationSection stageSec = stagesSec.getConfigurationSection(stageKey);
        if (stageSec == null) continue;

        Location start = parse(stageSec.getConfigurationSection("start-plate"));
        Location end = parse(stageSec.getConfigurationSection("end-plate"));
        if (start == null || end == null) {
          plugin.getLogger().warning("Stage incompleto em: " + name + " - " + stageKey);
          continue;
        }

        Set<Location> cps = new HashSet<>();
        ConfigurationSection cpsSec = stageSec.getConfigurationSection("checkpoints");
        if (cpsSec != null) {
          for (String idx : cpsSec.getKeys(false)) {
            Location cp = parse(cpsSec.getConfigurationSection(idx));
            if (cp != null) cps.add(cp);
          }
        }

        stages.add(new ParkourStage(stageNum, start, end, cps));
      }

      // ordena por número do estágio, se necessário
      stages.sort(Comparator.comparingInt(ParkourStage::getStageNumber));
      courses.put(name, new Course(name, stages));
    }
  }


  private Location parse(ConfigurationSection sec) {
    if (sec == null) return null;
    var world = plugin.getServer().getWorld(sec.getString("world", ""));
    if (world == null) return null;
    return new Location(world, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"));
  }
}
