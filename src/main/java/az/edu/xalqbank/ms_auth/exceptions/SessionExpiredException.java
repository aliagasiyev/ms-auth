package az.edu.xalqbank.ms_auth.exceptions;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(String message) {
        super(message);
    }
}
