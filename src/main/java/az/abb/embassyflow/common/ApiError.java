package az.abb.embassyflow.common;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record ApiError(int status, String code, String message, String timestamp, String path) {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(status, code, message, TIMESTAMP_FORMATTER.format(Instant.now()), path);
    }
}