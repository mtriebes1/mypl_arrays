
public class NewRValue implements RValue {

  public Token typeId = null;

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
  
}

