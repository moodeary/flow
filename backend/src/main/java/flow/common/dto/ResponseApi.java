package flow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseApi<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> ResponseApi<T> success(T data) {
        return ResponseApi.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }

    public static <T> ResponseApi<T> success(T data, String message) {
        return ResponseApi.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseApi<T> success() {
        return ResponseApi.<T>builder()
                .success(true)
                .message("성공")
                .build();
    }

    public static <T> ResponseApi<T> error(String message) {
        return ResponseApi.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ResponseApi<T> error(String message, String errorCode) {
        return ResponseApi.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}