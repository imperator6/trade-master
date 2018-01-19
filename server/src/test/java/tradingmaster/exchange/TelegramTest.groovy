package tradingmaster.exchange

import junit.framework.TestCase
import org.junit.Test
import org.telegram.bot.handlers.interfaces.IChatsHandler
import org.telegram.bot.handlers.interfaces.IUsersHandler
import org.telegram.bot.kernel.TelegramBot
import org.telegram.bot.services.BotLogger
import org.telegram.bot.structure.BotConfig
import org.telegram.bot.structure.LoginStatus
import tradingmaster.telegram.BotConfigImpl
import tradingmaster.telegram.ChatUpdatesBuilderImpl
import tradingmaster.telegram.CustomUpdatesHandler
import tradingmaster.telegram.database.DatabaseManagerImpl
import tradingmaster.telegram.handlers.ChatsHandler
import tradingmaster.telegram.handlers.MessageHandler
import tradingmaster.telegram.handlers.TLMessageHandler
import tradingmaster.telegram.handlers.UsersHandler

class TelegramTest extends TestCase {

    private static final int APIKEY = 12444977; // your api key
    private static final String APIHASH = "5f3ad29331de5ed0925e3c770c513dfe77"; // your api hash
    private static final String PHONENUMBER = ""; // Your phone number


    @Test
    void testgetTelegram() {

        final DatabaseManagerImpl databaseManager = new DatabaseManagerImpl();
        final BotConfig botConfig = new BotConfigImpl(PHONENUMBER);

        final IUsersHandler usersHandler = new UsersHandler(databaseManager);
        final IChatsHandler chatsHandler = new ChatsHandler(databaseManager);
        final MessageHandler messageHandler = new MessageHandler();
        final TLMessageHandler tlMessageHandler = new TLMessageHandler(messageHandler, databaseManager);

        final ChatUpdatesBuilderImpl builder = new ChatUpdatesBuilderImpl(CustomUpdatesHandler.class);
        builder.setBotConfig(botConfig)
                .setDatabaseManager(databaseManager)
                .setUsersHandler(usersHandler)
                .setChatsHandler(chatsHandler)
                .setMessageHandler(messageHandler)
                .setTlMessageHandler(tlMessageHandler);

        try {
            final TelegramBot kernel = new TelegramBot(botConfig, builder, APIKEY, APIHASH);
            LoginStatus status = kernel.init();
           /* if (status == LoginStatus.CODESENT) {
                Scanner in = new Scanner(System.in);
                boolean success = kernel.getKernelAuth().setAuthCode(in.nextLine().trim());
                if (success) {
                    status = LoginStatus.ALREADYLOGGED;
                }
            } */
            if (status == LoginStatus.ALREADYLOGGED) {
                kernel.startBot();
            } else {
                throw new Exception("Failed to log in: " + status);
            }
        } catch (Exception e) {
            BotLogger.severe("MAIN", e);
        }

        while(true) {
            Thread.sleep(600)
        }

        assertEquals(2, 2)
    }



}
