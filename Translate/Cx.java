package Translate;
import Tree.*;
import Temp.Label;
import Temp.Temp;

public abstract class Cx extends Exp{
	Tree.Expr unEx()
	{
		Temp r = new Temp();
		Label t = new Label();
		Label f = new Label();
		return new ESEQ(
				new SEQ(new MOVE(new TEMP(r), new CONST(1)),
						new SEQ(unCx(t, f),
						new SEQ(new LABEL(f),
						new SEQ(new MOVE(new TEMP(r), new Tree.CONST(0)),
						new LABEL(t))))),
						new TEMP(r));
	}
	Stm unNx(){return new EXP(unEx());}
	abstract Stm unCx(Label t, Label f);
}
