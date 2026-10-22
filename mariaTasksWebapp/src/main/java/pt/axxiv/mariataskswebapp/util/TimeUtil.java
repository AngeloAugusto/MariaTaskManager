package pt.axxiv.mariataskswebapp.util;

import java.time.*;
import java.util.Date;

public class TimeUtil {

    public static Date localTimeToDate(LocalTime localTime) {
        // Combine with today's date
        LocalDate today = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(today, localTime);

        // Convert to ZonedDateTime using system default zone
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());

        // Convert to Instant, then to Date
        return Date.from(zonedDateTime.toInstant());
    }
}
