package com.popfendi.handlers;


import com.popfendi.bots.SignalBot;
import com.popfendi.models.Direction;
import com.popfendi.models.Signal;
import com.popfendi.models.Targets;
import com.popfendi.repository.DataManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// handles channel messages (class name could have been clearer soz)
public class MessageHandler {

    private DataManager dm = DataManager.getInstance();
    // parses the signal message from the channel, persiss in db & begins tracking the prices, sends error to admin chat if unsuccessful.
    private Signal parseSignalMessage(String message){
        final String regex = ".*Pair:\\s*(?<pair>\\S*)\\s*Direction:\\s*(?<direction>\\S*)\\s*Entry:\\s*(?<entry>\\S*)\\s*TP1:\\s*(?<tp1>\\S*)\\s*TP2:\\s*(?<tp2>\\S*)\\s*TP3:\\s*(?<tp3>\\S*)\\s*SL:\\s(?<sl>\\S*)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(message);


        while (matcher.find()){
            boolean missingData =
                    matcher.group("pair") == null ||  matcher.group("direction") == null
                    || matcher.group("entry") == null || matcher.group("tp1") == null
                    || matcher.group("sl") == null;

            boolean badDirection = !matcher.group("direction").equalsIgnoreCase("long") &&
                    !matcher.group("direction").equalsIgnoreCase("short");

            if (missingData || badDirection){
                SignalBot.getBotInstance().sendErrorMessageToUser("I'm unable to create a signal with the data you provided, please check the format and try again.");
                return null;
            }

            String pair = matcher.group("pair");
            Direction direction = matcher.group("direction").equalsIgnoreCase("long") ? Direction.Long : Direction.Short;
            String entry = matcher.group("entry");
            String tp1 = matcher.group("tp1");
            String tp2 = matcher.group("tp2");
            String tp3 = matcher.group("tp3");
            String sl = matcher.group("sl");

            try {
                return new Signal(
                        pair,
                        direction,
                        new BigDecimal(entry),
                        new BigDecimal(tp1),
                        new BigDecimal(tp2),
                        new BigDecimal(tp3),
                        new BigDecimal(sl),
                        true,
                        new Targets(),
                        new Date(),
                        UUID.randomUUID().toString()
                );
            } catch (Exception e){
                SignalBot.getBotInstance().sendErrorMessageToUser("I'm unable to create a signal with the data you provided, please check the format and try again.");
                e.printStackTrace();
                return null;
            }
        }
            SignalBot.getBotInstance().sendErrorMessageToUser("I'm unable to create a signal with the data you provided, please check the format and try again.");
            return null;

    }

    public void insertAndTrackSignal(String message){
        Signal signal = parseSignalMessage(message);
        if (signal != null) {
            dm.insertAndTrackNewSignal(signal);
        }
    }

    public void closeAndStop(Signal signal){
        dm.closeAndStopTracking(signal);
    }

}
