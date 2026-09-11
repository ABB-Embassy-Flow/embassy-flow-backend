package az.abb.embassyflow.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex,
                                                            HttpServletRequest request,
                                                            Locale locale) {
        return toErrorResponse(ex.getStatus(), ex.getCode(), request, locale);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            InvalidFormatException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest request, Locale locale) {
        return toErrorResponse(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, request, locale);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest request,
                                                       Locale locale) {
        return toErrorResponse(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, request, locale);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex,
                                                         HttpServletRequest request,
                                                         Locale locale) {
        return toErrorResponse(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, request, locale);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException ex,
                                                   HttpServletRequest request,
                                                   Locale locale) {
        return toErrorResponse(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, request, locale);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request, Locale locale) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR, request, locale);
    }

    private ResponseEntity<ApiError> toErrorResponse(HttpStatus status, String code,
                                                     HttpServletRequest request, Locale locale) {
        String message = messageSource.getMessage("error." + code, null, code, locale);
        ApiError error = ApiError.of(status.value(), code, message, request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}