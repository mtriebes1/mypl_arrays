
public class SimpleRValue implements RValue {

  public Token val = null;

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}
