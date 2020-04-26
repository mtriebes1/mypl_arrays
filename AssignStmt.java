
import java.util.ArrayList;

public class AssignStmt implements Stmt {

  public LValue lhs = null;
  public Expr rhs = null;
    public ArrayList<Expr> arrList = new ArrayList<>();
  
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}

