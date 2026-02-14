/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.cubespace.geSuit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chinwe
 */
public class TimeParser
{
    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)([wdhms])");
    private static final int SECOND = 1;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;
    private static final int DAY = HOUR * 24;
    private static final int WEEK = DAY * 7;

    /**
     * Parse a string input into seconds, using w(eeks), d(ays), h(ours), m(inutes) and s(econds) For example: 4d8m2s -&gt; 4 days, 8 minutes and 2 seconds
     *
     * @param string String to convert to Seconds
     * @return Seconds
     */
    public static int parseString(String string)
    {
        Matcher m = TIME_PATTERN.matcher(string);
        int total = 0;
        while (m.find()) {
            int amount = Integer.valueOf(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 's':
                    total += amount * SECOND;
                    break;
                case 'm':
                    total += amount * MINUTE;
                    break;
                case 'h':
                    total += amount * HOUR;
                    break;
                case 'd':
                    total += amount * DAY;
                    break;
                case 'w':
                    total += amount * WEEK;
                    break;
            }
        }
        return total;
    }

    public static long parseStringtoMillisecs(String string) {
        int total = parseString(string);
        return total * 1000;
    }

}
