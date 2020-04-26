import java.util.ArrayList;

public class ArrDeclStmt implements Stmt {

    public Token arrName = null;
    public Token arrType = null;
    public Expr arrSize = null;
    public ArrayList<Expr> arrList = new ArrayList<>();

    public void accept(Visitor visitor) /*throws MyPLException*/ {
        visitor.visit(this);
    }
}