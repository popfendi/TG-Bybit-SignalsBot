package com.popfendi.handlers;

import com.popfendi.bots.SignalBot;
import com.popfendi.models.Signal;
import com.popfendi.repository.DataManager;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.logging.Logger;

public class ListCommand extends BotCommand {

    private String newLine = System.getProperty("line.separator");

    private static final Logger LOGGER = Logger.getLogger( ListCommand.class.getName() );

    public ListCommand() {
        super("list", "List previous 10 trades");
    }

    // command for listng last 10 signals stats
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        List<Signal> signals = DataManager.getInstance().getLastTenSignals();

        StringBuilder message = new StringBuilder();
        message.append("<b>Last 10 Signals:</b>");
        message.append(newLine);
        message.append(newLine);
        String emptyMessage = "Error: No Signals right now.";

        for (Signal s:
             signals) {
            message.append(s.getPair() + " ");

            if(s.getTargetsHit().isTp3()){
                message.append("✅✅✅");
            } else if (s.getTargetsHit().isTp2()) {
                message.append("✅✅");
            } else if (s.getTargetsHit().isTp1()) {
                message.append("✅");
            } else if (s.getTargetsHit().isSl()) {
                message.append("❌");
            }

            message.append(newLine);
        }

        SendMessage answer = new SendMessage();
        answer.enableHtml(true);
        answer.setChatId(chat.getId().toString());
        answer.setText(signals.size() > 0 ? message.toString() : emptyMessage);

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
           LOGGER.info(e.getMessage());
        }

    }

}
