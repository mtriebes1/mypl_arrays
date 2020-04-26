/**
 * Author: S. Bowers
 * Assign: 2
 * 
 * Basic class for representing MyPL interpreter exceptions and
 * printing them as compile (interpreter) and runtime errors.
 */


public class MyPLException extends Exception {

  public MyPLException(String type, String message, int line, int column) {
    this.type = type;
    this.message = message;
    this.line = line;
    this.column = column;
  }

  @Override
  public String toString() {
    return type + " error: " + message + " at line " + line + " column " + column;
  }

  private String type;          // e.g., lexer, parser, etc.
  private String message;       // the error message
  private int line;             // line of error in source file
  private int column;           // column of error in source file
  
}
