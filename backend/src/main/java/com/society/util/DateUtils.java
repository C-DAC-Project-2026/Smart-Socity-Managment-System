package com.society.util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public final class DateUtils {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private DateUtils() {}
    public static String format(LocalDateTime dt) {
        return dt != null ? dt.format(FMT) : "";
    }
}
