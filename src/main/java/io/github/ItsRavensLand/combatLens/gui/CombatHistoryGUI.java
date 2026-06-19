package io.github.ItsRavensLand.combatLens.gui;

import io.github.ItsRavensLand.combatLens.combat.CombatManager;
import io.github.ItsRavensLand.combatLens.combat.CombatSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// minimal list view, full detail lives in CombatDetailGUI
public class CombatHistoryGUI {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm");

    public static void open(Player player) {
        List<CombatSession> history = CombatManager.getInstance()
            .getHistory(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text("Your Fight History", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));

        fillBorder(inv);

        if (history.isEmpty()) {
            ItemStack noFights = new ItemStack(Material.BARRIER);
            ItemMeta meta = noFights.getItemMeta();
            meta.displayName(Component.text("No fights recorded yet.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, true));
            noFights.setItemMeta(meta);
            inv.setItem(22, noFights);
        } else {
            int slot = 10;
            for (CombatSession session : history) {
                if (slot > 43) break;
                inv.setItem(slot, buildSessionItem(session));
                slot++;
                if (slot == 17 || slot == 26 || slot == 35) slot += 2;
            }
        }

        player.openInventory(inv);
    }

    private static ItemStack buildSessionItem(CombatSession session) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        boolean won = session.getWinType() == CombatSession.WinType.KILL ||
                      session.getWinType() == CombatSession.WinType.DISCONNECT;

        NamedTextColor titleColor = won ? NamedTextColor.GREEN : NamedTextColor.RED;
        String resultText = won ? "Victory" : "Defeat";

        meta.displayName(
            Component.text(resultText + " against ", titleColor)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(session.getOpponentName(), NamedTextColor.WHITE)
                    .decoration(TextDecoration.BOLD, true))
        );

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(
            Component.text("How it ended  ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(formatWinType(session.getWinType()), NamedTextColor.YELLOW)
                    .decoration(TextDecoration.BOLD, false))
        );
        lore.add(
            Component.text("Weapon used  ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(session.getPlayerWeapon(), NamedTextColor.AQUA))
        );
        lore.add(
            Component.text("World  ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(session.getFightWorld(), NamedTextColor.WHITE))
        );
        lore.add(Component.empty());
        lore.add(
            Component.text("Duration  ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(formatDuration(session.getDurationSeconds()), NamedTextColor.GOLD))
        );
        lore.add(
            Component.text("Date  ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(session.getStartTime().format(FORMATTER), NamedTextColor.WHITE))
        );
        lore.add(Component.empty());
        lore.add(
            Component.text("Click to see full breakdown", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true)
        );

        meta.lore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private static void fillBorder(Inventory inv) {
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(Component.text(" "));
        border.setItemMeta(meta);

        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int i = 9; i < 45; i += 9) inv.setItem(i, border);
        for (int i = 17; i < 54; i += 9) inv.setItem(i, border);
    }

    static String formatWinType(CombatSession.WinType type) {
        return switch (type) {
            case KILL -> "Killed the opponent";
            case KILLED -> "Got killed";
            case DISCONNECT -> "Opponent disconnected";
            case LOGOUT -> "Left during combat";
            case TIMEOUT -> "Fight timed out";
        };
    }

    static String formatDuration(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        if (mins == 0) return secs + " seconds";
        return mins + "m " + secs + "s";
    }
}
