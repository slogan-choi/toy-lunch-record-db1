package lunch.record.repository.exception;

public class ValueTooLongException extends LunchRecordDbException {

    private int errorCode;

    public ValueTooLongException() {
    }

    public ValueTooLongException(String message) {
        super(message);
    }

    public ValueTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueTooLongException(Throwable cause) {
        super(cause);
    }

    public ValueTooLongException(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
