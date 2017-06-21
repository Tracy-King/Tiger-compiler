package Frame;
import Assem.InstrList;
import Temp.*;
import Util.*;
import Tree.*;

public abstract class Frame implements TempMap {
	public abstract Frame newFrame(Label name, BoolList formals);
	public Label name;
	public AccessList formals = null;
	public abstract Access allocLocal(boolean escape);
	public abstract Expr externalCall(String func, ExpList args);
	public abstract Temp FP();
	public abstract Temp SP();
	public abstract Temp RA();
	public abstract Temp RV();
	public abstract TempList registers();
	public abstract TempList colors();
	public abstract Stm procEntryExit1(Stm body);
	public abstract InstrList procEntryExit2(InstrList body);
	public abstract Proc procEntryExit3(InstrList body);
	public abstract String string(Label label, String value);
	public abstract InstrList codegen(Stm s);
	public abstract int wordSize();
    public abstract String pre();
    public abstract String post();
}
