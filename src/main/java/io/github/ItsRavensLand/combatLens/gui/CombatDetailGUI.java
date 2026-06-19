package io.github.ItsRavensLand.combatLens.gui;

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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// full stat breakdown for a single fight
public class CombatDetailGUI {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm:ss");

    public static void open(Player player, CombatSession session) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text("Fight vs " + session.getOpponentName(), NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));

        fillBorder(inv);

        inv.setItem(20, buildCombatStatsItem(session, true));
        inv.setItem(22, buildSummaryItem(session));
        inv.setItem(24, buildCombatStatsItem(session, false));

        inv.setItem(29, buildConsumablesItem(session, true));
        inv.setItem(31, buildProjectilesItem(session));
        inv.setItem(33, buildConsumablesItem(session, false));

        inv.setItem(38, buildGearItem(session, true));
        inv.setItem(40, buildTimeItem(session));
        inv.setItem(42, buildGearItem(session, false));

        inv.setItem(49, buildBackButton());

        player.openInventory(inv);
    }

    private static ItemStack buildCombatStatsItem(CombatSession session, boolean isPlayer) {
        ItemStack item = new ItemStack(isPlayer ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        String name = isPlayer ? session.getPlayerName() : session.getOpponentName();
        NamedTextColor color = isPlayer ? NamedTextColor.GREEN : NamedTextColor.RED;

        meta.displayName(
            Component.text(name, color)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("'s Combat", NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false))
        );

        int hits = isPlayer ? session.getPlayerHitsDealt() : session.getOpponentHitsDealt();
        int missed = isPlayer ? session.getPlayerMissedHits() : session.getOpponentMissedHits();
        int crits = isPlayer ? session.getPlayerCriticalHits() : session.getOpponentCriticalHits();
        int damage = isPlayer ? session.getPlayerDamageDealt() : session.getOpponentDamageDealt();
        int bestHit = isPlayer ? session.getPlayerBestHit() : session.getOpponentBestHit();
        double avgDmg = isPlayer ? session.getAverageDamagePerHit() : session.getOpponentAverageDamagePerHit();
        int combo = isPlayer ? session.getPlayerMaxCombo() : session.getOpponentMaxCombo();
        int healed = isPlayer ? session.getPlayerHealedAmount() : session.getOpponentHealedAmount();
        int startHp = isPlayer ? session.getPlayerStartHp() : session.getOpponentStartHp();
        int endHp = isPlayer ? session.getPlayerEndHp() : session.getOpponentEndHp();
        int shields = isPlayer ? session.getPlayerShieldBlocks() : session.getOpponentShieldBlocks();
        boolean shieldBroken = isPlayer ? session.isPlayerShieldBroken() : session.isOpponentShieldBroken();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        lore.add(Component.text("Health", NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Started at ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(startHp + " HP", NamedTextColor.WHITE))
            .append(Component.text(" ended at ", NamedTextColor.GRAY))
            .append(Component.text(endHp + " HP", NamedTextColor.WHITE)));
        lore.add(Component.text(" Healed ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(healed + " HP", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)));

        lore.add(Component.empty());

        lore.add(Component.text("Combat", NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Landed ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(hits + " hits", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true))
            .append(Component.text(" and missed ", NamedTextColor.GRAY))
            .append(Component.text(missed, NamedTextColor.WHITE)));
        lore.add(Component.text(" Critical hits ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(crits, NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Best hit ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(bestHit + " dmg", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Total damage ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(damage + " dmg", NamedTextColor.GOLD)));
        lore.add(Component.text(" Avg per hit ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(avgDmg + " dmg", NamedTextColor.YELLOW)));
        lore.add(Component.text(" Longest combo ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(combo + "x", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)));

        lore.add(Component.empty());

        lore.add(Component.text("Shield", NamedTextColor.AQUA)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Blocked ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(shields + " times", NamedTextColor.AQUA)));
        lore.add(Component.text(" Shield broken ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(shieldBroken
                ? Component.text("Yes", NamedTextColor.RED).decoration(TextDecoration.BOLD, true)
                : Component.text("No", NamedTextColor.GREEN)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildConsumablesItem(CombatSession session, boolean isPlayer) {
        ItemStack item = new ItemStack(isPlayer ? Material.GOLDEN_APPLE : Material.APPLE);
        ItemMeta meta = item.getItemMeta();

        String name = isPlayer ? session.getPlayerName() : session.getOpponentName();
        NamedTextColor color = isPlayer ? NamedTextColor.GREEN : NamedTextColor.RED;

        meta.displayName(
            Component.text(name, color)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("'s Consumables", NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false))
        );

        Set<String> effects = isPlayer ? session.getPlayerEffects() : session.getOpponentEffects();
        int gapples = isPlayer ? session.getPlayerGapplesUsed() : session.getOpponentGapplesUsed();
        int notch = isPlayer ? session.getPlayerNotchApplesUsed() : session.getOpponentNotchApplesUsed();
        int totems = isPlayer ? session.getPlayerTotemsPopped() : session.getOpponentTotemsPopped();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        lore.add(Component.text("Food & Totems", NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Golden apples ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(gapples, NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Enchanted apples ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(notch, NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Totems used ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(totems, NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)));

        lore.add(Component.empty());

        lore.add(Component.text("Active Effects", NamedTextColor.LIGHT_PURPLE)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        if (effects.isEmpty()) {
            lore.add(Component.text(" No effects", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        } else {
            for (String effect : effects) {
                lore.add(Component.text(" " + effect, NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            }
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildProjectilesItem(CombatSession session) {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
            Component.text("Ranged Combat", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );

        String playerName = session.getPlayerName();
        String opponentName = session.getOpponentName();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        lore.add(Component.text(playerName, NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Arrows shot ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getPlayerArrowsShot(), NamedTextColor.YELLOW)));
        lore.add(Component.text(" Arrows landed ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getPlayerArrowsHit(), NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Pearls thrown ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getPlayerPearlsThrown(), NamedTextColor.DARK_AQUA)));

        lore.add(Component.empty());

        lore.add(Component.text(opponentName, NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Arrows shot ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getOpponentArrowsShot(), NamedTextColor.YELLOW)));
        lore.add(Component.text(" Arrows landed ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getOpponentArrowsHit(), NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Pearls thrown ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getOpponentPearlsThrown(), NamedTextColor.DARK_AQUA)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildGearItem(CombatSession session, boolean isPlayer) {
        ItemStack item = new ItemStack(isPlayer ? Material.DIAMOND_SWORD : Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();

        String name = isPlayer ? session.getPlayerName() : session.getOpponentName();
        NamedTextColor color = isPlayer ? NamedTextColor.GREEN : NamedTextColor.RED;

        meta.displayName(
            Component.text(name, color)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("'s Gear", NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false))
        );

        String weapon = isPlayer ? session.getPlayerWeapon() : session.getOpponentWeapon();
        int sharpness = isPlayer ? session.getPlayerHighestSharpness() : session.getOpponentHighestSharpness();
        int protection = isPlayer ? session.getPlayerHighestProtection() : session.getOpponentHighestProtection();
        int xp = isPlayer ? session.getPlayerXpLevel() : session.getOpponentXpLevel();
        float hunger = isPlayer ? session.getPlayerHungerOnStart() : session.getOpponentHungerOnStart();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        lore.add(Component.text("Weapon", NamedTextColor.AQUA)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" " + weapon, NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Sharpness ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(sharpness > 0
                ? Component.text("Level " + sharpness, NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true)
                : Component.text("None", NamedTextColor.DARK_GRAY)));

        lore.add(Component.empty());

        lore.add(Component.text("Armor", NamedTextColor.BLUE)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" Protection ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(protection > 0
                ? Component.text("Level " + protection, NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true)
                : Component.text("None", NamedTextColor.DARK_GRAY)));

        lore.add(Component.empty());

        lore.add(Component.text("Status at Fight Start", NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" XP level ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(xp, NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text(" Hunger ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text((int) hunger + " / 20", NamedTextColor.GOLD)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildSummaryItem(CombatSession session) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        boolean won = session.getWinType() == CombatSession.WinType.KILL ||
                      session.getWinType() == CombatSession.WinType.DISCONNECT;

        meta.displayName(
            Component.text("Fight Summary", NamedTextColor.WHITE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Result ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(won
                ? Component.text("Victory", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                : Component.text("Defeat", NamedTextColor.RED).decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text("Ended by ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(CombatHistoryGUI.formatWinType(session.getWinType()), NamedTextColor.YELLOW)));
        lore.add(Component.text("Against ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getOpponentName(), NamedTextColor.WHITE)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.text("In ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getFightWorld(), NamedTextColor.WHITE)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildTimeItem(CombatSession session) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
            Component.text("Fight Timeline", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Started ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(session.getStartTime().format(FORMATTER), NamedTextColor.WHITE)));
        lore.add(Component.text("Ended ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(
                session.getEndTime() != null ? session.getEndTime().format(FORMATTER) : "Still ongoing",
                NamedTextColor.WHITE)));
        lore.add(Component.empty());
        lore.add(Component.text("Total duration ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(CombatHistoryGUI.formatDuration(session.getDurationSeconds()), NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(
            Component.text("Back to History", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );
        item.setItemMeta(meta);
        return item;
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
}
