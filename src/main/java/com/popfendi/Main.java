package com.popfendi;

import com.popfendi.bots.SignalBot;
import com.popfendi.client.Client;
import com.popfendi.config.ArgsParser;
import com.popfendi.config.PropertiesLoader;
import com.popfendi.repository.DataManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        //load CLI args into config
        ArgsParser.handleArgs(args);

        // init Application
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(SignalBot.getBotInstance());
                new Thread(Client::connectWebsocket).start();
                DataManager.getInstance().init();
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }

    }
}