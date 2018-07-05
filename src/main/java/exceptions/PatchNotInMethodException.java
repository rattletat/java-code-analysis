package exceptions;

@SuppressWarnings("serial")
public class PatchNotInMethodException extends Exception {
    // Parameterless Constructor
    public PatchNotInMethodException() {}

    // Constructor that accepts a message
    public PatchNotInMethodException(String message) {
        super(message);
    }
}
