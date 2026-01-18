package dev.Fjc.ultraBans.file.database;

import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.Util;
import dev.Fjc.ultraBans.api.Entry;
import dev.Fjc.ultraBans.api.mutes.MuteEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;

public class SQLiteManager {
    private static final UltraBans plugin = UltraBans.getInstance();

    private Connection connection;

    // Start the database connection locally
    public void startConnection() {
        if (isOpen()) return;

        Util.info("Preparing the database for connection...");
        try {
            File folder = plugin.getDataFolder();
            if (!folder.exists()) folder.mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(folder, "entrydatabase.db"));
        } catch (SQLException e) {
            plugin.getLogger().warning("Something has gone wrong while loading the database!");
            plugin.getLogger().warning(e.getLocalizedMessage());
            plugin.getLogger().warning(e.getSQLState());
            connection = null;
        } finally {
            if (connection != null) {
                Util.info("The connection to the database was successful.");
            }
        }
    }

    // Ends said connection
    public void closeConnection() {
        Util.info("Preparing to shut down the database...");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Util.info("Shutdown complete.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Something went horribly wrong while trying to close the database!!");
            plugin.getLogger().severe(e.getLocalizedMessage());
        }
    }

    public final boolean isOpen() {
        boolean open;
        try {
            open = connection != null && !connection.isClosed();
        } catch (SQLException e) {
            Util.err("The database is closed!");
            Util.err(Arrays.toString(e.getStackTrace()));
            open = false;
        }
        return open;
    }

    /**
     * Builds a new table for entries to use.
     * @return True if the table was created or already exists, false if something went wrong
     */
    public boolean buildTable() {
        if (!isOpen()) return false;
        String table = "CREATE TABLE IF NOT EXISTS entries(" +
                "autoId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "punishID TEXT NOT NULL, " +
                "punishType TEXT NOT NULL, " +
                "player TEXT NOT NULL, " +
                "executor TEXT NOT NULL, " +
                "reason TEXT, " +
                "createdAt TEXT, " +
                "duration TEXT, " +
                "CONSTRAINT uniqueId UNIQUE(player,punishID) "
                + ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(table);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("Something has gone wrong with the database!");
            plugin.getLogger().warning("Likely related to some table creation.");
            plugin.getLogger().warning(e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Adds an {@link Entry} to the table.
     * @param entry The entry to add, cannot be null
     * @return True if the action was successful, false if something went wrong
     */
    public boolean addEntry(@NotNull Entry<?> entry) {
        if (!isOpen()) return false;

        String id = entry.id().toString();
        String type = entry.type().toString();
        String target = entry.targetToString();
        String executor = entry.source();
        String reason = entry.reason();
        String created = Util.formatDateTime(entry.creationTime());
        String duration = null;
        if (entry instanceof MuteEntry<?> muteEntry) {
            duration = muteEntry.getDuration().toString();
        }

        String updater = "INSERT INTO entries (punishID, punishType, player, executor, reason, createdAt, duration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement p = connection.prepareStatement(updater)) {
            p.setString(1, id);
            p.setString(2, type);
            p.setString(3, target);
            p.setString(4, executor);
            p.setString(5, reason);
            p.setString(6, created);
            p.setString(7, duration);

            p.executeUpdate();
            return true;
        } catch (SQLException exception) {
            plugin.getLogger().warning("Something has went wrong while writing to the database!");
            plugin.getLogger().warning(exception.getLocalizedMessage());
            return false;
        }

    }

    /**
     * Removes all entries associated with a specified target.
     * @param target The target specified
     * @return True if the removal was successful, false if the record does not exist or something else went wrong
     * @apiNote This removes ALL entries associated with the player. Some entry types may allow for multiple entries on a player,
     * so you should use {@link SQLiteManager#removeEntry(String, String)} to target a specific entry.
     */
    public boolean removeEntry(String target) {
        if (!isOpen()) return false;

        String remover = "DELETE FROM entries WHERE player = ?";
        try (PreparedStatement p = connection.prepareStatement(remover)) {
            p.setString(1, target);
            return p.executeUpdate() > 0;
        } catch (SQLException exception) {
            plugin.getLogger().warning("Something went wrong while writing to the database: " + target);
            plugin.getLogger().warning(exception.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Removes the entry associated with the specified target and ID.
     * @param target The target specified
     * @param id The ID specified
     * @return True if the removal was successful, false if the record does not exist or something else went wrong
     */
    public boolean removeEntry(String target, String id) {
        if (!isOpen()) return false;

        String remover = "DELETE FROM entries WHERE player = ? AND punishID = ?";

        try (PreparedStatement p = connection.prepareStatement(remover)) {
            p.setString(1, target);
            p.setString(2, id);

            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("Something went wrong while writing to the database: " + id);
            plugin.getLogger().warning(e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Updates the specified entry with new values. If you only need to update a certain value, leave the other params null. Certain params can't be null.
     * @param target The target of the entry, cannot be null
     * @param ID The entry ID, cannot be null
     * @param type The type of entry, cannot be null
     * @param executor The executor, or null to ignore
     * @param reason The reason, or null to ignore
     * @param created The creation date/time, or null to ignore
     * @return True if the update was successful, false if no changes were made or something went wrong
     */
    public boolean updateEntry(@NotNull String target, @NotNull final String ID, @NotNull String type, @Nullable String executor, @Nullable String reason, @Nullable LocalDateTime created) {
        if (!isOpen()) return false;

        String updater = "UPDATE entries SET executor = COALESCE(?, executor), reason = COALESCE(?, reason), createdAt = COALESCE(?, createdAt) WHERE player = ? AND punishID = ? AND punishType = ?";
        try (PreparedStatement p = connection.prepareStatement(updater)) {
            p.setString(1, executor);
            p.setString(2, reason);
            if (created != null) p.setString(3, Util.formatDateTime(created));
            else p.setNull(3, Types.VARCHAR);

            p.setString(4, target);
            p.setString(5, ID);
            p.setString(6, type);

            return p.executeUpdate() > 0;
        } catch (SQLException exception) {
            plugin.getLogger().warning("Something went wrong while writing to the database: " + ID);
            plugin.getLogger().warning(exception.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Remove every entry from the table
     * @apiNote This removes ALL entries!! If you only need to remove a specific type, use {@link SQLiteManager#wipeDir(Entry.Type)}
     * or {@link SQLiteManager#removeEntry(String)} to remove entries associated with a target.
     * @see SQLiteManager#wipeDir(Entry.Type)
     * @see SQLiteManager#removeEntry(String)
     * @see SQLiteManager#removeEntry(String, String)
     */
    public void wipeDir() {
        if (!isOpen()) return;

        String wiper = "DELETE FROM entries";
        try (PreparedStatement p = connection.prepareStatement(wiper)) {
            p.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Something went wrong while writing to the database.");
            plugin.getLogger().warning(exception.getLocalizedMessage());
        }
    }

    /**
     * Removes all entries in the table that match the given type
     * @param type The type of entry to remove
     * @return True if the removal was successful, false if the records do not exist or something went wrong
     * @apiNote This removes ALL entries of a given type! If you only want to remove a specific entry, try
     * {@link SQLiteManager#removeEntry(String, String)} or {@link SQLiteManager#removeEntry(String)}.
     * @see SQLiteManager#wipeDir()
     */
    public boolean wipeDir(Entry.Type type) {
        if (!isOpen()) return false;

        String remover = "DELETE FROM entries WHERE punishType = ?";
        try (PreparedStatement p = connection.prepareStatement(remover)) {
            p.setString(1, type.name());

            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("Something went wrong while writing to the database.");
            plugin.getLogger().warning(e.getLocalizedMessage());
            return false;
        }
    }

}
