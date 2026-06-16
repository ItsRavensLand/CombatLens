package io.github.ItsRavensLand.combatLens;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class CombatManager {

    private static CombatManager instance;

    private final Map<UUID, CombatSession> activeSessions = new HashMap<>();
    private final Map<UUID, List<CombatSession>> combatHistory = new HashMap<>();

    public static CombatManager getInstance() {
        if (instance == null) instance = new CombatManager();
        return instance;
    }

    public void startCombat(Player player, Player opponent) {
        if (isInCombat(player) || isInCombat(opponent)) return;

        CombatSession playerSession = new CombatSession(player, opponent);
        CombatSession opponentSession = new CombatSession(opponent, player);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            String effectName = formatEffect(effect);
            playerSession.addPlayerEffect(effectName);
            opponentSession.addOpponentEffect(effectName);
        }
        for (PotionEffect effect : opponent.getActivePotionEffects()) {
            String effectName = formatEffect(effect);
            opponentSession.addPlayerEffect(effectName);
            playerSession.addOpponentEffect(effectName);
        }

        activeSessions.put(player.getUniqueId(), playerSession);
        activeSessions.put(opponent.getUniqueId(), opponentSession);

        CombatTagManager.getInstance().registerHit(player.getUniqueId());
        CombatTagManager.getInstance().registerHit(opponent.getUniqueId());

        CombatLens.getInstance().getLogger().info(
                player.getName() + " vs " + opponent.getName() + " - Combat started!"
        );
    }

    public void registerHit(Player attacker, Player victim, int damage, boolean isCritical) {
        CombatSession attackerSession = activeSessions.get(attacker.getUniqueId());
        CombatSession victimSession = activeSessions.get(victim.getUniqueId());

        if (attackerSession != null) attackerSession.addPlayerHit(damage, isCritical);
        if (victimSession != null) victimSession.addOpponentHit(damage, isCritical);

        CombatTagManager.getInstance().registerHit(attacker.getUniqueId());
        CombatTagManager.getInstance().registerHit(victim.getUniqueId());
    }

    public void endCombat(Player player, Player opponent,
                          CombatSession.WinType playerWinType,
                          CombatSession.WinType opponentWinType) {

        CombatSession playerSession = activeSessions.remove(player.getUniqueId());
        CombatSession opponentSession = activeSessions.remove(opponent.getUniqueId());

        CombatTagManager.getInstance().clearTag(player.getUniqueId());
        CombatTagManager.getInstance().clearTag(opponent.getUniqueId());

        if (playerSession != null) {
            playerSession.setPlayerEndHp((int) player.getHealth());
            playerSession.setOpponentEndHp((int) opponent.getHealth());
            playerSession.calculateUsedItems(player, opponent);
            playerSession.finish(playerWinType);
            saveToHistory(player.getUniqueId(), playerSession);
        }

        if (opponentSession != null) {
            opponentSession.setPlayerEndHp((int) opponent.getHealth());
            opponentSession.setOpponentEndHp((int) player.getHealth());
            opponentSession.calculateUsedItems(opponent, player);
            opponentSession.finish(opponentWinType);
            saveToHistory(opponent.getUniqueId(), opponentSession);
        }

        String outMsg = ConfigManager.getInstance().getOutOfCombatMessage();
        player.sendActionBar(
                Component.text(outMsg, NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        );
        if (opponent.isOnline()) {
            opponent.sendActionBar(
                    Component.text(outMsg, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
            );
        }
    }

    public void endCombatSingle(Player player, CombatSession.WinType winType) {
        CombatSession session = activeSessions.remove(player.getUniqueId());
        CombatTagManager.getInstance().clearTag(player.getUniqueId());

        if (session != null) {
            session.setPlayerEndHp((int) player.getHealth());
            session.finish(winType);
            saveToHistory(player.getUniqueId(), session);
        }

        player.sendActionBar(
                Component.text(ConfigManager.getInstance().getOutOfCombatMessage(), NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        );
    }

    public void handleDisconnect(Player player) {
        CombatSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        Player opponent = CombatLens.getInstance().getServer()
                .getPlayer(session.getOpponentUUID());

        if (opponent != null && opponent.isOnline()) {
            endCombat(opponent, player,
                    CombatSession.WinType.DISCONNECT,
                    CombatSession.WinType.LOGOUT);
        } else {
            activeSessions.remove(player.getUniqueId());
            CombatTagManager.getInstance().clearTag(player.getUniqueId());
        }
    }

    private void saveToHistory(UUID uuid, CombatSession session) {
        if (session.getDurationSeconds() < ConfigManager.getInstance().getMinFightDuration()) return;

        DatabaseManager.getInstance().saveSession(session);
        combatHistory.computeIfAbsent(uuid, k -> new ArrayList<>()).add(0, session);
        List<CombatSession> history = combatHistory.get(uuid);
        if (history.size() > ConfigManager.getInstance().getMaxHistory()) {
            history.remove(history.size() - 1);
        }
    }

    public List<CombatSession> getHistory(UUID uuid) {
        if (combatHistory.containsKey(uuid)) return combatHistory.get(uuid);
        List<CombatSession> history = DatabaseManager.getInstance().loadHistory(uuid);
        combatHistory.put(uuid, history);
        return history;
    }

    public boolean isInCombat(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public CombatSession getActiveSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    private String formatEffect(PotionEffect effect) {
        String name = effect.getType().key().value().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        int level = effect.getAmplifier() + 1;
        return result.toString().trim() + " " + toRoman(level);
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }
}