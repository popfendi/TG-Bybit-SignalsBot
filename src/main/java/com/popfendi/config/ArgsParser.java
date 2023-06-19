package com.popfendi.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// for parsing command line args into config
// if you're running in docker this is useful, otherwise you can just use application.properties
public class ArgsParser {

    private static String[] parseArg(String arg) {
        final String regex = "--([0-9\\w.]*)=(\\S*)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(arg);

        String[] array = new String[2];
        while (matcher.find()) {
            array[0] = matcher.group(1);
            array[1] = matcher.group(2);
        }
        return array;
    }

    public static void handleArgs(String[] args){
        for (String a:
        args) {
            String[] parsedArg = ArgsParser.parseArg(a);
            PropertiesLoader.getProperties().put(parsedArg[0], parsedArg[1]);
        }
    }
}
