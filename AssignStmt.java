

public class AssignStmt implements Stmt {

  public LValue lhs = null;
  public Expr rhs = null;
  
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}

