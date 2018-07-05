package exceptions;

@SuppressWarnings("serial")
public class DotfileDoesNotExist extends Exception {
  // Parameterless Constructor
  public DotfileDoesNotExist() {}

  // Constructor that accepts a message
  public DotfileDoesNotExist(String message) {
        super(message);
    }
}
