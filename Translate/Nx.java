package Translate;

import Temp.Label;
import Tree.Stm;

public class Nx extends Exp{
	Stm stm;
	Nx(Stm stm) {this.stm = stm;}
	Tree.Expr unEx() {return null;}
	Stm unNx() {return stm;}
	Stm unCx(Label t, Label f) {return null;}
}
