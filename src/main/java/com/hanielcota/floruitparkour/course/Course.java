package com.hanielcota.floruitparkour.course;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@Getter
@AllArgsConstructor
public class Course {
  private final String name;
  private final List<ParkourStage> stages;
}
