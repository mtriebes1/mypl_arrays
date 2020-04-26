/**
* Author: Alex Lloyd
* Homework: #3
* File: Parser.java
* 
* Recursive descent parser implementation for MyPL. The parser
* requires a lexer. Once a parser is created, the parse() method
* ensures the given program is syntactically correct. 
*/

import java.util.*;


public class Parser {
	
	private Lexer lexer; 
	private Token currToken = null;
	private boolean debug_flag = false;  // set to false to remove debug comments

	/** 
* Create a new parser over the given lexer.
*/
	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	/**
* Ensures program is syntactically correct. On error, throws a
* MyPLException.
*/
	public StmtList parse() throws MyPLException
	{
		StmtList Node = new StmtList();
		advance();
		stmts(Node);
		eat(TokenType.EOS, "expecting end of file");
		return Node;
	}


	/* Helper Functions */

	// sets current token to next token in stream
	private void advance() throws MyPLException {
		currToken = lexer.nextToken();
	}

	// checks that current token matches given type and advances,
	// otherwise creates an error with the given error message
	private void eat(TokenType t, String errmsg) throws MyPLException {
		if (currToken.type() == t)
		advance();
		else
		error(errmsg);
	}

	// generates an error message from the given message and throws a
	// corresponding MyPLException
	private void error(String errmsg) throws MyPLException {
		String s = errmsg + " found '" + currToken.lexeme() + "'";
		int row = currToken.row();
		int col = currToken.column();
		throw new MyPLException("Parser", errmsg, row, col);
	}

	// function to print a debug string if the debug_flag is set for
	// helping to diagnose/test the parser
	private void debug(String msg) {
		if (debug_flag)
		System.out.println(msg);
	}

	// helper function that returns true if the current token is an operator
	private boolean isOp(TokenType item) throws MyPLException {
		if(item == TokenType.PLUS || item == TokenType.MINUS || item == TokenType.DIVIDE ||
				item == TokenType.MULTIPLY || item == TokenType.MODULO || item == TokenType.AND ||
				item == TokenType.OR || item == TokenType.EQUAL || item == TokenType.LESS_THAN ||
				item == TokenType.GREATER_THAN || item == TokenType.LESS_THAN_EQUAL ||
				item == TokenType.GREATER_THAN_EQUAL || item == TokenType.NOT_EQUAL) {
			return true;
		}
		return false;
	}

	//helper funciotion that returns true if the current token type is a value
	private boolean isVal(TokenType item) throws MyPLException {
		if(item == TokenType.INT_VAL || item == TokenType.DOUBLE_VAL || item == TokenType.BOOL_VAL ||
				item == TokenType.CHAR_VAL || item == TokenType.STRING_VAL) {
			return true;
		}
		return false;
	}
	/* Recursive Descent Functions */

	// <stmts> ::= <stmt> <stmts> | epsilon
	private void stmts(StmtList Node) throws MyPLException {
		debug("<stmts>");
		if(currToken.type() != TokenType.EOS) {
			stmt(Node);
			stmts(Node);
		}
	}


	// <bstmts> ::= <bstmt> <bstmts> | epsilon
	private void bstmts(StmtList Node) throws MyPLException {
		debug("<bstmts>");
		if(!(currToken.type() == TokenType.ELIF || currToken.type() == TokenType.ELSE || 
					currToken.type() == TokenType.END || currToken.type() == TokenType.EOS)) {
			Node.stmts.add(bstmt(Node));
			bstmts(Node);
		}
	}

	//<stmt> ::= <tdecl> | <fdecl> | <bstmt>
	private Stmt stmt(StmtList Node) throws MyPLException {
		debug("<stmt>");
		Stmt stmtNode;
		if(currToken.type() == TokenType.FUN) {
			stmtNode = fdecl(Node);
		}
		else if(currToken.type() == TokenType.TYPE) {
			stmtNode = tdecl(Node);
		}
		else {
			stmtNode = bstmt(Node);
		}
		Node.stmts.add(stmtNode);
		return stmtNode;
	}

