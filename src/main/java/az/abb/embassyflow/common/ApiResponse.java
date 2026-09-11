package az.abb.embassyflow.common;

import java.time.Instant;

public class ApiResponse<T> {

    private final T data;
    private final Instant timestamp;

    private ApiResponse(T data, Instant timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, Instant.now());
    }

    public T getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}