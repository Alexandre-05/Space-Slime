package fr.alex96x2.slimecapture.service;

import fr.alex96x2.slimecapture.SlimeCapturePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Affiche les retours joueur dans la barre d'action (au-dessus de la barre d'XP).
 * Cumule les captures/relâchements rapides et efface après inactivité.
 */
public class ActionBarService {

    private final SlimeCapturePlugin plugin;
    private final MessageService messages;
    private final Map<UUID, BarState> states = new HashMap<>();

    public ActionBarService(SlimeCapturePlugin plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void shutdown() {
        for (BarState state : states.values()) {
            cancelClearTask(state);
        }
        states.clear();
    }

    public void clear(Player player) {
        BarState state = states.remove(player.getUniqueId());
        if (state != null) {
            cancelClearTask(state);
        }
        player.sendActionBar(Component.empty());
    }

    public void recordCapture(Player player, int amount, int total, int capacity) {
        BarState state = stateOf(player);
        state.capturedBurst += amount;
        state.total = total;
        state.capacity = capacity;
        state.staticLine = null;
        touch(player, state);
    }

    public void recordRelease(Player player, int amount, int remaining, int capacity) {
        BarState state = stateOf(player);
        state.releasedBurst += amount;
        state.total = remaining;
        state.capacity = capacity;
        state.staticLine = null;
        touch(player, state);
    }

    public void show(Player player, String messageKey, Map<String, String> placeholders) {
        BarState state = stateOf(player);
        state.staticLine = messages.get(messageKey, placeholders);
        state.capturedBurst = 0;
        state.releasedBurst = 0;
        touch(player, state);
    }

    public void show(Player player, String messageKey) {
        show(player, messageKey, null);
    }

    private BarState stateOf(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), id -> new BarState());
    }

    private void touch(Player player, BarState state) {
        cancelClearTask(state);
        state.clearGeneration++;
        render(player, state);

        int idleTicks = plugin.getPluginConfig().actionBarIdleTicks();
        int gen = state.clearGeneration;
        state.clearTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> clearAfterIdle(player, gen), idleTicks);
    }

    private void clearAfterIdle(Player player, int generation) {
        if (!player.isOnline()) {
            states.remove(player.getUniqueId());
            return;
        }
        BarState state = states.get(player.getUniqueId());
        if (state == null || state.clearGeneration != generation) {
            return;
        }
        player.sendActionBar(Component.empty());
        states.remove(player.getUniqueId());
    }

    private void render(Player player, BarState state) {
        Component line = state.staticLine != null
                ? state.staticLine
                : buildBurstLine(state);
        player.sendActionBar(line);
    }

    private Component buildBurstLine(BarState state) {
        Component line = Component.empty();
        boolean hasBurst = false;

        if (state.capturedBurst > 0) {
            String key = state.capturedBurst == 1 ? "captured_one" : "captured_many";
            line = line.append(messages.get(key, Map.of("amount", String.valueOf(state.capturedBurst))));
            hasBurst = true;
        }
        if (state.releasedBurst > 0) {
            if (hasBurst) {
                line = line.append(Component.text(" · ", NamedTextColor.DARK_GRAY));
            }
            String key = state.releasedBurst == 1 ? "released_one" : "released_many";
            line = line.append(messages.get(key, Map.of("amount", String.valueOf(state.releasedBurst))));
            hasBurst = true;
        }

        if (!hasBurst) {
            line = Component.text("…", NamedTextColor.DARK_GRAY);
        }
        return line;
    }

    private void cancelClearTask(BarState state) {
        if (state.clearTask != null) {
            state.clearTask.cancel();
            state.clearTask = null;
        }
    }

    private static final class BarState {
        int capturedBurst;
        int releasedBurst;
        int total;
        int capacity;
        Component staticLine;
        int clearGeneration;
        BukkitTask clearTask;
    }
}
