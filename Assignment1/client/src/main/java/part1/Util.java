package part1;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Util {

  public static String getTimestamp() {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
            .withLocale( Locale.US )
            .withZone( ZoneId.systemDefault() );
    Instant instant = Instant.now();
    String output = formatter.format( instant );
    return output;
  }
}
