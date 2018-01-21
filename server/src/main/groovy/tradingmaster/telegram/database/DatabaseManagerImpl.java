package tradingmaster.telegram.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.bot.kernel.database.DatabaseManager;
import org.telegram.bot.services.BotLogger;
import org.telegram.bot.structure.Chat;
import org.telegram.bot.structure.IUser;
import tradingmaster.telegram.structure.ChatImpl;
import tradingmaster.telegram.structure.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief TODO
 * @date 16 of October of 2016
 */
public class DatabaseManagerImpl implements DatabaseManager {
    private static final String LOGTAG = "DATABASEMANAGER";
    private static volatile ConnectionDB connetion;

    Map<Integer, Chat> chatMap = new ConcurrentHashMap();

    Map<Integer, int[]>  diffrencesMap = new ConcurrentHashMap();

    Map<Integer, User> userMap = new ConcurrentHashMap<>();

    /**
     * Private constructor (due to Singleton)
     */
    public DatabaseManagerImpl() {
        connetion = new ConnectionDB();
        final int currentVersion = connetion.checkVersion();
        BotLogger.info(LOGTAG, "Current db version: " + currentVersion);
        if (currentVersion < CreationStrings.version) {
            recreateTable(currentVersion);
        }
    }

    /**
     * Recreates the DB
     */
    private void recreateTable(int currentVersion) {
        try {
            connetion.initTransaction();
            if (currentVersion == 0) {
                createNewTables();
            }
            connetion.commitTransaction();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    private int createNewTables() throws SQLException {
        connetion.executeQuery(CreationStrings.createVersionTable);
        connetion.executeQuery(CreationStrings.createUsersTable);
        connetion.executeQuery(CreationStrings.insertCurrentVersion);
        connetion.executeQuery(CreationStrings.createChatTable);
        connetion.executeQuery(CreationStrings.createDifferencesDataTable);
        return CreationStrings.version;
    }

    /**
     * Gets an user by id
     *
     * @param userId ID of the user
     * @return User requested or null if it doesn't exists
     * @see User
     */
    @Override
    public @Nullable IUser getUserById(int userId) {
        return userMap.get(userId);
    }

    /**
     * Adds an user to the database
     *
     * @param user User to be added
     * @return true if it was added, false otherwise
     * @see User
     */
    public boolean addUser(@NotNull User user) {

        userMap.put(user.getUserId(), user);
        return true;
    }

    public boolean updateUser(@NotNull User user) {
        userMap.put(user.getUserId(), user);
        return true;
    }

    @Override
    public @Nullable Chat getChatById(int chatId) {

        return chatMap.get(chatId);
    }

    /**
     * Adds a chat to the database
     *
     * @param chat User to be added
     * @return true if it was added, false otherwise
     * @see User
     */
    public boolean addChat(@NotNull ChatImpl chat) {
        this.chatMap.put(chat.getId(), chat);
        return true;
    }

    public boolean updateChat(ChatImpl chat) {
        return addChat(chat);
    }

    @Override
    public @NotNull HashMap<Integer, int[]> getDifferencesData() {
        return new HashMap(diffrencesMap);
    }

    @Override
    public boolean updateDifferencesData(int botId, int pts, int date, int seq) {
        diffrencesMap.put(botId, new int[]{ pts, date, seq });
        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        connetion.closeConexion();
        super.finalize();
    }

}
