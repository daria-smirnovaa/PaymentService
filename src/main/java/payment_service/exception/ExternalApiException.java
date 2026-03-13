package payment_service.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException() {
        super("External API service unavailable");
    }

    public ExternalApiException(String message) {
        super(message);
    }
}
