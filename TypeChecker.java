
/**
 * Author: Alex Lloyd
 * Assign: 5
 * File: TypeChecker.java
 *
 * Visitor implementation of Semantic Analysis Checking for the MyPL
 * AST. Note the following conventions for representing type
 * information:
 * 
 * A variable name's type is a string (varname to string)
 *
 * A structured type name is a map of var mappings (typename to Map) where
 * each variable name is mapped to its type 
 *
 * A function type name is a list of parameter types (name to
 * List) where the list consists of each formal param type ending with
 * the return type of the function.
 *
 * For more information on the general design see the lecture notes.
 */


import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class TypeChecker implements Visitor {
  // the symbol table
  private SymbolTable symbolTable = new SymbolTable();
  // holds last inferred type
  private String currType = null;

  // sets up the initial environment for type checking
  public TypeChecker() {
    symbolTable.pushEnvironment();
    // add return type for global scope
    symbolTable.addName("return");
    symbolTable.setInfo("return", "int");
    // print function
    symbolTable.addName("print");
    symbolTable.setInfo("print", List.of("arraychar", "nil"));
    // read function
    symbolTable.addName("read");
    symbolTable.setInfo("read", List.of("arraychar"));

    symbolTable.addName("concat");
    symbolTable.setInfo("concat", List.of("arraychar", "arraychar","arraychar"));

    symbolTable.addName("append");
    symbolTable.setInfo("append", List.of("arraychar", "char", "arraychar"));

    symbolTable.addName("itos");
    symbolTable.setInfo("itos", List.of("int", "arraychar"));

    symbolTable.addName("stoi");
    symbolTable.setInfo("stoi", List.of("arraychar", "int"));

    symbolTable.addName("dtos");
    symbolTable.setInfo("dtos", List.of("double", "arraychar"));

    symbolTable.addName("stod");
    symbolTable.setInfo("stod", List.of("arraychar", "double"));

    symbolTable.addName("put");

    symbolTable.addName("get");

    symbolTable.addName("length");
  }

  
  // visitor functions
  
  public void visit(StmtList node) throws MyPLException {
    symbolTable.pushEnvironment();
    for (Stmt s : node.stmts)
      s.accept(this);
    symbolTable.popEnvironment();    
  }

  public void visit(VarDeclStmt node) throws MyPLException {
    if(symbolTable.nameExistsInCurrEnv(node.varId.lexeme())){
      error("variable already defined ", node.varId);
    }
    symbolTable.addName(node.varId.lexeme());
    node.varExpr.accept(this);
    if(node.varType == null){
      if(currType.equals("nil")){
        error("variable must be defined", node.varId);
      }
      symbolTable.setInfo(node.varId.lexeme(), currType);
    }
    else{
      
      if(!(currType.equals(node.varType.lexeme()))&& !(currType.equals("arraychar")&&node.varType.lexeme().equals("string"))){
        if(!(currType.equals("nil"))){
          error("type mismatch",node.varId);
        }
      }
      if(node.varType.lexeme().equals("string")){
      symbolTable.setInfo(node.varId.lexeme(),"arraychar");
      }
      else{
        symbolTable.setInfo(node.varId.lexeme(),node.varType.lexeme());
      }
    }
  }

  
  public void visit(AssignStmt node) throws MyPLException {
    // check and infer rhs type
    node.rhs.accept(this);
    String rhsType = currType;
    // check and obtain lhs type
    node.lhs.accept(this);
    String lhsType = currType;
    // error if rhs and lhs types don't match
    if (!rhsType.equals("nil") && !rhsType.equals(lhsType)) {
      error("mismatched type in assignment", node.lhs.path.get(0));
    }
  }

  public void visit(ReturnStmt node) throws MyPLException {
    if(node.returnExpr != null){
      node.returnExpr.accept(this);
      String exprType = currType;
      if(!exprType.equals(symbolTable.getInfo("return")) && !exprType.equals("nil")){
        if(!(exprType.equals("arraychar")) && !(symbolTable.getInfo("return").equals("string"))) {
          error("wrong return type", node.returnToken);
        }
      }
    }
    else{
      if(!symbolTable.getInfo("return").equals("nil")){
        error("wrong return type", node.returnToken);
      }
    }
  }

  public void visit(IfStmt node) throws MyPLException {
    node.ifPart.boolExpr.accept(this);
    if(!currType.equals("bool")){
      error("if expression is not bool", getFirstToken(node.ifPart.boolExpr));
    }
    node.ifPart.stmtList.accept(this);
    for(BasicIf e:node.elsifs){
      e.boolExpr.accept(this);
      if(!currType.equals("bool")){
        error("if expression is not bool", getFirstToken(e.boolExpr));
      }
      e.stmtList.accept(this);
    }
    if(node.hasElse = true){
    node.elseStmtList.accept(this);
    }
  }

  public void visit(WhileStmt node) throws MyPLException {
    node.boolExpr.accept(this);
    if(!currType.equals("bool")){
      error("while expression is not bool", getFirstToken(node.boolExpr));
    }
    node.stmtList.accept(this);
  }

  public void visit(ForStmt node) throws MyPLException{
    symbolTable.pushEnvironment();
    symbolTable.addName(node.var.lexeme());
    symbolTable.setInfo(node.var.lexeme(),"int");
    node.startExpr.accept(this);
    if(!currType.equals("int")){
      error("start expression must be int", getFirstToken(node.startExpr));
    }
    node.endExpr.accept(this);
    if(!currType.equals("int")){
      error("end expression must be int", getFirstToken(node.endExpr));
    }
    node.stmtList.accept(this);
    symbolTable.popEnvironment();
  }

  public void visit(TypeDeclStmt node) throws MyPLException{
    if(symbolTable.nameExists(node.typeId.lexeme())){
      error("variable already defined ", node.typeId);
    }
    symbolTable.addName(node.typeId.lexeme());
    Map<String,String> theType = new HashMap<>();
    symbolTable.pushEnvironment();
    for(VarDeclStmt e:node.fields){
      e.varExpr.accept(this);
      symbolTable.addName(e.varId.lexeme());
      if(e.varType == null){
        theType.put(e.varId.lexeme(), currType);
        symbolTable.setInfo(e.varId.lexeme(), currType);
      }
      else{
        theType.put(e.varId.lexeme(), e.varType.lexeme());
        symbolTable.setInfo(e.varId.lexeme(), e.varType.lexeme());
      }
    }
    symbolTable.popEnvironment();
    symbolTable.setInfo(node.typeId.lexeme(),theType);
  }

  public void visit(FunDeclStmt node) throws MyPLException {
    if(symbolTable.nameExists(node.funName.lexeme())){
      error("function already defined ", node.funName);
    }
    ArrayList<String> paramsList= new ArrayList<>();
    symbolTable.pushEnvironment();
    
    for(FunParam e:node.params){
      paramsList.add(e.paramType.lexeme());
      symbolTable.addName(e.paramName.lexeme());
      symbolTable.setInfo(e.paramName.lexeme(), e.paramType.lexeme());
    }
    paramsList.add(node.returnType.lexeme());

    symbolTable.addName("return");
    symbolTable.setInfo("return", node.returnType.lexeme());

    symbolTable.addName(node.funName.lexeme());
    symbolTable.setInfo(node.funName.lexeme(), List.copyOf(paramsList));

    node.stmtList.accept(this);
    symbolTable.popEnvironment();

    symbolTable.addName(node.funName.lexeme());
    symbolTable.setInfo(node.funName.lexeme(), List.copyOf(paramsList));
    
  }

  public void visit(Expr node) throws MyPLException {

    node.first.accept(this);
    
    String firstType = currType;
    String secondType = null;
    if(node.rest != null){
    node.rest.accept(this);
    secondType = currType;
    }
    if(secondType != null){
      if(!firstType.equals(secondType) && !secondType.equals("nil")){
      error("mismatched types in expression", getFirstToken(node));
      }
    }
    if(node.operator != null){
      String op = node.operator.lexeme();
      if(op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=")){
        if(secondType == null){
          error("cannot compair to null",getFirstToken(node));
        }
        else{
          currType = "bool";
        }
      }

      if(op.equals("!=") || op.equals("=")){
        currType = "bool";
      }

      if(op.equals("or") || op.equals("and")){
        if(secondType == null){
          error("second expression cannot be null",getFirstToken(node));
        }
        else if(!secondType.equals("bool")){
          error("expressions must be bools",getFirstToken(node));
        }
        else{
          currType = "bool";
        }
      }

      if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")){
        if(!(firstType.equals("int") || firstType.equals("double"))){
          error("invalid type in arithmetic expression",getFirstToken(node));
        }
        else{
          currType = firstType;
        }
      }
  }

    if(node.negated){
      if(secondType == null && !firstType.equals("bool")){
        error("cannot negate a non-bool", getFirstToken(node));
      }
      else if(!(secondType ==null)){
        if(!currType.equals("bool")){
          error("cannot negate a non-bool", getFirstToken(node));
        }
      }
    }
  }

  public void visit(LValue node) throws MyPLException{
    // check the first id in the path
    String varName = node.path.get(0).lexeme();
    if (!symbolTable.nameExists(varName))
      error("undefined variable '" + varName + "'", node.path.get(0));
    // make sure it isn't function or type name
    if (symbolTable.getInfo(varName) instanceof List)
      error("unexpected function name in rvalue", node.path.get(0));
    if (symbolTable.getInfo(varName) instanceof Map)
      error("unexpected type name in rvalue", node.path.get(0));
    // grab the type
    currType = (String)symbolTable.getInfo(varName);
    if (node.path.size() > 1 && !(symbolTable.getInfo(currType) instanceof Map))
      error("invalid member access for non-structured type", node.path.get(0));
    // check path
    Map<String,String> typeInfo = (Map<String,String>)symbolTable.getInfo(currType);
    // TODO: finish the path checking ...

    if(node.path.size() > 1) {
      typeInfo = (Map<String, String>) symbolTable.getInfo(currType);
      if(!typeInfo.containsKey(node.path.get(1).lexeme())){
        error("member does not exist", node.path.get(0));
      }
    }
    for(int i = 1; i < node.path.size(); i++){
      varName += "." + node.path.get(i).lexeme();
      if((!typeInfo.containsKey(node.path.get(i).lexeme()))){
        error("undefined variable", node.path.get(0));
      }
      currType = typeInfo.get(node.path.get(i).lexeme());
      if(!(symbolTable.getInfo(currType) instanceof Map) && i != node.path.size() -1) {
        error("invalide access", node.path.get(i));
      }
      else if(i < node.path.size() -1 ){
        typeInfo = (Map<String, String>)symbolTable.getInfo(currType);
      }
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
      currType = "int";
    else if (node.val.type() == TokenType.DOUBLE_VAL)
      currType = "double";
    else if (node.val.type() == TokenType.BOOL_VAL)
      currType = "bool";
    else if (node.val.type() == TokenType.CHAR_VAL)
      currType = "char";
    else if (node.val.type() == TokenType.STRING_VAL)
      currType = "arraychar";
    else if (node.val.type() == TokenType.NIL)
      currType = "nil";
  }

  public void visit(NewRValue node) throws MyPLException {
    currType = node.typeId.lexeme();
  }

  public void visit(Aitem node) throws MyPLException {
    String checkType;
    if(node.items.size() != 0){
      node.items.get(0).accept(this);
      checkType = currType;
      for(Expr n: node.items){
        n.accept(this);
        if(currType != checkType){
          error("Invalid Array", getFirstToken(node.items.get(0)));
        }
      }
      currType = "array" + checkType;
    }
    else{
      currType = "arraynil"; //[]
    }
  }

  public void visit(ArrDeclStmt node) throws MyPLException {
      if(symbolTable.nameExistsInCurrEnv(node.arrName.lexeme())){
        error("array already defined", node.arrName);
      }
      symbolTable.addName(node.arrName.lexeme());
      if(node.arrType.lexeme().equals("string")){
        symbolTable.setInfo(node.arrName.lexeme(),"arrayarraychar");
      }
      else{
        symbolTable.setInfo(node.arrName.lexeme(),"array"+node.arrType.lexeme());
      }
      node.arrList.accept(this);
      if(!currType.equals("arraynil") && !currType.equals(symbolTable.getInfo(node.arrName.lexeme()))){
        error("array type mismatch", node.arrName);
      }
  }

  public void visit(CallRValue node) throws MyPLException {
    if(!symbolTable.nameExists(node.funName.lexeme())){
      error("variable not defined ", node.funName);
    }
    if(node.funName.lexeme().equals("put") || node.funName.lexeme().equals("get")){
      if(node.argList.size() > 1){
        node.argList.get(1).accept(this);
        if(arrayType()){
          defineArray(node);
        }
        else{
          error("type mismatch in function call :)", node.funName);
        }
      }
    }
    if(node.funName.lexeme().equals("length")){
      if(node.argList.size() > 0){
        node.argList.get(0).accept(this);
        if(arrayType()){
          defineArray(node);
        }
        else{
          error("type mismatch in function call :(", node.funName);
        }
      }
    }
    if(!(symbolTable.getInfo(node.funName.lexeme()) instanceof List)){
      error("unexpected id", node.funName);
    }

    List<String> typeList = (List<String>)symbolTable.getInfo(node.funName.lexeme());
    for(int i = 0; i < typeList.size() - 1; i++){
      node.argList.get(i).accept(this);
      if(!(typeList.get(i).equals(currType)) && !(currType.equals("nil"))){
        if((!(typeList.get(i).equals("arraychar")) && !(currType.equals("string"))) && (!(typeList.get(i).equals("string")) && !(currType.equals("arraychar")))) {
          error("type mismatch in function call :0", getFirstToken(node));
        }
      }
    }
    currType = typeList.get(typeList.size() - 1);
  }
  
  public void visit(IDRValue node) throws MyPLException {
    // check the first id in the path
    String varName = node.path.get(0).lexeme();
    if (!symbolTable.nameExists(varName))
      error("undefined variable '" + varName + "'", node.path.get(0));
    // make sure it isn't function or type name
    if (symbolTable.getInfo(varName) instanceof List)
      error("unexpected function name in rvalue", node.path.get(0));
    if (symbolTable.getInfo(varName) instanceof Map)
      error("unexpected type name in rvalue", node.path.get(0));
    // grab the type
    currType = (String)symbolTable.getInfo(varName);
    if (node.path.size() > 1 && !(symbolTable.getInfo(currType) instanceof Map))
      error("invalid member access for non-structured type", node.path.get(0));
    // check path
    Map<String,String> typeInfo = (Map<String,String>)symbolTable.getInfo(currType);
    // TODO: finish the path checking ...

    if(node.path.size() > 1) {
      typeInfo = (Map<String, String>) symbolTable.getInfo(currType);
      if(!typeInfo.containsKey(node.path.get(1).lexeme())){
        error("member does not exist", node.path.get(0));
      }
    }
    for(int i = 1; i < node.path.size(); i++){
      varName += "." + node.path.get(i).lexeme();
      if((!typeInfo.containsKey(node.path.get(i).lexeme()))){
        error("undefined variable", node.path.get(0));
      }
      currType = typeInfo.get(node.path.get(i).lexeme());
      if(!(symbolTable.getInfo(currType) instanceof Map) && i != node.path.size() -1) {
        error("invalide access", node.path.get(i));
      }
      else if(i < node.path.size() -1 ){
        typeInfo = (Map<String, String>)symbolTable.getInfo(currType);
      }
    }
  }

  public void visit(NegatedRValue node) throws MyPLException{
    node.expr.accept(this);
    if(!(currType.equals("int") || currType.equals("double"))){
      error("cannot negate a non int or double", getFirstToken(node));
    }
  }


  // ...

  
  // helper functions

  private Boolean arrayType() throws MyPLException {
    String temporary = null;
    Boolean isArray = false;
    String comparison = "array";
    if (currType.length() > comparison.length() + 1){
      isArray = true;
      for(int i = 0 ; i < comparison.length(); i++){
        if(currType.charAt(i) != comparison.charAt(i)){
          isArray = false;
        }
      }
      if(isArray){
        temporary = Character.toString(currType.charAt(comparison.length()));
        for(int i = comparison.length() + 1; i < currType.length(); i++){
          temporary += currType.charAt(i);
        }
      }
    }
    if(isArray){
    currType = temporary;
    }
    return isArray;
  }

  private void defineArray(CallRValue node) throws MyPLException{
    String input = "array"+currType;
    symbolTable.setInfo("length", List.of(input,"int"));

    symbolTable.setInfo("get", List.of("int", input, currType));

    symbolTable.setInfo("put", List.of("int", input, currType, "nil"));
  }

  private void error(String msg, Token token) throws MyPLException {
    int row = token.row();
    int col = token.column();
    throw new MyPLException("Type", msg, row, col);
  }

  // gets first token of an expression
  private Token getFirstToken(Expr node) {
    return getFirstToken(node.first);
  }

  // gets first token of an expression term
  private Token getFirstToken(ExprTerm node) {
    if (node instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)node).rvalue);
    else
      return getFirstToken(((ComplexTerm)node).expr);      
  }

  // gets first token of an rvalue
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

