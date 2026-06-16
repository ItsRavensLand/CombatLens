package io.github.ItsRavensLand.combatLens;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTagManager {

    private static CombatTagManager instance;

    private final Map<UUID, Long> lastHitTime = new HashMap<>();

    private static final int TIMEOUT_SECONDS = 15;

    public static CombatTagManager getInstance() {
        if (instance == null) instance = new CombatTagManager();
        return instance;
    }

    public void startTagTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                CombatLens.getInstance().getLogger().info("Tag task tick running");
                for (Player player : CombatLens.getInstance().getServer().getOnlinePlayers()) {
                    if (!CombatManager.getInstance().isInCombat(player)) continue;

                    CombatSession session = CombatManager.getInstance().getActiveSession(player);
                    if (session == null) continue;

                    if (!lastHitTime.containsKey(player.getUniqueId())) {
                        showActionBar(player, session, TIMEOUT_SECONDS);
                        continue;
                    }

                    long lastHit = lastHitTime.get(player.getUniqueId());
                    long secondsSinceHit = (System.currentTimeMillis() - lastHit) / 1000;
                    long remaining = TIMEOUT_SECONDS - secondsSinceHit;

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

        player.sendActionBar(
                Component.text("In Combat with ", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(session.getOpponentName(), NamedTextColor.WHITE)
                                .decoration(TextDecoration.BOLD, true))
                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.BOLD, false))
                        .append(Component.text(timer, remaining <= 5
                                        ? NamedTextColor.RED
                                        : NamedTextColor.YELLOW)
                                .decoration(TextDecoration.BOLD, false))
        );
    }

    private void handleTimeout(Player player) {
        CombatSession session = CombatManager.getInstance().getActiveSession(player);
        if (session == null) return;

        Player opponent = CombatLens.getInstance().getServer()
                .getPlayer(session.getOpponentUUID());

        clearTag(player.getUniqueId());

        if (opponent != null && opponent.isOnline()) {
            clearTag(opponent.getUniqueId());
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
