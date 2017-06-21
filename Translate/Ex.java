package Translate;
import Temp.Label;
import Tree.*;

public class Ex extends Exp{
	Tree.Expr exp = null;
	Ex(Tree.Expr exp) {this.exp = exp;}
	Tree.Expr unEx() {return exp;}
	Stm unNx() {return new EXP(exp);}
	Stm unCx(Label t, Label f)
	{
		return new CJUMP(CJUMP.NE, exp, new CONST(0), t, f);
	}
}
