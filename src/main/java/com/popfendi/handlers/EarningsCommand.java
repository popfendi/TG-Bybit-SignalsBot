package com.popfendi.handlers;

import com.popfendi.bots.SignalBot;
import com.popfendi.repository.DataManager;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;

public class EarningsCommand extends BotCommand {

    private String newLine = System.getProperty("line.separator");

    private static final Logger LOGGER = Logger.getLogger( EarningsCommand.class.getName() );

    public EarningsCommand() {
        super("earnings", "Projected earnings if followed all signals");
    }

    // command for tracking earnings stats
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Double total = DataManager.getInstance().getProfitAggregation();

        StringBuilder message = new StringBuilder();
        message.append("<b>Earnings:</b>");
        message.append(newLine);
        message.append("<i>(10x Leverage)</i>");
        message.append(newLine);
        message.append(newLine);

        if (total == null){
          message.append("Error: can't aggregate data right now.");
        } else {
            message.append(String.format("%.3f%%", total));
        }


        SendMessage answer = new SendMessage();
        answer.enableHtml(true);
        answer.setChatId(chat.getId().toString());
        answer.setText(message.toString());

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            LOGGER.info(e.getMessage());
        }
    }
}
