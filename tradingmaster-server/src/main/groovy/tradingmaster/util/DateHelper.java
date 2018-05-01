package tradingmaster.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateHelper {


    public static ZonedDateTime toUtcZonedDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.of("UTC").normalized());

    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}
