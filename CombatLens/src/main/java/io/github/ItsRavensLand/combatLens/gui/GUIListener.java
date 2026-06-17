package io.github.ItsRavensLand.combatLens.gui;

import io.github.ItsRavensLand.combatLens.combat.CombatManager;
import io.github.ItsRavensLand.combatLens.combat.CombatSession;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

// routes clicks inside CombatLens GUIs to the right handler
public class GUIListener implements Listener {

    // slot positions in CombatHistoryGUI that hold fight entries, in order
    private static final int[] HISTORY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText()
            .serialize(event.getView().title());

        if (!title.equals("Your Fight History") && !title.startsWith("Fight vs")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (title.equals("Your Fight History")) {
            handleHistoryClick(player, event.getSlot());
        }

        if (title.startsWith("Fight vs")) {
            handleDetailClick(player, clicked);
        }
    }

    private void handleHistoryClick(Player player, int slot) {
        List<CombatSession> history = CombatManager.getInstance()
            .getHistory(player.getUniqueId());

        int index = slotToIndex(slot);
        if (index < 0 || index >= history.size()) return;

        CombatSession session = history.get(index);
        CombatDetailGUI.open(player, session);
    }

    private void handleDetailClick(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.ARROW) {
            CombatHistoryGUI.open(player);
        }
    }

    private int slotToIndex(int slot) {
        for (int i = 0; i < HISTORY_SLOTS.length; i++) {
            if (HISTORY_SLOTS[i] == slot) return i;
        }
        return -1;
    }
}
