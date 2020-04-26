/**
* Author: Alex Lloyd, Nick Mooney, Matthew Triebes
* Assign: Lexer
*
* The lexer implementation tokenizes a given input stream. The lexer
* implements a pull-based model via the nextToken function such that
* each call to nextToken advances the lexer to the next token (which
* is returned by nextToken). The file has been completed read when
* nextToken returns the EOS token. Lexical errors in the source file
* result in the nextToken function throwing a MyPL Exception.
*/

import java.util.*;
import java.io.*;


public class Lexer {

	private BufferedReader buffer; // handle to input stream
	private int line;
	private int column;

	/** 
*/
	public Lexer(InputStream instream) {
		buffer = new BufferedReader(new InputStreamReader(instream));
		this.line = 1;
		this.column = 0;
	}


	/**
* Returns next character in the stream. Returns -1 if end of file.
*/
	private int read() throws MyPLException {
		try {
			int ch = buffer.read();
			return ch;
		} catch(IOException e) {
			error("read error", line, column + 1);
		}
		return -1;
	}


	/** 
* Returns next character without removing it from the stream.
*/
	private int peek() throws MyPLException {
		int ch = -1;
		try {
			buffer.mark(1);
			ch = read();
			buffer.reset();
		} catch(IOException e) {
			error("read error", line, column + 1);
		}
		return ch;
	}


	/**
* Print an error message and exit the program.
*/
	private void error(String msg, int line, int column) throws MyPLException {
		throw new MyPLException("Lexer", msg, line, column);
	}


