package Translate;
import Tree.*;
import Temp.*;

abstract public class Exp {
	abstract Tree.Expr unEx();
	abstract Stm unNx();
	abstract Stm unCx(Label t, Label f);
}
