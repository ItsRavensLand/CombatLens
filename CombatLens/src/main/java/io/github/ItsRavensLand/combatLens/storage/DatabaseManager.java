package io.github.ItsRavensLand.combatLens.storage;

import io.github.ItsRavensLand.combatLens.CombatLens;
import io.github.ItsRavensLand.combatLens.combat.CombatSession;
import io.github.ItsRavensLand.combatLens.config.ConfigManager;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// owns the sqlite connection and all fight persistence
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void connect() {
        try {
            File dataFolder = CombatLens.getInstance().getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();

            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/combatlens.db";
            connection = DriverManager.getConnection(url);
            createTables();
            CombatLens.getInstance().getLogger().info("Database connected!");

        } catch (SQLException e) {
            CombatLens.getInstance().getLogger().severe("DB connection failed: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                CombatLens.getInstance().getLogger().info("Database disconnected.");
            }
        } catch (SQLException e) {
            CombatLens.getInstance().getLogger().severe("DB disconnect failed: " + e.getMessage());
        }
    }

    // reopens the connection if it died, used before every query
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            File dataFolder = CombatLens.getInstance().getDataFolder();
            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/combatlens.db";
            connection = DriverManager.getConnection(url);
            CombatLens.getInstance().getLogger().warning("Database reconnected after drop.");
        }
        return connection;
    }

    private void createTables() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS combat_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                opponent_uuid TEXT NOT NULL,
                opponent_name TEXT NOT NULL,
                player_start_hp INTEGER,
                player_end_hp INTEGER,
                opponent_start_hp INTEGER,
                opponent_end_hp INTEGER,
                player_hits INTEGER,
                opponent_hits INTEGER,
                player_missed_hits INTEGER,
                opponent_missed_hits INTEGER,
                player_critical_hits INTEGER,
                opponent_critical_hits INTEGER,
                player_damage INTEGER,
                opponent_damage INTEGER,
                player_best_hit INTEGER,
                opponent_best_hit INTEGER,
                player_max_combo INTEGER,
                opponent_max_combo INTEGER,
                player_healed INTEGER,
                opponent_healed INTEGER,
                player_effects TEXT,
                opponent_effects TEXT,
                player_gapples INTEGER,
                opponent_gapples INTEGER,
                player_notch_apples INTEGER,
                opponent_notch_apples INTEGER,
                player_totems INTEGER,
                opponent_totems INTEGER,
                player_arrows_shot INTEGER,
                opponent_arrows_shot INTEGER,
                player_arrows_hit INTEGER,
                opponent_arrows_hit INTEGER,
                player_pearls INTEGER,
                opponent_pearls INTEGER,
                player_shield_blocks INTEGER,
                opponent_shield_blocks INTEGER,
                player_shield_broken INTEGER,
                opponent_shield_broken INTEGER,
                player_sharpness INTEGER,
                opponent_sharpness INTEGER,
                player_protection INTEGER,
                opponent_protection INTEGER,
                player_xp_level INTEGER,
                opponent_xp_level INTEGER,
                player_hunger REAL,
                opponent_hunger REAL,
                player_weapon TEXT,
                opponent_weapon TEXT,
                fight_world TEXT,
                win_type TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT,
                duration_seconds INTEGER
            );
        """;
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveSession(CombatSession session) {
        String sql = """
            INSERT INTO combat_sessions (
                player_uuid, player_name, opponent_uuid, opponent_name,
                player_start_hp, player_end_hp, opponent_start_hp, opponent_end_hp,
                player_hits, opponent_hits,
                player_missed_hits, opponent_missed_hits,
                player_critical_hits, opponent_critical_hits,
                player_damage, opponent_damage,
                player_best_hit, opponent_best_hit,
                player_max_combo, opponent_max_combo,
                player_healed, opponent_healed,
                player_effects, opponent_effects,
                player_gapples, opponent_gapples,
                player_notch_apples, opponent_notch_apples,
                player_totems, opponent_totems,
                player_arrows_shot, opponent_arrows_shot,
                player_arrows_hit, opponent_arrows_hit,
                player_pearls, opponent_pearls,
                player_shield_blocks, opponent_shield_blocks,
                player_shield_broken, opponent_shield_broken,
                player_sharpness, opponent_sharpness,
                player_protection, opponent_protection,
                player_xp_level, opponent_xp_level,
                player_hunger, opponent_hunger,
                player_weapon, opponent_weapon,
                fight_world, win_type, start_time, end_time, duration_seconds
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, session.getPlayerUUID().toString());
            stmt.setString(2, session.getPlayerName());
            stmt.setString(3, session.getOpponentUUID().toString());
            stmt.setString(4, session.getOpponentName());
            stmt.setInt(5, session.getPlayerStartHp());
            stmt.setInt(6, session.getPlayerEndHp());
            stmt.setInt(7, session.getOpponentStartHp());
            stmt.setInt(8, session.getOpponentEndHp());
            stmt.setInt(9, session.getPlayerHitsDealt());
            stmt.setInt(10, session.getOpponentHitsDealt());
            stmt.setInt(11, session.getPlayerMissedHits());
            stmt.setInt(12, session.getOpponentMissedHits());
            stmt.setInt(13, session.getPlayerCriticalHits());
            stmt.setInt(14, session.getOpponentCriticalHits());
            stmt.setInt(15, session.getPlayerDamageDealt());
            stmt.setInt(16, session.getOpponentDamageDealt());
            stmt.setInt(17, session.getPlayerBestHit());
            stmt.setInt(18, session.getOpponentBestHit());
            stmt.setInt(19, session.getPlayerMaxCombo());
            stmt.setInt(20, session.getOpponentMaxCombo());
            stmt.setInt(21, session.getPlayerHealedAmount());
            stmt.setInt(22, session.getOpponentHealedAmount());
            stmt.setString(23, String.join(", ", session.getPlayerEffects()));
            stmt.setString(24, String.join(", ", session.getOpponentEffects()));
            stmt.setInt(25, session.getPlayerGapplesUsed());
            stmt.setInt(26, session.getOpponentGapplesUsed());
            stmt.setInt(27, session.getPlayerNotchApplesUsed());
            stmt.setInt(28, session.getOpponentNotchApplesUsed());
            stmt.setInt(29, session.getPlayerTotemsPopped());
            stmt.setInt(30, session.getOpponentTotemsPopped());
            stmt.setInt(31, session.getPlayerArrowsShot());
            stmt.setInt(32, session.getOpponentArrowsShot());
            stmt.setInt(33, session.getPlayerArrowsHit());
            stmt.setInt(34, session.getOpponentArrowsHit());
            stmt.setInt(35, session.getPlayerPearlsThrown());
            stmt.setInt(36, session.getOpponentPearlsThrown());
            stmt.setInt(37, session.getPlayerShieldBlocks());
            stmt.setInt(38, session.getOpponentShieldBlocks());
            stmt.setInt(39, session.isPlayerShieldBroken() ? 1 : 0);
            stmt.setInt(40, session.isOpponentShieldBroken() ? 1 : 0);
            stmt.setInt(41, session.getPlayerHighestSharpness());
            stmt.setInt(42, session.getOpponentHighestSharpness());
            stmt.setInt(43, session.getPlayerHighestProtection());
            stmt.setInt(44, session.getOpponentHighestProtection());
            stmt.setInt(45, session.getPlayerXpLevel());
            stmt.setInt(46, session.getOpponentXpLevel());
            stmt.setFloat(47, session.getPlayerHungerOnStart());
            stmt.setFloat(48, session.getOpponentHungerOnStart());
            stmt.setString(49, session.getPlayerWeapon());
            stmt.setString(50, session.getOpponentWeapon());
            stmt.setString(51, session.getFightWorld());
            stmt.setString(52, session.getWinType().name());
            stmt.setString(53, session.getStartTime().toString());
            stmt.setString(54, session.getEndTime() != null ? session.getEndTime().toString() : null);
            stmt.setLong(55, session.getDurationSeconds());
            stmt.executeUpdate();
        } catch (SQLException e) {
            CombatLens.getInstance().getLogger().severe("Failed to save session: " + e.getMessage());
        }
    }

    // limit follows config so history table and stored rows stay in sync
    public List<CombatSession> loadHistory(UUID playerUUID) {
        List<CombatSession> history = new ArrayList<>();
        String sql = """
            SELECT * FROM combat_sessions
            WHERE player_uuid = ?
            ORDER BY id DESC
            LIMIT ?
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, ConfigManager.getInstance().getMaxHistory());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CombatSession session = mapResultSet(rs);
                if (session != null) history.add(session);
            }
        } catch (SQLException e) {
            CombatLens.getInstance().getLogger().severe("Failed to load history: " + e.getMessage());
        }
        return history;
    }

    private CombatSession mapResultSet(ResultSet rs) {
        try {
            CombatSession session = new CombatSession(
                UUID.fromString(rs.getString("player_uuid")),
                rs.getString("player_name"),
                UUID.fromString(rs.getString("opponent_uuid")),
                rs.getString("opponent_name"),
                rs.getInt("player_start_hp"),
                rs.getInt("opponent_start_hp")
            );

            session.setPlayerEndHp(rs.getInt("player_end_hp"));
            session.setOpponentEndHp(rs.getInt("opponent_end_hp"));

            for (int i = 0; i < rs.getInt("player_hits"); i++) session.addPlayerHit(0);
            for (int i = 0; i < rs.getInt("opponent_hits"); i++) session.addOpponentHit(0);

            session.overridePlayerDamage(rs.getInt("player_damage"));
            session.overrideOpponentDamage(rs.getInt("opponent_damage"));
            session.overridePlayerBestHit(rs.getInt("player_best_hit"));
            session.overrideOpponentBestHit(rs.getInt("opponent_best_hit"));
            session.overridePlayerMaxCombo(rs.getInt("player_max_combo"));
            session.overrideOpponentMaxCombo(rs.getInt("opponent_max_combo"));
            session.overridePlayerMissedHits(rs.getInt("player_missed_hits"));
            session.overrideOpponentMissedHits(rs.getInt("opponent_missed_hits"));
            session.overridePlayerCriticalHits(rs.getInt("player_critical_hits"));
            session.overrideOpponentCriticalHits(rs.getInt("opponent_critical_hits"));
            session.overridePlayerHealedAmount(rs.getInt("player_healed"));
            session.overrideOpponentHealedAmount(rs.getInt("opponent_healed"));
            session.overridePlayerEffects(rs.getString("player_effects"));
            session.overrideOpponentEffects(rs.getString("opponent_effects"));
            session.overridePlayerGapples(rs.getInt("player_gapples"));
            session.overrideOpponentGapples(rs.getInt("opponent_gapples"));
            session.overridePlayerNotchApples(rs.getInt("player_notch_apples"));
            session.overrideOpponentNotchApples(rs.getInt("opponent_notch_apples"));
            session.overridePlayerTotems(rs.getInt("player_totems"));
            session.overrideOpponentTotems(rs.getInt("opponent_totems"));
            session.overridePlayerArrowsShot(rs.getInt("player_arrows_shot"));
            session.overrideOpponentArrowsShot(rs.getInt("opponent_arrows_shot"));
            session.overridePlayerArrowsHit(rs.getInt("player_arrows_hit"));
            session.overrideOpponentArrowsHit(rs.getInt("opponent_arrows_hit"));
            session.overridePlayerPearls(rs.getInt("player_pearls"));
            session.overrideOpponentPearls(rs.getInt("opponent_pearls"));
            session.overridePlayerShieldBlocks(rs.getInt("player_shield_blocks"));
            session.overrideOpponentShieldBlocks(rs.getInt("opponent_shield_blocks"));
            session.overridePlayerShieldBroken(rs.getInt("player_shield_broken") == 1);
            session.overrideOpponentShieldBroken(rs.getInt("opponent_shield_broken") == 1);
            session.overridePlayerSharpness(rs.getInt("player_sharpness"));
            session.overrideOpponentSharpness(rs.getInt("opponent_sharpness"));
            session.overridePlayerProtection(rs.getInt("player_protection"));
            session.overrideOpponentProtection(rs.getInt("opponent_protection"));
            session.overridePlayerXpLevel(rs.getInt("player_xp_level"));
            session.overrideOpponentXpLevel(rs.getInt("opponent_xp_level"));
            session.overridePlayerHunger(rs.getFloat("player_hunger"));
            session.overrideOpponentHunger(rs.getFloat("opponent_hunger"));
            session.overridePlayerWeapon(rs.getString("player_weapon"));
            session.overrideOpponentWeapon(rs.getString("opponent_weapon"));
            session.overrideFightWorld(rs.getString("fight_world"));

            session.finish(
                CombatSession.WinType.valueOf(rs.getString("win_type")),
                LocalDateTime.parse(rs.getString("start_time")),
                rs.getString("end_time") != null ? LocalDateTime.parse(rs.getString("end_time")) : null
            );

            return session;
        } catch (SQLException e) {
            CombatLens.getInstance().getLogger().severe("Failed to map session: " + e.getMessage());
            return null;
        }
    }
}