	/**
*/
	public Token nextToken() throws MyPLException {
		String out = "";
		int temp;
		char tempC;
		
		while(peek() == ' ' || peek() == '\n' || peek() == '\r' || peek() == '#' || peek() == '\t'){
			if(peek() == ' ' || peek() == '\t'){
				column++;
				read();
			}
			else if(peek() == '#') {
				column = 0;
				line++;
				while(peek() != '\n' && peek() != -1){
					read();
				}
				read();
			}
			else if( peek() == '\r') {
				read();
			}
			else {
				column = 0;
				line++;
				read();
			}
		}

		if(peek()==-1){
			return new Token(TokenType.EOS, "", line, column);
		}
		char finder = (char)peek();
		switch(finder){
		case ',': 
			out += (char)read();
			column++;
			return new Token(TokenType.COMMA, out, line, column);
		case '.': 
			out += (char)read();
			column++;
			return new Token(TokenType.DOT, out, line, column);
		case '+': 
			out += (char)read();
			column++;
			return new Token(TokenType.PLUS, out, line, column);
		case '-': 
			out += (char)read();
			column ++;
			return new Token(TokenType.MINUS, out, line, column);
		case '*': 
			out += (char)read();
			column ++;
			return new Token(TokenType.MULTIPLY,out, line, column);
		case '/': 
			out += (char)read();
			column ++;
			return new Token(TokenType.DIVIDE,out, line, column);
		case '%':
			out += (char)read();
			column ++;
			return new Token(TokenType.MODULO, out, line, column);
		case '=':
			out += (char)read();
			column ++;
			return new Token(TokenType.EQUAL, out, line, column);
		case '(':
			out += (char)read();
			column ++;
			return new Token(TokenType.LPAREN, out, line, column);
		case ')':
			out += (char)read();
			column ++;
			return new Token(TokenType.RPAREN, out, line, column);
		case '[':
			out += (char)read();
			column++;
			return new Token(TokenType.LBRACKET, out, line, column); // added for arrays
		case ']':
			out += (char)read();
			column++;
			return new Token(TokenType.RBRACKET, out, line, column); // added for arrays
		case '>':
			out += (char)read();
			column++;
			if(peek() == '='){
				out += (char)read();
				column++;
				return new Token(TokenType.GREATER_THAN_EQUAL,out, line, column);
			}
			return new Token(TokenType.GREATER_THAN, out, line, column);
		case '<':
			out += (char)read();
			column++;
			temp = column;
			if(peek() == '='){
				out += (char)read();
				column++;
				return new Token(TokenType.LESS_THAN_EQUAL, out, line, temp);
			}
			return new Token(TokenType.LESS_THAN, out, line, column);
			case':':
			out += (char)read();
			column++;
			temp = column;
			if(peek() == '='){
				out += (char)read();
				column++;
				return new Token(TokenType.ASSIGN,out, line, temp);
			}
			error("unexpected symbol " + out, line, column);
			
			case'!':
			out += (char)read();
			column++;
			if(peek() == '='){
				out+= (char)read();
				column++;
				return new Token(TokenType.NOT_EQUAL, out, line, column);
			}
			error("unexpected symbol" + out, line, column);
		default:
			break;
		}
		
		if((peek() >= 'A' && peek() <= 'Z') || (peek() >= 'a' && peek() <= 'z')){
			temp = 1 + column;
			while((peek() >= 'a' && peek() <='z')||(peek() >= 'A' && peek() <='Z')||(peek() >= '0' && peek() <='9')||(peek() =='_')){
				out += (char)read();
				column++;
			}
			switch (out){
			case "int":
				return new Token(TokenType.INT_TYPE, out, line, temp);
			case "double":
				return new Token(TokenType.DOUBLE_TYPE,out, line, temp);
			case "char":
				return new Token(TokenType.CHAR_TYPE, out, line, temp);
			case "string":
				return new Token(TokenType.STRING_TYPE, out, line, temp);
			case "bool":
				return new Token(TokenType.BOOL_TYPE, out, line, temp);
			case "array":
				return new Token(TokenType.ARRAY, out, line, temp); // added in for arrays
			case "type":
				return new Token(TokenType.TYPE, out, line, temp);
			case "and":
				return new Token(TokenType.AND, out, line, temp);
			case "or":
				return new Token(TokenType.OR, out, line, temp);
			case "not":
				return new Token(TokenType.NOT, out, line, temp);
			case "neg":
				return new Token(TokenType.NEG, out, line, temp);
			case "while":
				return new Token(TokenType.WHILE, out, line, temp);
			case "for":
				return new Token(TokenType.FOR, out, line, temp);
			case "to":
				return new Token(TokenType.TO, out, line, temp);
			case "do":
				return new Token(TokenType.DO, out, line, temp);
			case "if":
				return new Token(TokenType.IF, out, line, temp);
			case "then":
				return new Token(TokenType.THEN, out, line, temp);
			case "else":
				return new Token(TokenType.ELSE, out, line, temp);
			case "elif":
				return new Token(TokenType.ELIF, out, line, temp);
			case "end":
				return new Token(TokenType.END, out, line, temp);
			case "fun":
				return new Token(TokenType.FUN, out, line, temp);
			case "var":
				return new Token(TokenType.VAR, out, line, temp);
			case "set":
				return new Token(TokenType.SET, out, line, temp);
			case "return":
				return new Token(TokenType.RETURN, out, line, temp);
			case "new":
				return new Token(TokenType.NEW, out, line, temp);
			case "nil":
				return new Token(TokenType.NIL, out, line, temp);
			case "false":
				return new Token(TokenType.BOOL_VAL, out, line, temp);
			case "true":
				return new Token(TokenType.BOOL_VAL, out, line, temp);
			default:
				return new Token(TokenType.ID, out, line, temp);
			}
		}
		
		if(peek() == 39){
			read();
			column++;
			temp = column;
			if(peek() == '\n' || peek() == '\r'){
				error("found newline within string", line, column);
			}
			out += (char)read();
			column++;
			if(peek() == '\n' || peek() == '\r'){
				error("found newline within string", line, column);
			}
			read();
			column++;
			return new Token(TokenType.CHAR_VAL, out, line, temp);
		}
		
		if(peek() == 34){
			read();
			column++;
			temp = column;
			while(peek() != 34){
				out += (char)read();
				column++;
				if(peek() == '\n'){
					error("found newline within string", line, column+1);
				}
			}
			read();
			column++;
			return new Token(TokenType.STRING_VAL, out, line, temp);
		}
		

		
		if(peek() >= 48 && peek() <= 57){
			boolean zero = peek() == 48;
			out += (char)read();
			column++;
			temp = column;
			while(peek() >= 48 && peek() <= 57){
				out += (char)read();
				column++;
			}
			if(out.length() == 1){
				zero = false;
			}
			if(peek() == '.'){
				out += (char)read();
				if(!(peek() >= '0' && peek() <= '9')){
					error("missing digit in float '" + out + "'", line, temp);
				}
				column++;
				while(peek() >= 48 && peek() <= 57){
					out += (char)read();
					column++;
				}
				if(zero){
					error("leading zero in " + out, line, temp);
				}
				if((peek() >= 'A' && peek() <= 'Z') || (peek() >= 'a' && peek() <= 'z')){
					column++;
					tempC = (char)peek();
					error("unexpected symbol '" + tempC + "'", line, column);
				}
				return new Token(TokenType.DOUBLE_VAL, out, line, temp);
			}
			if(zero){
				error("leading zero in " + out, line, temp);
			}
			if((peek() >= 'A' && peek() <= 'Z') || (peek() >= 'a' && peek() <= 'z')){
				column++;
				tempC = (char)peek();
				error("unexpected symbol '" + tempC +"'", line, column);
			}
			return new Token(TokenType.INT_VAL, out, line, temp);
		}
		
		
		column++;
		out += (char)read();
		while(peek() != ' ' && peek() != '\n' && peek() != '\r'&& peek() != -1){
			out += (char)read();
			column++;
		}
		error("unexpected symbol1 " + out, line, column);
		return new Token(TokenType.EOS, "",line, column);
	}


}
