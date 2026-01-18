package dev.Fjc.ultraBans;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A class of utility methods. This just exists to make my life easier.
 */
public abstract class Util {

    private Util() {}

    private static final UltraBans plugin = UltraBans.getInstance();

    public static void info(String text) {
        plugin.getLogger().info(text);
    }

    public static void warn(String text) {
        plugin.getLogger().warning(text);
    }

    public static void err(String text) {
        plugin.getLogger().severe(text);
    }

    public static List<String> onlinePlayerNameList() {
        List<String> names = new ArrayList<>();
        plugin.getServer().getOnlinePlayers().forEach(
                action -> names.add(action.getName())
        );
        return names;
    }

    public static String formatDateTime(TemporalAccessor accessor) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(accessor);
    }

    /**
     * A simple parser to convert some input string into a valid {@link Duration}.
     * @param input The string to parse
     * @return A valid {@link Duration}
     * @throws IllegalArgumentException if the input is null, empty, or otherwise invalid
     */
    public static Duration parse(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("Bad input: " + input);

        input = input.strip();

        int index = 0;
        while (index < input.length() && Character.isDigit(input.charAt(index))) index++;

        if (index == 0 || index == input.length()) throw new IllegalArgumentException("Bad input: " + input);

        long value = Long.parseLong(input.substring(0, index));
        String type = input.substring(index);

        return switch (type) {
            case "ms" -> Duration.ofMillis(value);
            case "s" -> Duration.ofSeconds(value);
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            case "d" -> Duration.ofDays(value);
            default -> Duration.ZERO;
        };
    }
}
