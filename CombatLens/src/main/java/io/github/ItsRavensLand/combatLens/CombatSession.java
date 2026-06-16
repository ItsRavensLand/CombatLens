package io.github.ItsRavensLand.combatLens;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class CombatSession {

    public enum WinType {
        KILL,
        KILLED,
        DISCONNECT,
        LOGOUT,
        TIMEOUT
    }

    private final UUID playerUUID;
    private final String playerName;
    private final UUID opponentUUID;
    private final String opponentName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // HP
    private int playerStartHp;
    private int playerEndHp;
    private int opponentStartHp;
    private int opponentEndHp;

    // Hits
    private int playerHitsDealt;
    private int opponentHitsDealt;
    private int playerMissedHits;
    private int opponentMissedHits;
    private int playerCriticalHits;
    private int opponentCriticalHits;

    // Damage
    private int playerDamageDealt;
    private int opponentDamageDealt;
    private int playerBestHit;
    private int opponentBestHit;

    // Combo
    private int playerMaxCombo;
    private int opponentMaxCombo;
    private int playerCurrentCombo;
    private int opponentCurrentCombo;

    // Healing
    private int playerHealedAmount;
    private int opponentHealedAmount;

    // Effects
    private final Set<String> playerEffects = new LinkedHashSet<>();
    private final Set<String> opponentEffects = new LinkedHashSet<>();

    // Consumables
    private int playerGapplesUsed;
    private int opponentGapplesUsed;
    private int playerNotchApplesUsed;
    private int opponentNotchApplesUsed;
    private int playerTotemsPopped;
    private int opponentTotemsPopped;

    // Totem snapshot
    private int playerStartTotems;
    private int opponentStartTotems;

    // Arrows
    private int playerArrowsShot;
    private int opponentArrowsShot;
    private int playerArrowsHit;
    private int opponentArrowsHit;

    // Pearls
    private int playerPearlsThrown;
    private int opponentPearlsThrown;

    // Shield
    private int playerShieldBlocks;
    private int opponentShieldBlocks;
    private boolean playerShieldBroken;
    private boolean opponentShieldBroken;

    // Enchants
    private int playerHighestSharpness;
    private int opponentHighestSharpness;
    private int playerHighestProtection;
    private int opponentHighestProtection;

    // Misc
    private int playerXpLevel;
    private int opponentXpLevel;
    private float playerHungerOnStart;
    private float opponentHungerOnStart;
    private String playerWeapon;
    private String opponentWeapon;
    private String fightWorld;

    private WinType winType;
    private boolean finished;

    // live fight constructor
    public CombatSession(Player player, Player opponent) {
        this.playerUUID = player.getUniqueId();
        this.playerName = player.getName();
        this.opponentUUID = opponent.getUniqueId();
        this.opponentName = opponent.getName();
        this.startTime = LocalDateTime.now();
        this.playerStartHp = (int) player.getHealth();
        this.opponentStartHp = (int) opponent.getHealth();
        this.playerWeapon = getWeaponName(player);
        this.opponentWeapon = getWeaponName(opponent);
        this.playerXpLevel = player.getLevel();
        this.opponentXpLevel = opponent.getLevel();
        this.playerHungerOnStart = player.getFoodLevel();
        this.opponentHungerOnStart = opponent.getFoodLevel();
        this.fightWorld = player.getWorld().getName();
        this.playerHighestSharpness = getSharpnessLevel(player);
        this.opponentHighestSharpness = getSharpnessLevel(opponent);
        this.playerHighestProtection = getProtectionLevel(player);
        this.opponentHighestProtection = getProtectionLevel(opponent);
        this.playerStartTotems = countItem(player, Material.TOTEM_OF_UNDYING);
        this.opponentStartTotems = countItem(opponent, Material.TOTEM_OF_UNDYING);
        this.finished = false;
    }

    // database load constructor
    public CombatSession(UUID playerUUID, String playerName,
                         UUID opponentUUID, String opponentName,
                         int playerStartHp, int opponentStartHp) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.opponentUUID = opponentUUID;
        this.opponentName = opponentName;
        this.playerStartHp = playerStartHp;
        this.opponentStartHp = opponentStartHp;
        this.finished = true;
    }

    // only totems use snapshot now
    public void calculateUsedItems(Player player, Player opponent) {
        this.playerTotemsPopped = Math.max(0, playerStartTotems - countItem(player, Material.TOTEM_OF_UNDYING));
        this.opponentTotemsPopped = Math.max(0, opponentStartTotems - countItem(opponent, Material.TOTEM_OF_UNDYING));
    }

    private int countItem(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == material) count += item.getAmount();
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == material) count += offhand.getAmount();
        return count;
    }

    private String getWeaponName(Player player) {
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return "Fist";
        return formatMaterial(player.getInventory().getItemInMainHand().getType());
    }

    private int getSharpnessLevel(Player player) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) return 0;
        return weapon.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.SHARPNESS);
    }

    private int getProtectionLevel(Player player) {
        int maxProt = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType() == Material.AIR) continue;
            int prot = armor.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.PROTECTION);
            if (prot > maxProt) maxProt = prot;
        }
        return maxProt;
    }

    private String formatMaterial(Material material) {
        String name = material.name().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(word.charAt(0))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }
        return result.toString().trim();
    }

    // live finish
    public void finish(WinType winType) {
        this.winType = winType;
        this.endTime = LocalDateTime.now();
        this.finished = true;
    }

    // database finish
    public void finish(WinType winType, LocalDateTime startTime, LocalDateTime endTime) {
        this.winType = winType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.finished = true;
    }

    public long getDurationSeconds() {
        if (startTime == null || endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    public double getAverageDamagePerHit() {
        if (playerHitsDealt == 0) return 0;
        return Math.round((double) playerDamageDealt / playerHitsDealt * 10.0) / 10.0;
    }

    public double getOpponentAverageDamagePerHit() {
        if (opponentHitsDealt == 0) return 0;
        return Math.round((double) opponentDamageDealt / opponentHitsDealt * 10.0) / 10.0;
    }

    public void addPlayerHit(int damage, boolean isCritical) {
        this.playerHitsDealt++;
        this.playerDamageDealt += damage;
        if (damage > playerBestHit) this.playerBestHit = damage;
        if (isCritical) this.playerCriticalHits++;
        this.playerCurrentCombo++;
        this.opponentCurrentCombo = 0;
        if (playerCurrentCombo > playerMaxCombo) this.playerMaxCombo = playerCurrentCombo;
    }

    public void addOpponentHit(int damage, boolean isCritical) {
        this.opponentHitsDealt++;
        this.opponentDamageDealt += damage;
        if (damage > opponentBestHit) this.opponentBestHit = damage;
        if (isCritical) this.opponentCriticalHits++;
        this.opponentCurrentCombo++;
        this.playerCurrentCombo = 0;
        if (opponentCurrentCombo > opponentMaxCombo) this.opponentMaxCombo = opponentCurrentCombo;
    }

    public void addPlayerHit(int damage) { addPlayerHit(damage, false); }
    public void addOpponentHit(int damage) { addOpponentHit(damage, false); }
    public void addPlayerMiss() { this.playerMissedHits++; }
    public void addOpponentMiss() { this.opponentMissedHits++; }
    public void addPlayerHeal(int amount) { this.playerHealedAmount += amount; }
    public void addOpponentHeal(int amount) { this.opponentHealedAmount += amount; }
    public void addPlayerEffect(String effect) { this.playerEffects.add(effect); }
    public void addOpponentEffect(String effect) { this.opponentEffects.add(effect); }
    public void addPlayerGapple() { this.playerGapplesUsed++; }
    public void addOpponentGapple() { this.opponentGapplesUsed++; }
    public void addPlayerNotchApple() { this.playerNotchApplesUsed++; }
    public void addOpponentNotchApple() { this.opponentNotchApplesUsed++; }
    public void addPlayerArrowShot() { this.playerArrowsShot++; }
    public void addOpponentArrowShot() { this.opponentArrowsShot++; }
    public void addPlayerArrowHit() { this.playerArrowsHit++; }
    public void addOpponentArrowHit() { this.opponentArrowsHit++; }
    public void addPlayerPearl() { this.playerPearlsThrown++; }
    public void addOpponentPearl() { this.opponentPearlsThrown++; }
    public void addPlayerShieldBlock() { this.playerShieldBlocks++; }
    public void addOpponentShieldBlock() { this.opponentShieldBlocks++; }
    public void setPlayerShieldBroken() { this.playerShieldBroken = true; }
    public void setOpponentShieldBroken() { this.opponentShieldBroken = true; }

    // database overrides
    public void overridePlayerDamage(int v) { this.playerDamageDealt = v; }
    public void overrideOpponentDamage(int v) { this.opponentDamageDealt = v; }
    public void overridePlayerBestHit(int v) { this.playerBestHit = v; }
    public void overrideOpponentBestHit(int v) { this.opponentBestHit = v; }
    public void overridePlayerWeapon(String v) { this.playerWeapon = v; }
    public void overrideOpponentWeapon(String v) { this.opponentWeapon = v; }
    public void overridePlayerMaxCombo(int v) { this.playerMaxCombo = v; }
    public void overrideOpponentMaxCombo(int v) { this.opponentMaxCombo = v; }
    public void overridePlayerMissedHits(int v) { this.playerMissedHits = v; }
    public void overrideOpponentMissedHits(int v) { this.opponentMissedHits = v; }
    public void overridePlayerCriticalHits(int v) { this.playerCriticalHits = v; }
    public void overrideOpponentCriticalHits(int v) { this.opponentCriticalHits = v; }
    public void overridePlayerHealedAmount(int v) { this.playerHealedAmount = v; }
    public void overrideOpponentHealedAmount(int v) { this.opponentHealedAmount = v; }
    public void overridePlayerEffects(String effects) {
        if (effects == null || effects.isEmpty()) return;
        for (String e : effects.split(",")) this.playerEffects.add(e.trim());
    }
    public void overrideOpponentEffects(String effects) {
        if (effects == null || effects.isEmpty()) return;
        for (String e : effects.split(",")) this.opponentEffects.add(e.trim());
    }
    public void overridePlayerGapples(int v) { this.playerGapplesUsed = v; }
    public void overrideOpponentGapples(int v) { this.opponentGapplesUsed = v; }
    public void overridePlayerNotchApples(int v) { this.playerNotchApplesUsed = v; }
    public void overrideOpponentNotchApples(int v) { this.opponentNotchApplesUsed = v; }
    public void overridePlayerTotems(int v) { this.playerTotemsPopped = v; }
    public void overrideOpponentTotems(int v) { this.opponentTotemsPopped = v; }
    public void overridePlayerArrowsShot(int v) { this.playerArrowsShot = v; }
    public void overrideOpponentArrowsShot(int v) { this.opponentArrowsShot = v; }
    public void overridePlayerArrowsHit(int v) { this.playerArrowsHit = v; }
    public void overrideOpponentArrowsHit(int v) { this.opponentArrowsHit = v; }
    public void overridePlayerPearls(int v) { this.playerPearlsThrown = v; }
    public void overrideOpponentPearls(int v) { this.opponentPearlsThrown = v; }
    public void overridePlayerShieldBlocks(int v) { this.playerShieldBlocks = v; }
    public void overrideOpponentShieldBlocks(int v) { this.opponentShieldBlocks = v; }
    public void overridePlayerShieldBroken(boolean v) { this.playerShieldBroken = v; }
    public void overrideOpponentShieldBroken(boolean v) { this.opponentShieldBroken = v; }
    public void overridePlayerSharpness(int v) { this.playerHighestSharpness = v; }
    public void overrideOpponentSharpness(int v) { this.opponentHighestSharpness = v; }
    public void overridePlayerProtection(int v) { this.playerHighestProtection = v; }
    public void overrideOpponentProtection(int v) { this.opponentHighestProtection = v; }
    public void overridePlayerXpLevel(int v) { this.playerXpLevel = v; }
    public void overrideOpponentXpLevel(int v) { this.opponentXpLevel = v; }
    public void overridePlayerHunger(float v) { this.playerHungerOnStart = v; }
    public void overrideOpponentHunger(float v) { this.opponentHungerOnStart = v; }
    public void overrideFightWorld(String v) { this.fightWorld = v; }

    // getters
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public UUID getOpponentUUID() { return opponentUUID; }
    public String getOpponentName() { return opponentName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getPlayerStartHp() { return playerStartHp; }
    public int getPlayerEndHp() { return playerEndHp; }
    public int getOpponentStartHp() { return opponentStartHp; }
    public int getOpponentEndHp() { return opponentEndHp; }
    public int getPlayerHitsDealt() { return playerHitsDealt; }
    public int getOpponentHitsDealt() { return opponentHitsDealt; }
    public int getPlayerMissedHits() { return playerMissedHits; }
    public int getOpponentMissedHits() { return opponentMissedHits; }
    public int getPlayerCriticalHits() { return playerCriticalHits; }
    public int getOpponentCriticalHits() { return opponentCriticalHits; }
    public int getPlayerDamageDealt() { return playerDamageDealt; }
    public int getOpponentDamageDealt() { return opponentDamageDealt; }
    public int getPlayerBestHit() { return playerBestHit; }
    public int getOpponentBestHit() { return opponentBestHit; }
    public int getPlayerMaxCombo() { return playerMaxCombo; }
    public int getOpponentMaxCombo() { return opponentMaxCombo; }
    public int getPlayerHealedAmount() { return playerHealedAmount; }
    public int getOpponentHealedAmount() { return opponentHealedAmount; }
    public Set<String> getPlayerEffects() { return playerEffects; }
    public Set<String> getOpponentEffects() { return opponentEffects; }
    public int getPlayerGapplesUsed() { return playerGapplesUsed; }
    public int getOpponentGapplesUsed() { return opponentGapplesUsed; }
    public int getPlayerNotchApplesUsed() { return playerNotchApplesUsed; }
    public int getOpponentNotchApplesUsed() { return opponentNotchApplesUsed; }
    public int getPlayerTotemsPopped() { return playerTotemsPopped; }
    public int getOpponentTotemsPopped() { return opponentTotemsPopped; }
    public int getPlayerArrowsShot() { return playerArrowsShot; }
    public int getOpponentArrowsShot() { return opponentArrowsShot; }
    public int getPlayerArrowsHit() { return playerArrowsHit; }
    public int getOpponentArrowsHit() { return opponentArrowsHit; }
    public int getPlayerPearlsThrown() { return playerPearlsThrown; }
    public int getOpponentPearlsThrown() { return opponentPearlsThrown; }
    public int getPlayerShieldBlocks() { return playerShieldBlocks; }
    public int getOpponentShieldBlocks() { return opponentShieldBlocks; }
    public boolean isPlayerShieldBroken() { return playerShieldBroken; }
    public boolean isOpponentShieldBroken() { return opponentShieldBroken; }
    public int getPlayerHighestSharpness() { return playerHighestSharpness; }
    public int getOpponentHighestSharpness() { return opponentHighestSharpness; }
    public int getPlayerHighestProtection() { return playerHighestProtection; }
    public int getOpponentHighestProtection() { return opponentHighestProtection; }
    public int getPlayerXpLevel() { return playerXpLevel; }
    public int getOpponentXpLevel() { return opponentXpLevel; }
    public float getPlayerHungerOnStart() { return playerHungerOnStart; }
    public float getOpponentHungerOnStart() { return opponentHungerOnStart; }
    public String getPlayerWeapon() { return playerWeapon != null ? playerWeapon : "Unknown"; }
    public String getOpponentWeapon() { return opponentWeapon != null ? opponentWeapon : "Unknown"; }
    public String getFightWorld() { return fightWorld != null ? fightWorld : "Unknown"; }
    public WinType getWinType() { return winType; }
    public boolean isFinished() { return finished; }
    public void setPlayerEndHp(int hp) { this.playerEndHp = hp; }
    public void setOpponentEndHp(int hp) { this.opponentEndHp = hp; }
}