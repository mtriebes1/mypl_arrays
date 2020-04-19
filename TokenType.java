/**
 * Authors: Matthew Triebes, Alex Lloyd, Nick Mooney
 * Assign: 2
 *
 * The set of token types in MyPL.  Note that EOS denotes the "end of
 * stream", which is used to signal the end of the source file. Token
 * types with VAL denote value types (e.g., 42), whereas token types
 * with TYPE denote corresponding types (e.g., the type int). The
 * token type TYPE denotes the 'type' reserved word used when defining
 * structured types.
 */

public enum TokenType {
  // basic symbols
  COMMA, //done
  DOT, //done
  PLUS, //done
  MINUS, //done
  MULTIPLY, //done
  DIVIDE, //done
  MODULO, //done
  EQUAL, //done
  GREATER_THAN, //done
  GREATER_THAN_EQUAL,
  LESS_THAN, //done
  LESS_THAN_EQUAL,
  NOT_EQUAL, //done
  LPAREN, //done
  RPAREN, //done
  LBRACKET, //added for arrays
  RBRACKET, //added for arrays
  ASSIGN, // done
  EOS, // done
 
  // data types
  INT_VAL, //done
  DOUBLE_VAL, //done
  CHAR_VAL, //done
  STRING_VAL, //done
  BOOL_VAL,
  
  // reserved words
  INT_TYPE, //done
  BOOL_TYPE, //done
  DOUBLE_TYPE, //done
  CHAR_TYPE, //done
  STRING_TYPE, //done
  TYPE, //done
  AND, //done 
  OR, //done
  NOT, //done
  NEG, //done
  WHILE, //done
  FOR, //done
  TO, //done
  DO, //done
  IF, //done
  THEN, //done
  ELSE, //done
  ELIF, //done
  END, //done
  FUN, //done
  VAR, //done
  SET, //done
  RETURN, //done
  NEW, //done
  NIL, //done
  GET, //added for arrays
  LEN, //added for arrays
  ARRAY,
  // identifiers
  ID
}
