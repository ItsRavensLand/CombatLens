package io.github.ItsRavensLand.combatLens;

import io.github.ItsRavensLand.combatLens.gui.CombatHistoryGUI;
import io.github.ItsRavensLand.combatLens.gui.GUIListener;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CombatLens extends JavaPlugin {

    private static CombatLens instance;

    @Override
    public void onEnable() {
        instance = this;

        DatabaseManager.getInstance().connect();
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        CombatTagManager.getInstance().startTagTask();

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(
                    Commands.literal("combatlens")
                            .executes(ctx -> {
                                if (ctx.getSource().getSender() instanceof org.bukkit.entity.Player player) {
                                    CombatHistoryGUI.open(player);
                                }
                                return 1;
                            })
                            .then(Commands.literal("help")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getSender() instanceof org.bukkit.entity.Player player) {
                                            new CombatLensCommand().sendHelpPublic(player);
                                        }
                                        return 1;
                                    }))
                            .then(Commands.literal("stats")
                                    .executes(ctx -> {
                                        if (ctx.getSource().getSender() instanceof org.bukkit.entity.Player player) {
                                            new CombatLensCommand().sendStatsPublic(player);
                                        }
                                        return 1;
                                    }))
                            .build(),
                    "Open your combat history",
                    List.of("cl", "combat")
            );
        });

        getLogger().info("CombatLens enabled!");
    }

    @Override
    public void onDisable() {
        DatabaseManager.getInstance().disconnect();
        getLogger().info("CombatLens disabled.");
    }

    public static CombatLens getInstance() {
        return instance;
    }
}