package flow.common.exception;

import flow.common.dto.ResponseApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation Exception: {}", errors);
        return ResponseEntity.badRequest()
                .body(ResponseApi.error("입력값이 올바르지 않습니다.", "VALIDATION_ERROR"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseApi<Void>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime Exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseApi.error("서버 내부 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseApi.error("예기치 못한 오류가 발생했습니다.", "UNEXPECTED_ERROR"));
    }
}