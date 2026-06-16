package io.github.ItsRavensLand.combatLens.gui;

import io.github.ItsRavensLand.combatLens.CombatManager;
import io.github.ItsRavensLand.combatLens.CombatSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());


        if (!title.equals("Your Fight History") && !title.startsWith("Fight vs")) return;


        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        // --------- History GUI ---------
        if (title.equals("Your Fight History")) {
            handleHistoryClick(player, clicked, event.getSlot());
        }

        // --------- Detail GUI ---------
        if (title.startsWith("Fight vs")) {
            handleDetailClick(player, clicked, title);
        }
    }

    private void handleHistoryClick(Player player, ItemStack clicked, int slot) {
        List<CombatSession> history = CombatManager.getInstance()
                .getHistory(player.getUniqueId());


        int index = slotToIndex(slot);
        if (index < 0 || index >= history.size()) return;

        CombatSession session = history.get(index);
        CombatDetailGUI.open(player, session);
    }

    private void handleDetailClick(Player player, ItemStack clicked, String title) {

        if (clicked.getType() == org.bukkit.Material.ARROW) {
            CombatHistoryGUI.open(player);
        }
    }


    private int slotToIndex(int slot) {

        int[] validSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < validSlots.length; i++) {
            if (validSlots[i] == slot) return i;
        }
        return -1;
    }
}