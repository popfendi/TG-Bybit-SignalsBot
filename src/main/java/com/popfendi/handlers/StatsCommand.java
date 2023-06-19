package com.popfendi.handlers;

import com.popfendi.models.Stats;
import com.popfendi.repository.DataManager;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;

// stats command for all signals and success rates
public class StatsCommand extends BotCommand {
    private String newLine = System.getProperty("line.separator");

    private static final Logger LOGGER = Logger.getLogger( StatsCommand.class.getName() );
    public StatsCommand() {
        super("stats", "Get a breakdown of all trades");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Stats stats = DataManager.getInstance().getStatsAggregation();

        StringBuilder message = new StringBuilder();
        message.append("<b>Statistics:</b>");
        message.append(newLine);
        message.append(newLine);

        float tp1perc = (float) stats.getTp1() / (float) stats.getTotal() * 100;
        float tp2perc = (float) stats.getTp2() / (float) stats.getTotal() * 100;
        float tp3perc = (float) stats.getTp3() / (float) stats.getTotal() * 100;

        if (stats == null){
            message.append("Error: can't fetch data right now.");
        } else {
            message.append(String.format("Wins: %d/%d \uD83C\uDFC5", stats.getWins(), stats.getTotal()));
            message.append(newLine);
            message.append(String.format("Open: %d/%d \uD83D\uDD30", stats.getOpen(), stats.getTotal()));
            message.append(newLine);
            message.append(String.format("SL: %d/%d ❌", stats.getSl(), stats.getTotal()));
            message.append(newLine);
            message.append(newLine);
            message.append(String.format("TP1: %d/%d ( %.2f%% ) ✅" ,stats.getTp1(), stats.getTotal(), tp1perc));
            message.append(newLine);
            message.append(String.format("TP2: %d/%d ( %.2f%% ) ✅✅" ,stats.getTp2(), stats.getTotal(), tp2perc));
            message.append(newLine);
            message.append(String.format("TP3: %d/%d ( %.2f%% ) ✅✅✅" ,stats.getTp3(), stats.getTotal(), tp3perc));
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
