
/**
* Author: Alex Lloyd
* Assign: 4
* File: PrintVisitor.java
*
* Print Visitor skeleton code for MyPL AST.
*/


import java.io.PrintStream;


public class PrintVisitor implements Visitor {
	private PrintStream out;      // the output stream for printing
	private int indent = 0;       // the current indent level (num spaces)

	// indent helper functions

	// to get a string with the current indentation level (in spaces)
	private String getIndent() {
		return " ".repeat(indent);
	}

	// to increment the indent level
	private void incIndent() {
		indent += 2;
	}

	// to decrement the indent level
	private void decIndent() {
		indent -= 2;
	}

	// visitor functions

	public PrintVisitor(PrintStream printStream) {
		this.out = printStream;
	}

	public void visit(StmtList node) {
		// iterate through each statement list node and delegate
		for (Stmt s : node.stmts) {
			out.print(getIndent());
			s.accept(this);
			out.println();
		}
	}


	public void visit(VarDeclStmt node) {
		out.print("var ");
		if (node.varType != null)
		out.print(node.varType.lexeme() + " ");
		out.print(node.varId.lexeme() + " := ");
		node.varExpr.accept(this);
	}

	public void visit(AssignStmt node) {
		out.print("set ");
		node.lhs.accept(this);
		out.print(" := ");
		node.rhs.accept(this);
	}

	public void visit(ReturnStmt node) {
		out.print("return ");
		if(node.returnToken != null){
			node.returnExpr.accept(this);
		}
	}

	public void visit(IfStmt node) {
		out.print("if ");
		node.ifPart.boolExpr.accept(this);
		out.print(" then\n");
		incIndent();
		node.ifPart.stmtList.accept(this);
		decIndent();
		for(BasicIf s : node.elsifs) {
			out.print(getIndent());
			out.print("elif ");
			s.boolExpr.accept(this);
			out.print(" then\n");
			incIndent();
			s.stmtList.accept(this);
			decIndent();
		}
		if(node.elseStmtList != null && node.hasElse) {
			out.print(getIndent());
			out.print("else\n");
			incIndent();
			node.elseStmtList.accept(this);
			decIndent();
			
		}
		out.print(getIndent());
		out.print("end");
	}

	public void visit(WhileStmt node) {
		out.print("while ");
		node.boolExpr.accept(this);
		out.println(" do ");
		incIndent();
		node.stmtList.accept(this);
		decIndent();
		out.print(getIndent());
		out.print("end");
	}

	public void visit(ForStmt node) {
		out.print("for " + node.var.lexeme() + " ");
		node.startExpr.accept(this);
		out.print(" to ");
		node.endExpr.accept(this);
		out.println(" do");
		incIndent();
		node.stmtList.accept(this);
		decIndent();
		out.print(getIndent());
		out.print("end");
	}

	public void visit(TypeDeclStmt node) {
		out.println();
		out.println("type " + node.typeId.lexeme());
		incIndent();
		
		for(Stmt s : node.fields) {
			out.print(getIndent());
			s.accept(this);
			out.println();
		}
		decIndent();
		out.println("end");
	}

	public void visit(FunDeclStmt node) {
		out.println();
		out.print("fun " + node.returnType.lexeme() + " ");
		out.print(node.funName.lexeme() + "(");
		for(int i = 0; i < node.params.size() - 1; ++i) {
			out.print(node.params.get(i).paramType.lexeme() + " ");
			out.print(node.params.get(i).paramName.lexeme());
			out.print(", ");
		}
		out.print(node.params.get(node.params.size() - 1).paramType.lexeme() + " ");
		out.print(node.params.get(node.params.size() - 1).paramName.lexeme());
		out.println(")");
		incIndent();
		node.stmtList.accept(this);
		decIndent();
		out.print(getIndent());
		out.println("end");
	}

	public void visit(Expr node) {
		if(node.negated) {
			out.print("not ");
		}
		if(node.operator != null) {
			out.print("(");
		}
		
		node.first.accept(this);
		
		if(node.operator != null){
			out.print(" " + node.operator.lexeme() + " ");
			node.rest.accept(this);
			out.print(")");
		}
	}

	public void visit(LValue node) {
		for(int i = 0; i < node.path.size() - 1; ++i) {
			out.print(node.path.get(i).lexeme());
			out.print(".");
		}
		out.print(node.path.get(node.path.size() - 1).lexeme());
	}

	public void visit(SimpleTerm node) {
		node.rvalue.accept(this);
	}

	public void visit(ComplexTerm node) {
		node.expr.accept(this);
	}

	public void visit(SimpleRValue node) {
		if(node.val.type() == TokenType.STRING_VAL){
			out.print("\"");
		}
		out.print(node.val.lexeme());
		if(node.val.type() == TokenType.STRING_VAL){
			out.print("\"");
		}
	}

	public void visit(NewRValue node) {
		out.print(node.typeId.lexeme());
	}

	public void visit(CallRValue node) {
		out.print(node.funName.lexeme() + "(");
		for(int i = 0; i < node.argList.size() - 1; ++i) {
			node.argList.get(i).accept(this);
			out.print(", ");
		}
		node.argList.get(node.argList.size() - 1).accept(this);
		out.print(")");
	}

	public void visit(IDRValue node) {
		for(int i = 0; i < node.path.size() - 1; ++i) {
			out.print(node.path.get(i).lexeme());
			out.print(".");
		}
		out.print(node.path.get(node.path.size() - 1).lexeme());
	}

	public void visit(NegatedRValue node) {
		out.print("neg ");
		node.expr.accept(this);
	}


}    

