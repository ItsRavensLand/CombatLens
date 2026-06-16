package io.github.ItsRavensLand.combatLens;

import io.github.ItsRavensLand.combatLens.gui.CombatHistoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class CombatLensCommand {

    public void sendHelpPublic(Player player) {
        player.sendMessage(Component.text("═══════════════════", NamedTextColor.DARK_RED));
        player.sendMessage(Component.text("  CombatLens Help", NamedTextColor.RED));
        player.sendMessage(Component.text("═══════════════════", NamedTextColor.DARK_RED));
        player.sendMessage(Component.text("/combatlens ", NamedTextColor.YELLOW)
                .append(Component.text("- Open combat history GUI", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/combatlens stats ", NamedTextColor.YELLOW)
                .append(Component.text("- View your combat stats", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/combatlens help ", NamedTextColor.YELLOW)
                .append(Component.text("- Show this help menu", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("═══════════════════", NamedTextColor.DARK_RED));
    }

    public void sendStatsPublic(Player player) {
        List<CombatSession> history = CombatManager.getInstance()
                .getHistory(player.getUniqueId());

        if (history.isEmpty()) {
            player.sendMessage(Component.text(
                    "You have no recorded fights yet!", NamedTextColor.RED
            ));
            return;
        }

        int wins = 0, losses = 0, totalDamage = 0, totalHits = 0;

        for (CombatSession session : history) {
            if (session.getWinType() == CombatSession.WinType.KILL ||
                    session.getWinType() == CombatSession.WinType.DISCONNECT) {
                wins++;
            } else {
                losses++;
            }
            totalDamage += session.getPlayerDamageDealt();
            totalHits += session.getPlayerHitsDealt();
        }

        player.sendMessage(Component.text("═══════════════════", NamedTextColor.DARK_RED));
        player.sendMessage(Component.text("  Your Combat Stats", NamedTextColor.RED));
        player.sendMessage(Component.text("═══════════════════", NamedTextColor.DARK_RED));
        player.sendMessage(Component.text("Fights: ", NamedTextColor.GRAY)
                .append(Component.text(history.size(), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Wins: ", NamedTextColor.GRAY)
                .append(Component.text(wins, NamedTextColor.GREEN)));
        player.sendMessage(Component.text("Losses: ", NamedTextColor.GRAY)
                .append(Component.text(losses, NamedTextColor.RED)));
        player.sendMessage(Component.text("W/L Ratio: ", NamedTextColor.GRAY)
                .append(Component.text(
                        losses == 0 ? "Perfect" : String.format("%.2f", (float) wins / losses),
                        NamedTextColor.GOLD
                )));
        player.sendMessage(Component.text("Total Damage Dealt: ", NamedTextColor.GRAY)
                .append(Component.text(totalDamage, NamedTextColor.GOLD)));
        player.sendMessage(Component.text("Total Hits: ", NamedTextColor.GRAY)
                .append(Component.text(totalHits, NamedTextColor.WHITE)));
        player.sendMessage(Component.text("═══════════════════", NamedTextColor.DARK_RED));
    }
}