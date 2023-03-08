package lunch.record.repository.exception;

public class LunchRecordDbException extends RuntimeException {

    public LunchRecordDbException() {
    }

    public LunchRecordDbException(String message) {
        super(message);
    }

    public LunchRecordDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public LunchRecordDbException(Throwable cause) {
        super(cause);
    }
}
