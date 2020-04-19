/** 
 * Author: S. Bowers
 * Homework: #3
 * File: HW3.java
 * 
 * Basic driver program for running the MyPL Parser
 * implementation. Can be used from standard input (one statement per
 * line) or over a given input file (e.g., java HW3 p1.mypl).
 */

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class HW3 {

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
      parser.parse();
    } catch (MyPLException e) {
      System.out.println(e);
      System.exit(1);
    } catch (FileNotFoundException e) {
      System.out.println("Unable to open file '" + args[0] + "'");
      System.exit(1);
    }
  }
  
}
