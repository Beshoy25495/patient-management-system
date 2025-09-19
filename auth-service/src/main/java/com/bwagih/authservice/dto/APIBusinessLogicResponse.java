package com.bwagih.authservice.dto;

import com.bwagih.authservice.dto.enums.APIBusinessLogicResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIBusinessLogicResponse<T> {

    private String replyCode;

    private String replyMessage;

    @Builder.Default
    private APIBusinessLogicResponseStatus status = APIBusinessLogicResponseStatus.SUCCESS; // SUCCESS or ERROR

    private T result;
    private Instant timestamp;



    /**
     * Create a success response with default message and code without result
     */
    public static <T> APIBusinessLogicResponse<T> success() {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode("0000")
                .replyMessage("Operation Done Successfully")
                .status(APIBusinessLogicResponseStatus.SUCCESS)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a success response
     */
    public static <T> APIBusinessLogicResponse<T> success(T result) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode("0000")
                .replyMessage("Operation Done Successfully")
                .status(APIBusinessLogicResponseStatus.SUCCESS)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a success response with custom message
     */
    public static <T> APIBusinessLogicResponse<T> success(String message, T result) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode("0000")
                .replyMessage(message)
                .status(APIBusinessLogicResponseStatus.SUCCESS)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a success response with custom code and message without result
     */
    public static <T> APIBusinessLogicResponse<T> success(String replyCode, String message) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode(replyCode)
                .replyMessage(message)
                .status(APIBusinessLogicResponseStatus.SUCCESS)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a success response with custom code and message
     */
    public static <T> APIBusinessLogicResponse<T> success(String replyCode, String message, T result) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode(replyCode)
                .replyMessage(message)
                .status(APIBusinessLogicResponseStatus.SUCCESS)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> APIBusinessLogicResponse<T> error() {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode("ERR0000")
                .replyMessage("Operation Failed")
                .status(APIBusinessLogicResponseStatus.ERROR)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with custom message
     */
    public static <T> APIBusinessLogicResponse<T> error(String replyMessage) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode("ERR0000")
                .replyMessage(replyMessage)
                .status(APIBusinessLogicResponseStatus.ERROR)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with custom code and message
     */
    public static <T> APIBusinessLogicResponse<T> error(String replyCode, String replyMessage) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode(replyCode)
                .replyMessage(replyMessage)
                .status(APIBusinessLogicResponseStatus.ERROR)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with custom data
     */
    public static <T> APIBusinessLogicResponse<T> error(String replyCode, String replyMessage, T result) {
        return APIBusinessLogicResponse.<T>builder()
                .replyCode(replyCode)
                .replyMessage(replyMessage)
                .status(APIBusinessLogicResponseStatus.ERROR)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }
}
