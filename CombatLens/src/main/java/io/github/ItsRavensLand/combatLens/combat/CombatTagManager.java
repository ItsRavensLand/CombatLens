package io.github.ItsRavensLand.combatLens.combat;

import io.github.ItsRavensLand.combatLens.CombatLens;
import io.github.ItsRavensLand.combatLens.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// drives the action bar timer and combat timeouts
public class CombatTagManager {

    private static CombatTagManager instance;

    private final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();

    public static CombatTagManager getInstance() {
        if (instance == null) instance = new CombatTagManager();
        return instance;
    }

    // ticks every second, checks every online player in combat
    public void startTagTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!ConfigManager.getInstance().isCombatTagEnabled()) return;

                for (Player player : CombatLens.getInstance().getServer().getOnlinePlayers()) {
                    if (!CombatManager.getInstance().isInCombat(player)) continue;

                    CombatSession session = CombatManager.getInstance().getActiveSession(player);
                    if (session == null) continue;

                    if (!lastHitTime.containsKey(player.getUniqueId())) {
                        showActionBar(player, session, ConfigManager.getInstance().getTimeout());
                        continue;
                    }

                    long lastHit = lastHitTime.get(player.getUniqueId());
                    long secondsSinceHit = (System.currentTimeMillis() - lastHit) / 1000;
                    long remaining = ConfigManager.getInstance().getTimeout() - secondsSinceHit;

                    if (remaining <= 0) {
                        handleTimeout(player);
                        continue;
                    }

                    showActionBar(player, session, remaining);
                }
            }
        }.runTaskTimer(CombatLens.getInstance(), 0L, 20L);
    }

    public void registerHit(UUID playerUUID) {
        lastHitTime.put(playerUUID, System.currentTimeMillis());
    }

    public void clearTag(UUID playerUUID) {
        lastHitTime.remove(playerUUID);
    }

    private void showActionBar(Player player, CombatSession session, long remaining) {
        long mins = remaining / 60;
        long secs = remaining % 60;
        String timer = mins > 0
                ? mins + ":" + String.format("%02d", secs)
                : secs + "s";

        String template = ConfigManager.getInstance().getInCombatMessage();
        String[] parts = template.split("\\{player\\}", 2);

        Component message = Component.text(parts[0], NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false);

        message = message.append(
                Component.text(session.getOpponentName(), NamedTextColor.WHITE)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false)
        );

        if (parts.length > 1) {
            message = message.append(
                    Component.text(parts[1], NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false)
            );
        }

        player.sendActionBar(
                message
                        .append(Component.text("  |  ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(timer, remaining <= 5
                                        ? NamedTextColor.RED
                                        : NamedTextColor.YELLOW)
                                .decoration(TextDecoration.BOLD, false))
        );
    }

    // ends the fight on timeout, guarded so both players timing out
    // in the same tick can't double-trigger endCombat
    private void handleTimeout(Player player) {
        if (!CombatManager.getInstance().isInCombat(player)) return;

        CombatSession session = CombatManager.getInstance().getActiveSession(player);
        if (session == null) return;

        Player opponent = CombatLens.getInstance().getServer()
            .getPlayer(session.getOpponentUUID());

        if (opponent != null && opponent.isOnline()) {
            // re-check here too, opponent's own tick may have already ended it
            if (!CombatManager.getInstance().isInCombat(opponent)) {
                clearTag(player.getUniqueId());
                return;
            }
            CombatManager.getInstance().endCombat(
                player, opponent,
                CombatSession.WinType.TIMEOUT,
                CombatSession.WinType.TIMEOUT
            );
        } else {
            CombatManager.getInstance().endCombatSingle(player, CombatSession.WinType.TIMEOUT);
        }
    }
}
