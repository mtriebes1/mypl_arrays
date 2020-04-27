/**
 * Author: Alex Lloyd
 * Assign: 7
 * File: Interpreter.java
 *
 * Visitor implementation of a basic "Pure AST" Interpreter for MyPL. 
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Interpreter implements Visitor {
  private SymbolTable symbolTable = new SymbolTable();
  private Object currVal = null;

  private Map<Integer,Map<String,Object>> heap = new HashMap<>();

  public Integer run(StmtList stmtList) throws MyPLException {
    try {
      stmtList.accept(this);
      return 0;
      }
    catch (MyPLException e) {
      if (!e.isReturnException())
        throw e;
      Object returnVal = e.getReturnValue();
      if (returnVal == null)
        return 0;
      return (Integer)returnVal;
    }
  }

  
  // visitor functions

  
  public void visit(StmtList node) throws MyPLException {
    symbolTable.pushEnvironment();
    for (Stmt s : node.stmts) {
      s.accept(this);
    }
    symbolTable.popEnvironment();    
  }

  
  public void visit(VarDeclStmt node) throws MyPLException {
    node.varExpr.accept(this);
    symbolTable.addName(node.varId.lexeme());
    symbolTable.setInfo(node.varId.lexeme(), currVal);
  }

  
  public void visit(AssignStmt node) throws MyPLException {
    // evaluate rhs
    node.rhs.accept(this);
    // let LValue do the assignment
    node.lhs.accept(this);
  }

  
  public void visit(ReturnStmt node) throws MyPLException {
    if(node.returnExpr != null){
    node.returnExpr.accept(this);
    }
    Object returnVal = currVal;
    throw new MyPLException(returnVal);
  }

  
  public void visit(IfStmt node) throws MyPLException {
    // TODO: HW6
    node.ifPart.boolExpr.accept(this);
    if((Boolean)currVal){
      node.ifPart.stmtList.accept(this);
    }
    else{
      for(int i = 0; i < node.elsifs.size(); i++){
        node.elsifs.get(i).boolExpr.accept(this);
        if((Boolean)currVal){
          node.elsifs.get(i).stmtList.accept(this);
          currVal = true;
          break;
        }
      }
      if(!(Boolean)currVal && node.hasElse){
        node.elseStmtList.accept(this);
      }
    }

  }

  
  public void visit(WhileStmt node) throws MyPLException {
    node.boolExpr.accept(this);
    while((Boolean)currVal){
      node.stmtList.accept(this);
      node.boolExpr.accept(this);
    }
  }

  
  public void visit(ForStmt node) throws MyPLException {
    node.endExpr.accept(this);
    int end = (Integer)currVal;
    symbolTable.addName(node.var.lexeme());
    node.startExpr.accept(this);
    symbolTable.setInfo(node.var.lexeme(), currVal);
    int start = (Integer)currVal;
    while(start <= end){
      node.stmtList.accept(this);
      start = (Integer)symbolTable.getInfo(node.var.lexeme()) + 1;
      symbolTable.setInfo(node.var.lexeme(), start);
    }
  }


  public void visit(TypeDeclStmt node) throws MyPLException {
    int envId = symbolTable.getEnvironmentId();
    List<Object> typeInfo = List.of(envId, node);
    symbolTable.addName(node.typeId.lexeme());
    symbolTable.setInfo(node.typeId.lexeme(), typeInfo);
  }





  public void visit(FunDeclStmt node) throws MyPLException {
    // TODO: HW7
    int envId = symbolTable.getEnvironmentId();
    List<Object> typeInfo = List.of(envId, node);
    symbolTable.addName(node.funName.lexeme());
    symbolTable.setInfo(node.funName.lexeme(), typeInfo);
  }

  
  // expressions
  
  public void visit(Expr node) throws MyPLException {
    //
    // The following is a basic sketch of what you need to
    // implement. You will need to fill in the remaining code to get
    // Expr working.

    node.first.accept(this);
    Object firstVal = currVal;
    
    if (node.operator != null) {
      if(firstVal instanceof Aitem){
        error("cannot compare arrays",getFirstToken(node));
      }
      node.rest.accept(this);
      Object restVal = currVal;
      String op = node.operator.lexeme();

      // Check for null values (all except == and !=)
      // if you find a null value report an error

      // basic math ops (+, -, *, /, %)
      if (op.equals("+")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal + (Integer)restVal;
        else 
          currVal = (Double)firstVal + (Double)restVal;
      }
      else if (op.equals("-")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal - (Integer)restVal;
        else 
          currVal = (Double)firstVal - (Double)restVal;
      }
      else if (op.equals("*")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal * (Integer)restVal;
        else 
          currVal = (Double)firstVal * (Double)restVal;
      }
      else if (op.equals("/")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal / (Integer)restVal;
        else 
          currVal = (Double)firstVal / (Double)restVal;
      }
      else if (op.equals("%")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal % (Integer)restVal;
        else 
          currVal = (Double)firstVal % (Double)restVal;
      }

      // boolean operators (and, or)
      else if (op.equals("and")) {
        currVal = (Boolean)firstVal && (Boolean)restVal;
      }
      else if (op.equals("or")) {
        currVal = (Boolean)firstVal || (Boolean)restVal;
      }
        
      // relational comparators (=, !=, <, >, <=, >=)
      else if(firstVal == null){
        if(op.equals("=")){
          if(restVal == null){
            currVal = true;
          }
          else
            currVal = false;
        }
        else if(op.equals("!=")){
          if(restVal == null)
            currVal = false;
          else
            currVal = true;
        }
        else{
          error("comparison to a nil", getFirstToken(node));
        }
      }
      else if (op.equals("=")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal == (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal == (Double)restVal;
        else
        //check for null values
          currVal = ((String)firstVal).equals((String)restVal);
      }
      else if (op.equals("!=")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal != (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal != (Double)restVal;
        else
        //check for null values
          currVal = !((String)firstVal).equals((String)restVal);
      }
      else if (op.equals("<")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal < (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal < (Double)restVal;
        else
          currVal = ((String)firstVal).compareTo((String)restVal) < 0;
      }
      else if (op.equals("<=")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal == (Integer)restVal || (Integer)firstVal < (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal == (Double)restVal ||  (Double)firstVal < (Double)restVal;
        else
        //check for null values
          currVal = ((String)firstVal).equals((String)restVal) || ((String)firstVal).compareTo((String)restVal) < 0;
      }
      else if (op.equals(">")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal > (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal > (Double)restVal;
        else
          currVal = ((String)firstVal).compareTo((String)restVal) > 0;
      }
      else if (op.equals(">=")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal == (Integer)restVal || (Integer)firstVal > (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal == (Double)restVal ||  (Double)firstVal > (Double)restVal;
        else
        //check for null values
          currVal = ((String)firstVal).equals((String)restVal) || ((String)firstVal).compareTo((String)restVal) > 0;
      }
    }
    // deal with not operator
    if (node.negated) {
      currVal = !((Boolean)currVal);
    }
  }


  public void visit(LValue node) throws MyPLException {
    if(node.path.size() == 1){
      symbolTable.setInfo(node.path.get(0).lexeme(), currVal);
    }
    else{
      int oid = (Integer)symbolTable.getInfo(node.path.get(0).lexeme());
      Map<String,Object> obj = new HashMap<>();
      obj = (Map<String,Object>)heap.get(oid);
      for(int i = 1; i < node.path.size() - 1; i++){
        oid = (Integer)obj.get(node.path.get(i).lexeme());
        obj = (Map<String,Object>)heap.get(oid);
      }
      obj.put(node.path.get(node.path.size() - 1).lexeme(), currVal);

    }
  }


  
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }

  
  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }

  
  public void visit(SimpleRValue node) throws MyPLException {
    if (node.val.type() == TokenType.INT_VAL)
      currVal = Integer.parseInt(node.val.lexeme());
    else if (node.val.type() == TokenType.DOUBLE_VAL)
      currVal = Double.parseDouble(node.val.lexeme());
    else if (node.val.type() == TokenType.BOOL_VAL)
      currVal = Boolean.parseBoolean(node.val.lexeme());
    else if (node.val.type() == TokenType.CHAR_VAL)
      currVal = node.val.lexeme(); // leave as single character string
    else if (node.val.type() == TokenType.STRING_VAL)
      currVal = node.val.lexeme();
    else if (node.val.type() == TokenType.NIL)
      currVal = null;
  }

  
  public void visit(NewRValue node) throws MyPLException {
    int CurrEnv = symbolTable.getEnvironmentId();
    List<Object> typeInfo = (List<Object>)symbolTable.getInfo(node.typeId.lexeme());
    

    symbolTable.setEnvironmentId((int)typeInfo.get(0));

    Map<String,Object> obj = new HashMap<>();
    int oid = System.identityHashCode(obj);
    symbolTable.pushEnvironment();
    TypeDeclStmt node2 = (TypeDeclStmt)typeInfo.get(1);
    for(VarDeclStmt e:node2.fields){
      e.varExpr.accept(this);
      symbolTable.addName(e.varId.lexeme());
      symbolTable.setInfo(e.varId.lexeme(), currVal);
      obj.put(e.varId.lexeme(), currVal);
     }
    symbolTable.popEnvironment();
    symbolTable.setEnvironmentId(CurrEnv);

    heap.put(oid, obj);
    currVal = oid;
  }


  public void visit(CallRValue node) throws MyPLException {
    List<String> builtIns = List.of("print", "read", "length", "get",
                                    "concat", "append", "itos", "stoi",
                                    "dtos", "stod");
    String funName = node.funName.lexeme();
    if (builtIns.contains(funName))
      callBuiltInFun(node);
    
    else{
    // TODO: User-Defined Functions for HW7
      List<Object> funInfo = (List<Object>)symbolTable.getInfo(node.funName.lexeme());
      int currEnv = symbolTable.getEnvironmentId();

      List<Object> argVals = new ArrayList<>();
      for(int i = 0; i < node.argList.size(); i++){
        node.argList.get(i).accept(this);
        argVals.add(currVal);
      }
      int env = (Integer)funInfo.get(0);
      symbolTable.setEnvironmentId(env);
      FunDeclStmt node2 = (FunDeclStmt)funInfo.get(1);

      symbolTable.pushEnvironment();
      for(int i = 0; i < node.argList.size(); i++){
        symbolTable.addName(node2.params.get(i).paramName.lexeme());
        symbolTable.setInfo(node2.params.get(i).paramName.lexeme(), argVals.get(i));
      }

        try{
          node2.stmtList.accept(this);
        }
        catch (MyPLException e){
          if(!e.isReturnException())
            throw e;
          Object returnVal = e.getReturnValue();
          currVal = returnVal;
        }

      symbolTable.popEnvironment();

      symbolTable.setEnvironmentId(currEnv);
    }
  }

  public void visit(ArrDeclStmt node) throws MyPLException {
    node.arrSize.accept(this);
    int Size = (Integer)currVal;
    node.arrList.accept(this);
    List<Object> items = (List<Object>)node.arrList;
    if(items.size() > Size){
      error("array out of bounds", node.arrName);
    }
    List<Object> content= List.of(Size,items);
    symbolTable.addName(node.arrName.lexeme());
    symbolTable.setInfo(node.arrName.lexeme(), content);
  }

  public void visit(Aitem node) throws MyPLException {
    currVal = node.items;
  }


  
  public void visit(IDRValue node) throws MyPLException {
    if(node.path.size() > 1){
      Map<String,Object> obj = new HashMap<>();
      int oid = (Integer)symbolTable.getInfo(node.path.get(0).lexeme());
      obj = (Map<String,Object>)heap.get(oid);
      for(int i = 1; i < node.path.size() - 1; i++){
        oid = (int)obj.get(node.path.get(i).lexeme());
        obj = (Map<String, Object>)heap.get(oid);
      }
      currVal = obj.get(node.path.get(node.path.size() - 1).lexeme());
    }
    else{
      String varName = node.path.get(0).lexeme();
      currVal = symbolTable.getInfo(varName);
    }
    
  }


  public void visit(NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    if (currVal instanceof Integer)
      currVal = -(Integer)currVal;
    else
      currVal = -(Double)currVal;
  }

  
  // helper functions

  
  private void callBuiltInFun(CallRValue node) throws MyPLException {
    // TODO: HW6
    // Most of function is completed, fill in rest below
    
    String funName = node.funName.lexeme();
    // get the function arguments
    List<Object> argVals = new ArrayList<>();
    for (Expr arg : node.argList) {
      arg.accept(this);
      // make sure no null values
      if (currVal == null)
        error("nil value", getFirstToken(arg));
      argVals.add(currVal);
    }
    if (funName.equals("print")) {
      // Fix '\' 'n' issue
      String msg = (String)argVals.get(0);
      msg = msg.replace("\\n", "\n");
      msg = msg.replace("\\t", "\t");
      System.out.print(msg);
      currVal = null;
    }
    else if (funName.equals("read")) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      try {
        currVal = reader.readLine();
      }
      catch(Exception e) {
        currVal = null;
      }
    }
    else if (funName.equals("get")) {
      int index = (Integer)argVals.get(0);
      String str = (String)argVals.get(1);
      if(index > str.length() - 1){
        error("index out of bounds", getFirstToken(node));
      }
      currVal = str.charAt(index);
    }
    else if (funName.equals("concat")) {
      String str1 = (String)argVals.get(0);
      String str2 = (String)argVals.get(1);
      currVal = str1 + str2;
    }
    else if (funName.equals("append")) {
      currVal = (String)argVals.get(0) + argVals.get(1);
    }
    else if (funName.equals("itos")) {
      currVal = argVals.get(0).toString();
    }
    else if (funName.equals("stoi")) {
      currVal = Integer.parseInt((String)argVals.get(0));
    }
    else if (funName.equals("dtos")) {
      currVal = argVals.get(0).toString();
    }
    else if (funName.equals("stod")) {
      currVal = Double.parseDouble((String)argVals.get(0));
    }
  }

  
  private void error(String msg, Token token) throws MyPLException {
    int row = token.row();
    int col = token.column();
    throw new MyPLException("\nRuntime", msg, row, col);
  }

  
  private Token getFirstToken(Expr node) {
    return getFirstToken(node.first);
  }

  
  private Token getFirstToken(ExprTerm node) {
    if (node instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)node).rvalue);
    else
      return getFirstToken(((ComplexTerm)node).expr);      
  }

  
  private Token getFirstToken(RValue node) {
    if (node instanceof SimpleRValue)
      return ((SimpleRValue)node).val;
    else if (node instanceof CallRValue)
      return ((CallRValue)node).funName;
    else if (node instanceof IDRValue)
      return ((IDRValue)node).path.get(0);
    else if (node instanceof NegatedRValue) 
      return getFirstToken(((NegatedRValue)node).expr);
    else 
      return ((NewRValue)node).typeId;
  }

}    

