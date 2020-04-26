
public class SimpleTerm implements ExprTerm {

  public RValue rvalue = null;

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}
