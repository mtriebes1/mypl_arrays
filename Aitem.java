import java.util.ArrayList;
public class Aitem implements RValue {

    public ArrayList<Expr> items = new ArrayList<>();

    public void accept(Visitor visitor) throws MyPLException {
      visitor.visit(this);
    }
  
  }
  