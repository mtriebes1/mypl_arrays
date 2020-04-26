
import java.util.ArrayList;

public class ArrDeclStmt implements Stmt {

    public Token arrName = null;
    public Token arrType = null;
    public Expr arrSize = null;
    public Expr arrList = null;

    public void accept(Visitor visitor) {
        visitor.visit(this);
      }
}