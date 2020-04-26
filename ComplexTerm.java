

public class ComplexTerm implements ExprTerm {

  public Expr expr = null;

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}

