package io.github.ItsRavensLand.combatLens.listener;

import io.github.ItsRavensLand.combatLens.CombatLens;
import io.github.ItsRavensLand.combatLens.combat.CombatManager;
import io.github.ItsRavensLand.combatLens.combat.CombatSession;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;

// listens to all combat-related game events and feeds the manager
public class CombatListener implements Listener {

    private final CombatManager combatManager = CombatManager.getInstance();

    // main hit detection, also catches arrows fired by players
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Arrow arrow &&
                   arrow.getShooter() instanceof Player p) {
            attacker = p;
            if (combatManager.isInCombat(p)) {
                CombatSession session = combatManager.getActiveSession(p);
                if (session != null) {
                    if (session.getPlayerUUID().equals(p.getUniqueId())) {
                        session.addPlayerArrowHit();
                    } else {
                        session.addOpponentArrowHit();
                    }
                }
            }
        }

        if (attacker == null) return;

        int damage = (int) event.getFinalDamage();
        boolean isCritical = isCriticalHit(attacker);

        if (!combatManager.isInCombat(attacker) && !combatManager.isInCombat(victim)) {
            combatManager.startCombat(attacker, victim);
        }

        combatManager.registerHit(attacker, victim, damage, isCritical);
    }

    // counts a miss only if the player is actually holding a weapon,
    // avoids false misses from clicking with an empty hand
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR) return;

        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        Material inHand = player.getInventory().getItemInMainHand().getType();
        if (inHand == Material.AIR) return;

        CombatSession session = combatManager.getActiveSession(player);
        if (session == null) return;

        if (session.getPlayerUUID().equals(player.getUniqueId())) {
            session.addPlayerMiss();
        } else {
            session.addOpponentMiss();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!combatManager.isInCombat(player)) return;

        CombatSession session = combatManager.getActiveSession(player);
        if (session == null) return;

        int amount = (int) event.getAmount();
        if (session.getPlayerUUID().equals(player.getUniqueId())) {
            session.addPlayerHeal(amount);
        } else {
            session.addOpponentHeal(amount);
        }
    }

    // golden apple and notch apple usage, counted directly off the consume event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        CombatSession session = combatManager.getActiveSession(player);
        if (session == null) return;

        boolean isPlayer = session.getPlayerUUID().equals(player.getUniqueId());
        Material type = event.getItem().getType();

        if (type == Material.GOLDEN_APPLE) {
            if (isPlayer) session.addPlayerGapple();
            else session.addOpponentGapple();
        } else if (type == Material.ENCHANTED_GOLDEN_APPLE) {
            if (isPlayer) session.addPlayerNotchApple();
            else session.addOpponentNotchApple();
        }
    }

    // records any effect gained during the fight, on both sessions
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEffectApplied(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED &&
            event.getAction() != EntityPotionEffectEvent.Action.CHANGED) return;
        if (!combatManager.isInCombat(player)) return;

        PotionEffect effect = event.getNewEffect();
        if (effect == null) return;

        String effectName = formatEffect(effect);
        CombatSession session = combatManager.getActiveSession(player);
        if (session == null) return;

        session.addPlayerEffect(effectName);

        Player opponent = CombatLens.getInstance().getServer()
            .getPlayer(session.getOpponentUUID());
        if (opponent != null && opponent.isOnline()) {
            CombatSession opponentSession = combatManager.getActiveSession(opponent);
            if (opponentSession != null) {
                opponentSession.addOpponentEffect(effectName);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!combatManager.isInCombat(player)) return;

        CombatSession session = combatManager.getActiveSession(player);
        if (session == null) return;

        if (session.getPlayerUUID().equals(player.getUniqueId())) {
            session.addPlayerArrowShot();
        } else {
            session.addOpponentArrowShot();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPearlThrow(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        CombatSession session = combatManager.getActiveSession(player);
        if (session == null) return;

        if (session.getPlayerUUID().equals(player.getUniqueId())) {
            session.addPlayerPearl();
        } else {
            session.addOpponentPearl();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!combatManager.isInCombat(victim)) return;
        if (!victim.isBlocking()) return;
        if (event.getDamage() > 0) return;

        CombatSession session = combatManager.getActiveSession(victim);
        if (session == null) return;

        if (session.getPlayerUUID().equals(victim.getUniqueId())) {
            session.addPlayerShieldBlock();
        } else {
            session.addOpponentShieldBlock();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!combatManager.isInCombat(victim)) return;

        CombatSession session = combatManager.getActiveSession(victim);
        if (session == null) return;

        Player killer = victim.getKiller();

        if (killer != null && killer.isOnline()) {
            combatManager.endCombat(killer, victim,
                CombatSession.WinType.KILL,
                CombatSession.WinType.KILLED);
        } else {
            Player opponent = CombatLens.getInstance()
                .getServer().getPlayer(session.getOpponentUUID());
            if (opponent != null && opponent.isOnline()) {
                combatManager.endCombat(opponent, victim,
                    CombatSession.WinType.KILL,
                    CombatSession.WinType.KILLED);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;
        combatManager.handleDisconnect(player);
    }

    // crit if airborne and not in a vehicle, matches vanilla crit conditions closely enough
    private boolean isCriticalHit(Player player) {
        return !player.isOnGround()
            && player.getFallDistance() > 0
            && !player.isInsideVehicle()
            && !player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
    }

    // turns EFFECT_NAME into "Effect Name II"
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
