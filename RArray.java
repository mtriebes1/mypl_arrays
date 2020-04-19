
import java.util.ArrayList;

public class RArray implements RValue {

    public Token arrType = null;
    public Token arrName = null;
    public int arrSize = null;
    public ArrayList<Expr> arrList = new ArrayList<>();

    public void accept(Visitor visitor) throws MyPLException {
        visitor.visit(this);
      }
}