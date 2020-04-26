
public class ReturnStmt implements Stmt {

  public Token returnToken = null; // for empty return type error
  public Expr returnExpr = null; 

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}

