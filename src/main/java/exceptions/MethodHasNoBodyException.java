package exceptions;

@SuppressWarnings("serial")
public class MethodHasNoBodyException extends Exception {
    // Parameterless Constructor
    public MethodHasNoBodyException() {}

    // Constructor that accepts a message
    public MethodHasNoBodyException(String message) {
        super(message);
    }
}
