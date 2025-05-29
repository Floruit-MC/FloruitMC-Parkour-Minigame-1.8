package com.hanielcota.floruitparkour.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import java.util.Set;

@Getter
@AllArgsConstructor
public class ParkourStage {
    private final int stageNumber;
    private final Location startPlate;
    private final Location endPlate;
    private final Set<Location> checkpointPlates;
}
