package com.hanielcota.floruitparkour.scoreboard;

import com.hanielcota.floruitparkour.FloruitParkour;
import com.hanielcota.floruitparkour.session.Session;
import com.hanielcota.floruitparkour.session.SessionManager;
import fr.mrmicky.fastboard.FastBoard;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ParkourBoardManager implements Listener {

    private final FloruitParkour plugin;
    private final SessionManager sessionManager;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    public void startUpdater() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (FastBoard board : boards.values()) {
                updateBoard(board);
            }
        }, 0L, 20L); // atualiza a cada 1 segundo
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;

        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);

        board.updateTitle(plugin.getConfigManager().getScoreboardTitle());
        boards.put(player.getUniqueId(), board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FastBoard board = boards.remove(event.getPlayer().getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    private void updateBoard(FastBoard board) {
        Player player = board.getPlayer();
        Session session = sessionManager.getSession(player);

        String time = "N/A";
        String checkpoint = "Não";

        if (session != null) {
            time = session.getElapsedTime() / 1000 + "s";
            checkpoint = session.getLastCheckpoint() != null ? "Sim" : "Não";
        }

        List<String> formattedLines = new ArrayList<>();
        String level = session != null ? String.valueOf(session.getCurrentLevel()) : "N/A";
        for (String line : plugin.getConfigManager().getScoreboardLines()) {
            String replace = ChatColor.translateAlternateColorCodes('&', line)
                    .replace("{time}", time)
                    .replace("{checkpoint}", checkpoint)
                    .replace("{level}", level)
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
            formattedLines.add(replace);
        }

        board.updateLines(formattedLines);
    }
}
