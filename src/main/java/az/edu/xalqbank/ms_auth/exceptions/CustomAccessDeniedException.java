package az.edu.xalqbank.ms_auth.exceptions;

public class CustomAccessDeniedException extends RuntimeException {
  public CustomAccessDeniedException(String message) {
    super(message);
  }
}
