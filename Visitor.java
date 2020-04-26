
/**
 * Author: S. Bowers
 * Assign: 4
 * File: Visitor.java
 *
 * Visitor interface for MyPL AST.
 */


public interface Visitor {

  // statement list
  public void visit(StmtList node);

  // statements
  public void visit(VarDeclStmt node);
  public void visit(AssignStmt node);
  public void visit(ReturnStmt node);
  public void visit(IfStmt node);
  public void visit(WhileStmt node);
  public void visit(ForStmt node);
  public void visit(TypeDeclStmt node);
  public void visit(FunDeclStmt node);

  // expressions
  public void visit(Expr node);
  public void visit(LValue node);
  public void visit(SimpleTerm node);
  public void visit(ComplexTerm node);

  // rvalues
  public void visit(SimpleRValue node);
  public void visit(NewRValue node);
  public void visit(CallRValue node);
  public void visit(IDRValue node);
  public void visit(NegatedRValue node);

}    

