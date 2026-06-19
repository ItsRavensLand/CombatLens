package io.github.ItsRavensLand.combatLens.config;

import io.github.ItsRavensLand.combatLens.CombatLens;

// reads and caches config.yml values
public class ConfigManager {

    private static ConfigManager instance;

    private int timeout;
    private int minFightDuration;
    private int maxHistory;
    private boolean combatTagEnabled;
    private String inCombatMessage;
    private String outOfCombatMessage;

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    public void load() {
        CombatLens plugin = CombatLens.getInstance();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        timeout = plugin.getConfig().getInt("combat.timeout", 15);
        minFightDuration = plugin.getConfig().getInt("combat.min-fight-duration", 5);
        maxHistory = plugin.getConfig().getInt("combat.max-history", 10);
        combatTagEnabled = plugin.getConfig().getBoolean("combat-tag.enabled", true);
        inCombatMessage = plugin.getConfig().getString("messages.in-combat", "In Combat with {player}");
        outOfCombatMessage = plugin.getConfig().getString("messages.out-of-combat", "You are no longer in combat");

        plugin.getLogger().info("Config loaded!");
    }

    public int getTimeout() { return timeout; }
    public int getMinFightDuration() { return minFightDuration; }
    public int getMaxHistory() { return maxHistory; }
    public boolean isCombatTagEnabled() { return combatTagEnabled; }
    public String getInCombatMessage() { return inCombatMessage; }
    public String getOutOfCombatMessage() { return outOfCombatMessage; }
}
