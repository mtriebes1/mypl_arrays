
/**
 * Author: S. Bowers
 * Assign: 5
 * File: HW5.java
 *
 * Basic test driver for the type checker.
 */


import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class HW5 {

  public static void main(String[] args) {
    try {
      // determine if file or standard in
      InputStream istream = System.in;
      if (args.length == 1) {
        istream = new FileInputStream(new File(args[0]));
      }
      // create the lexer
      Lexer lexer = new Lexer(istream);
      // create the parser
      Parser parser = new Parser(lexer);
      StmtList stmtList = parser.parse();
      TypeChecker typeChecker = new TypeChecker();
      stmtList.accept(typeChecker);
    }
    catch (MyPLException e) {
      System.out.println(e);
      System.exit(1);
    }
    catch (FileNotFoundException e) {
      System.out.println("Unable to open file '" + args[0] + "'");
      System.exit(1);
    }
     
  }
  
}
