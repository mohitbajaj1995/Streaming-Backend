package com.easyliveline.streamingbackend.exceptions;

import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.easyliveline.streamingbackend.models.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Common method for error response creation
    private ResponseEntity<ApiResponse<ErrorResponse>> buildErrorResponse(String errorCode, String errorMessage, String details, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(errorCode, errorMessage, details);
        return ResponseEntity.status(status).body(ApiResponseBuilder.failure(errorCode, errorResponse));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleRuntimeException(RuntimeException ex) {
        // Log exception for debugging purposes
        // logger.error("RuntimeException: ", ex);
        return buildErrorResponse("RUNTIME_EXCEPTION", ex.getMessage(), "A runtime error occurred during the operation.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage(), "The requested resource could not be found.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse("INVALID_ARGUMENT", ex.getMessage(), "The provided argument is invalid.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleSlotNotFoundException(SlotNotFoundException ex) {
        return buildErrorResponse("SLOT_NOT_FOUND", ex.getMessage(), "Slot not assigned.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidQueryException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidQuery(InvalidQueryException ex) {
        return buildErrorResponse("INVALID_QUERY", ex.getMessage(), "Please check filters or sorting parameters.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInternalError(InternalServerErrorException ex) {
        return buildErrorResponse("INTERNAL_ERROR", ex.getMessage(), "Something went wrong while processing your request.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomQueryException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleQueryException(CustomQueryException ex) {
        return buildErrorResponse(
                ex.getErrorType(),
                ex.getMessage(),
                "Query could not be parsed or executed.",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MeetingNotAssignedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMeetingNotAssignedException(MeetingNotAssignedException ex) {
        return buildErrorResponse("MEETING_NOT_ASSIGNED", ex.getMessage(), "Meeting not assigned to slot.", HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMeetingNotAssignedException(InsufficientPointsException ex) {
        return buildErrorResponse("INSUFFICIENT_POINTS", ex.getMessage(), "Parent don't have enough points", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGeneralException(Exception ex) {
        return buildErrorResponse("INTERNAL_SERVER_ERROR", ex.getMessage(), "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ZakTokenNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleZakTokenNotFoundException(ZakTokenNotFoundException ex) {
        return buildErrorResponse("ZAK_TOKEN_NOT_FOUND", ex.getMessage(), "The ZAK token is missing for the sub-host.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNullPointerException(NullPointerException ex) {
        return buildErrorResponse("NULL_POINTER_EXCEPTION", ex.getMessage(), "Expected a value, but null was encountered.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PermissionDeniedDataAccessExceptionWithRole.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handlePermissionDeniedDataAccessException(PermissionDeniedDataAccessExceptionWithRole ex) {
        return buildErrorResponse("PERMISSION_DENIED", ex.getMessage(), "Permission denied for this operation.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoClassDefFoundError.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNoClassDefFoundError(NoClassDefFoundError ex) {
        return buildErrorResponse("NO_CLASS_DEF_FOUND_ERROR", ex.getMessage(), "Required class is missing or not found in the classpath.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RefundNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleRefundNotFound(RefundNotFoundException ex) {
        return buildErrorResponse("REFUND_NOT_FOUND", ex.getMessage(), "Refund ID not found in database", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse("USER_NOT_FOUND", ex.getMessage(), "Required user not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTenantError(TenantNotFoundException ex) {
        return buildErrorResponse("TENANT_NOT_FOUND", ex.getMessage(), "No matching tenant for subdomain", HttpStatus.BAD_REQUEST);
    }


//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDenied(AccessDeniedException ex) {
//        return buildErrorResponse(
//                "ACCESS_DENIED",
//                ex.getMessage(),
//                "You do not have permission for this operation.",
//                HttpStatus.FORBIDDEN
//        );
//    }
}