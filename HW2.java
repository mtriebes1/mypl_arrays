/**
 * Author: S. Bowers
 * Assign: 2
 *
 * A simple test driver class for running and testing the MyPL lexer
 * implementation. Note that the test driver can be run with a test
 * file or without (where text is entered via standard input).
 */

import java.io.*;

public class HW2 {

  public static void main(String[] args) {
    try {
      // determine if file or standard in
      InputStream istream = System.in;
      if (args.length == 1) {
        istream = new FileInputStream(new File(args[0]));
      }
      // create the lexer
      Lexer lexer = new Lexer(istream);
      Token t = lexer.nextToken();
      while (t.type() != TokenType.EOS) {
        System.out.println(t);
        t = lexer.nextToken();
      }
      System.out.println(t);
    } catch (MyPLException e) {
      System.out.println(e);
      System.exit(1);
    } catch (FileNotFoundException e) {
      System.out.println("Unable to open file '" + args[0] + "'");
      System.exit(1);
    }
  }
  
}