	//<bstmt> ::= <vdecl> | <assign> | <cond> | <while> | <for> | <expr> | <exit>
	private Stmt bstmt(StmtList Node) throws MyPLException {
		debug("<bstmt>");
		Stmt stmtNode;
		if(currToken.type() == TokenType.FOR){
			stmtNode = FOR(Node);
		}
		else if(currToken.type() == TokenType.WHILE) {
			stmtNode = WHILE(Node);
		}
		else if(currToken.type() == TokenType.RETURN) {
			stmtNode = exit(Node);
		}
		else if(currToken.type() == TokenType.SET) {
			stmtNode = assign(Node);
		}
		else if(currToken.type() == TokenType.IF) {
			stmtNode = cond(Node);
		}
		else if(currToken.type() == TokenType.VAR) {
			stmtNode = vdecl(Node);
		}
		else if(currToken.type() == TokenType.ARRAY){
			stmtNode = arrdecl(Node);
		}
		else {
			stmtNode = expr(Node);
		}
		return stmtNode;
	}

	//<arrdecl> ::= ARRAY (<dtype> | epsilon) LBRACKET <expr> RBRACKET <aitem>
	private ArrDeclStmt arrdecl(StmtList Node) throws MyPLException {
		ArrDeclStmt arrNode = new ArrDeclStmt();
		advance();
		if(currToken.type() != TokenType.LBRACKET){
			arrNode.arrType = currToken;
			dtype(Node);
		}
		eat(TokenType.LBRACKET, "expecting [");
		arrNode.arrSize = expr(Node);
		eat(TokenType.RBRACKET, "expecting ]");
		arrNode.arrName = currToken;
		eat(TokenType.ID, "expecting ID");
		eat(TokenType.ASSIGN, "expecting :=");
		arrNode.arrList = aitem(Node);
		return arrNode;
	}

	//<aitem> ::= (LBRACKET (<expr> (COMAA <expr>)*| epsilon) RBRACKET)
	private ArrayList<Expr> aitem(StmtList Node) throws MyPLException {
		ArrayList<Expr> item = new ArrayList<>();
		eat(TokenType.LBRACKET, "expecting [");
		if(currToken.type() != TokenType.RBRACKET){
			item.add(expr(Node));
			while(currToken.type() == TokenType.COMMA){
				advance();
				item.add(expr(Node));
			}
			eat(TokenType.RBRACKET, "expecting ]");
		}
		else{
			advance();
		}
		return item;
	}

	//<tdecl> ::= TYPE ID <vdecls> END
	private TypeDeclStmt tdecl(StmtList Node) throws MyPLException {
		debug("<tdecl>");
		TypeDeclStmt typeNode = new TypeDeclStmt();
		if(currToken.type() == TokenType.TYPE) {
			advance();
			typeNode.typeId = currToken;
		}
		else{
			eat(TokenType.TYPE, "expecting type");
		}
		eat(TokenType.ID, "expecting ID");
		typeNode.fields = vdecls(Node);
		eat(TokenType.END, "expecting exit");
		return typeNode;
	}

	//<vdecls> ::= <vdecl> <vdecls> | epsilon
	private ArrayList<VarDeclStmt> vdecls(StmtList Node) throws MyPLException {
		debug("<vdecls>"); 
		ArrayList<VarDeclStmt> listNode = new ArrayList<>();
		if((currToken.type() != TokenType.END)&(currToken.type() != TokenType.EOS)) {
			listNode.add(vdecl(Node));
			listNode.addAll(vdecls(Node));
		}
		return listNode;
	}

