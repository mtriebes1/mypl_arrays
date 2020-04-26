
import java.util.ArrayList;


public class LValue implements ASTNode {

  public ArrayList<Token> path = new ArrayList<>();

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}

