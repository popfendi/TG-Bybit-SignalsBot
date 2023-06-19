package com.popfendi.bots;

import com.popfendi.config.PropertiesLoader;
import com.popfendi.handlers.EarningsCommand;
import com.popfendi.handlers.ListCommand;
import com.popfendi.handlers.MessageHandler;
import com.popfendi.handlers.StatsCommand;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;

// the actual bot
public class SignalBot extends TelegramLongPollingCommandBot {

    public static  SignalBot botInstance;

    private static final Logger LOGGER = Logger.getLogger( SignalBot.class.getName() );


    private String channelId = PropertiesLoader.getProperties().getProperty("channel.id");
    private String adminChatId = PropertiesLoader.getProperties().getProperty("admin.chat.id");

    public String getBotToken(){
        return PropertiesLoader.getProperties().getProperty("bot.key");
    }

    public SignalBot(){
        register(new ListCommand());
        register(new EarningsCommand());
        register(new StatsCommand());
    }

    public static SignalBot getBotInstance(){
        if(botInstance == null){
            botInstance = new SignalBot();
        }

        return botInstance;
    }


    private MessageHandler messageHandler = new MessageHandler();

    @Override
    public String getBotUsername() {
        return "TradeTrackerBot";
    }

    public void broadcastToChannel(String message){
        SendMessage sm = new SendMessage();

        sm.setChatId(channelId);
        sm.setText(message);

        try {
            execute(sm);
            LOGGER.info("Broadcasting to channel: " + message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendErrorMessageToUser(String message){
        SendMessage sm = new SendMessage();

        sm.setChatId(adminChatId);
        sm.setText(message);

        try {
            execute(sm);
            LOGGER.info("Sending error message to user " + message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        LOGGER.info("Recieved Update! " + update.getUpdateId());

        boolean channelPost = update.hasChannelPost() && update.getChannelPost().hasText() && channelId.equals(String.valueOf(update.getChannelPost().getChatId()));

        // We check if the update comes from the channel
        if (channelPost) {

            messageHandler.insertAndTrackSignal(update.getChannelPost().getText());

        }
    }
}