	//<fdecl> ::= FUN ( <dtype> | NIL ) ID LPAREN <params> RPAREN <bstmts> END
	private FunDeclStmt fdecl(StmtList Node) throws MyPLException {
		debug("<fdecl>");
		FunDeclStmt funNode = new FunDeclStmt();
		advance();
		if(currToken.type() != TokenType.NIL){
			funNode.returnType = currToken;
			dtype(Node);
		}
		else {
			funNode.returnType = currToken;
			eat(TokenType.NIL, "expecting NIL");
		}
		if(currToken.type() == TokenType.ID) {
			funNode.funName = currToken;
			advance();
		}
		else {
			eat(TokenType.ID, "expecting ID");
		}
		eat(TokenType.LPAREN, "expecting (");
		funNode.params.add(params(Node));
		while(currToken.type() == TokenType.COMMA) {
			eat(TokenType.COMMA, "expecting ,");
			funNode.params.add(params(Node));
		}
		eat(TokenType.RPAREN, "expecting )");
		StmtList list = new StmtList();
		bstmts(funNode.stmtList);
		eat(TokenType.END, "expecting end");
		return funNode;
	}

	//<params> ::= hdtypei ID ( COMMA hdtypei ID )* | epsilon
	private FunParam params(StmtList Node) throws MyPLException {
		debug("<params>");
		FunParam paramsNode = new FunParam();
		if(currToken.type() != TokenType.RPAREN) {
			paramsNode.paramType = currToken;
			dtype(Node);
			if(currToken.type() == TokenType.ID) {
				paramsNode.paramName = currToken;
				advance();
			}
			else {
				eat(TokenType.ID, "expecting ID");
			}
		} 
		return paramsNode;
	}

	//<dtype> ::= INT_TYPE | DOUBLE_TYPE | BOOL_TYPE | CHAR_TYPE | STRING_TYPE | ID
	private void dtype(StmtList Node) throws MyPLException {
		debug("<dtype>");
		if(currToken.type() == TokenType.INT_TYPE) {
			eat(TokenType.INT_TYPE, "expecting integer data");
		}
		else if(currToken.type() == TokenType.DOUBLE_TYPE) {
			eat(TokenType.DOUBLE_TYPE, "expecting double data");
		}
		else if(currToken.type() == TokenType.BOOL_TYPE) {
			eat(TokenType.BOOL_TYPE, "expecting boolean data");
		}
		else if(currToken.type() == TokenType.CHAR_TYPE) {
			eat(TokenType.CHAR_TYPE, "expecting character data");
		}
		else if(currToken.type() == TokenType.STRING_TYPE) {
			eat(TokenType.STRING_TYPE, "expecting string data");
		}
		else {
			eat(TokenType.ID, "expecting ID or data");
		}
	}

	//<exit> ::= RETURN ( <expr> | epsilon )
	private ReturnStmt exit(StmtList Node) throws MyPLException {
		debug("<exit>");
		ReturnStmt returnNode = new ReturnStmt();
		eat(TokenType.RETURN, "expecting return");
		if(isVal(currToken.type()) || currToken.type() == TokenType.ID || currToken.type() == TokenType.NIL){
			returnNode.returnToken = currToken;
			returnNode.returnExpr = expr(Node);
		}
		return returnNode;
	}

	//<vdecl> ::= VAR ( <dtype> | epsilon ) ID ASSIGN <expr>
	private VarDeclStmt vdecl(StmtList Node) throws MyPLException {
		debug("<vdecl>");
		VarDeclStmt varNode = new VarDeclStmt();
		eat(TokenType.VAR, "expecting var");
		if(currToken.type() != TokenType.ID){
			varNode.varType = currToken;
			dtype(Node);
		}
		varNode.varId = currToken;
		eat(TokenType.ID, "expecting ID");
		if(currToken.type() == TokenType.ID) {
			varNode.varType = varNode.varId;
			varNode.varId = currToken;
			advance();
		}
		eat(TokenType.ASSIGN, "expecting :=");
		varNode.varExpr = expr(Node);

		return varNode;
	}

