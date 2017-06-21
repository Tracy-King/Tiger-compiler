package Translate;

import Temp.Label;
import Tree.Expr;
import Tree.Stm;

public class WhileExp extends Exp {
	Exp test = null; 
	Exp body = null;
	Label done = null;
	WhileExp(Exp test, Exp body, Label done) {
	this.test = test;
	this.body = body;
	this.done = done;
	}

	Expr unEx() {
		return null;
	}

	Stm unNx() {
		Label begin = new Label();
		Label t = new Label();
		return new Tree.SEQ(new Tree.LABEL(begin),
		new Tree.SEQ(test.unCx(t, done),
		new Tree.SEQ(new Tree.LABEL(t),
		new Tree.SEQ(body.unNx(),
		new Tree.SEQ(new Tree.JUMP(begin),
		new Tree.LABEL(done))))));
	}

	Stm unCx(Label t, Label f) {
		return null;
	}

}
