package ru.mentola.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for various utility methods.
 */
@UtilityClass
public class Util {
    /**
     * Parses object from string, if not parsed, return input.
     * @param input Input string.
     * @return Parsed object.
     */
    public Object parsePrimitiveObjectFromString(final @NotNull String input) {
        try { return Integer.parseInt(input);
        } catch (Exception ignore) { }
        try { return Double.parseDouble(input);
        } catch (Exception ignore) { }
        try { return Long.parseLong(input);
        } catch (Exception ignore) { }
        try { return Boolean.parseBoolean(input);
        } catch (Exception ignore) { }
        return input;
    }
}