	//<assign> ::= SET <lvalue> ASSIGN <expr>
	private AssignStmt assign(StmtList Node) throws MyPLException {
		debug("<assign>");
		AssignStmt assignNode = new AssignStmt();
		eat(TokenType.SET, "expecting set");
		assignNode.lhs = lvalue(Node);
		eat(TokenType.ASSIGN, "expecting :=");
		if(currToken.type() != TokenType.LBRACKET){
		assignNode.rhs = expr(Node);
		}else{
		assignNode.arrList = aitem();
		}
		return assignNode;
	}

	//<lvalue> ::= ID ( DOT ID )*
	private LValue lvalue(StmtList Node) throws MyPLException {
		debug("<lvalue>");
		LValue lNode = new LValue();
		lNode.path.add(currToken);
		eat(TokenType.ID, "expecting ID");
		while(currToken.type() == TokenType.DOT) {
			eat(TokenType.DOT, "expecting .");
			lNode.path.add(currToken);
			eat(TokenType.ID, "expecting ID");
		}
		return lNode;
	}

	//<cond> ::= IF <expr> THEN <bstmts> <condt> END
	private IfStmt cond(StmtList Node) throws MyPLException {
		debug("<cond>");
		IfStmt ifNode = new IfStmt();
		eat(TokenType.IF, "expecting if");
		ifNode.ifPart.boolExpr = expr(Node);
		eat(TokenType.THEN, "expecting then");
		bstmts(ifNode.ifPart.stmtList);
		if(currToken.type() != TokenType.END){
			ifNode = condt(Node, ifNode);
		}
		eat(TokenType.END, "expecting end");
		return ifNode;
	}

	//<condt> ::= ELIF <expr> THEN <bstmts> <condt> | ELSE <bstmts> | epsilon
	private IfStmt condt(StmtList Node, IfStmt ifNode) throws MyPLException {
		debug("<condt>");
		if(currToken.type() == TokenType.ELIF){
			IfStmt elifNode = new IfStmt();
			eat(TokenType.ELIF, "expecting elif");
			elifNode.ifPart.boolExpr = expr(Node);
			eat(TokenType.THEN, "expecting then");
			bstmts(elifNode.ifPart.stmtList);
			ifNode.elsifs.add(elifNode.ifPart);
			if(currToken.type() != TokenType.END){
				ifNode = condt(Node, ifNode);
			}
			return ifNode;
		}
		else{
			ifNode.hasElse = true;
			eat(TokenType.ELSE, "expecting else or elif or end");
			bstmts(ifNode.elseStmtList);
			return ifNode;
		}
	}

	//<while> ::= WHILE <expr> DO <bstmts> END
	private WhileStmt WHILE(StmtList Node) throws MyPLException {
		debug("<WHILE>");
		WhileStmt whileNode = new WhileStmt();
		eat(TokenType.WHILE, "expecting while");
		whileNode.boolExpr = expr(Node);
		eat(TokenType.DO, "expecting do");
		bstmts(whileNode.stmtList);
		eat(TokenType.END, "expecting end");
		return whileNode;
	}
	
	//<for> ::= FOR ID ASSIGN <expr> TO <expr> DO <bstmts> END
	private ForStmt FOR(StmtList Node) throws MyPLException {
		debug("<FOR>");
		ForStmt forNode = new ForStmt();
		eat(TokenType.FOR, "expecting for");
		forNode.var = currToken;
		eat(TokenType.ID, "expecting variable");
		eat(TokenType.ASSIGN, "expecting assign");
		forNode.startExpr = expr(Node);
		eat(TokenType.TO, "expecting to");
		forNode.endExpr = expr(Node);
		eat(TokenType.DO, "expecting do");
		bstmts(forNode.stmtList);
		eat(TokenType.END, "expecting end");
		return forNode;
	}

	//<expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <operator> <expr> | epsilon )
	private Expr expr(StmtList Node) throws MyPLException {
		debug("<expr>");
		Expr exprNode = new Expr();
		ComplexTerm termNode = new ComplexTerm();
		SimpleTerm simpleNode = new SimpleTerm();
		if(currToken.type() == TokenType.NOT) {
			advance();
			termNode.expr = expr(Node);
			exprNode.first = termNode;
			exprNode.negated = true;
		}
		else if(currToken.type() == TokenType.LPAREN) {
			advance();
			termNode.expr = expr(Node);
			exprNode.first = termNode;
			eat(TokenType.RPAREN, "expecting )");
		}
		else{
			simpleNode.rvalue = rvalue(Node);
			exprNode.first = simpleNode;
		}
		
		if(isOp(currToken.type())) {
			exprNode.operator = currToken;
			operator();
			exprNode.rest = expr(Node);
		}
		return exprNode;
	}

	//<operator> ::= PLUS | MINUS | DIVIDE | MULTIPLY | MODULO | AND | OR | EQUAL | LESS_THAN 
	//| GREATER_THAN | LESS_THAN_EQUAL | GREATER_THAN_EQUAL | NOT_EQUAL
	private void operator() throws MyPLException {
		debug("<operator>");
		if(isOp(currToken.type())){
			advance();
		}
		else {
			error("expecting operator");
		}
	}

	//<rvalue> ::= <pval> | NIL | NEW ID | <idrval> | NEG <expr>
	private RValue rvalue(StmtList Node) throws MyPLException {
		debug("<rvalue>");
		
		if(currToken.type() == TokenType.NIL) {
			SimpleRValue None = new SimpleRValue();
			None.val = currToken;
			advance();
			return None;
		}
		else if(currToken.type() == TokenType.NEW) {
			advance();
			NewRValue newNode = new NewRValue();
			newNode.typeId = currToken;
			eat(TokenType.ID, "expecting ID");
			return newNode;
		}
		else if(currToken.type() == TokenType.NEG) {
			advance();
			NegatedRValue negNode = new NegatedRValue();
			negNode.expr = expr(Node);
			return negNode;
		}
		else if(isVal(currToken.type())) {
			SimpleRValue simpNode = new SimpleRValue();
			simpNode.val = currToken;
			pval();
			return simpNode;
		}
		else {
			return idrval(Node);
		}

	}

	//<pval> ::= INT_VAL | DOUBLE_VAL | BOOL_VAL | CHAR_VAL | STRING_VAL
	private void pval() throws MyPLException {
		debug("<pval>");
		if(isVal(currToken.type())) {
			advance();
		}
		else {
			error("expecting value");
		}
	}

	//<idrval> ::= ID ( DOT ID )* | ID LPAREN <exprlist> RPAREN
	private RValue idrval(StmtList Node) throws MyPLException {
		debug("<idrval>");
		IDRValue idNode = new IDRValue();
		CallRValue callNode = new CallRValue();
		idNode.path.add(currToken);
		callNode.funName = currToken;
		eat(TokenType.ID, "expecting ID");
		if(currToken.type() == TokenType.LPAREN){
			advance();
			callNode = exprlist(Node, callNode);
			eat(TokenType.RPAREN, "expecting )");
			return callNode;
		}
		else {
			while(currToken.type() == TokenType.DOT){
				advance();
				idNode.path.add(currToken);
				eat(TokenType.ID, "expecitng ID");
			}
			return idNode;
		}
	}

	//<exprlist> ::= <expr> ( COMMA <expr> ) | epsilon
	private CallRValue exprlist(StmtList Node, CallRValue callNode) throws MyPLException {
		debug("<exprlist>");
		if(currToken.type() != TokenType.RPAREN) {
			callNode.argList.add(expr(Node));
			while(currToken.type() == TokenType.COMMA) {
				advance();
				callNode.argList.add(expr(Node));
			}
		}
		return callNode;
	}


}
