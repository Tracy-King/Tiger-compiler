package Translate;

import java.util.Stack;

import Frag.*;
import Frame.Frame;
import Symbol.Symbol;
import Temp.Label;
import Temp.Temp;


public class Translate {
	private Frag frags = null;
	public Frame frame;
	public Translate(Frame frame) {this.frame = frame;}
	public Frag getResult() {return frags;}
	private Stack<Label> loopStack = new Stack<Label>();
	public void addFrag(Frag frag){
		frag.next = frags;
		frags = frag;
	}
	
	public void newLoop(Label label){
		loopStack.push(label);
	}
	
	public void exitLoop(){
		if(loopStack.empty())
			Error();
		else loopStack.pop();
	}
	
	public boolean isInLoop(){
		if(!loopStack.empty())
			return true;
		else return false;
	}
	
	public Label LoopLabel(){
		return loopStack.peek();
	}
	
	public void procEntryExit(Level level, Exp body){
		Frame myframe = level.frame;
        Tree.Expr bodyExp = body.unEx();
        Tree.Stm bodyStm;
        if (bodyExp != null) {
            bodyStm = MOVE(TEMP(myframe.RV()), bodyExp);
        } else {
            bodyStm = body.unNx();
        }
        ProcFrag frag = new ProcFrag(myframe.procEntryExit1(bodyStm), myframe);
        addFrag(frag);
	}
	
    private static Tree.Expr CONST(int value) {
        return new Tree.CONST(value);
    }

    private static Tree.Expr NAME(Label label) {
        return new Tree.NAME(label);
    }

    private static Tree.Expr TEMP(Temp temp) {
        return new Tree.TEMP(temp);
    }

    private static Tree.Expr BINOP(int binop, Tree.Expr left, Tree.Expr right) {
        return new Tree.BINOP(binop, left, right);
    }

    private static Tree.Expr MEM(Tree.Expr exp) {
        return new Tree.MEM(exp);
    }

    private static Tree.Expr CALL(Tree.Expr func, Tree.ExpList args) {
        
        return new Tree.CALL(func, args);
    }

    private static Tree.Expr ESEQ(Tree.Stm stm, Tree.Expr exp) {
        if (stm == null) {
            return exp;
        }
        return new Tree.ESEQ(stm, exp);
    }

    private static Tree.Stm MOVE(Tree.Expr dst, Tree.Expr src) {
        return new Tree.MOVE(dst, src);
    }

    private static Tree.Stm EXPR(Tree.Expr exp) {
        return new Tree.EXP(exp);
    }

    private static Tree.Stm JUMP(Label target) {
        return new Tree.JUMP(target);
    }

    private static Tree.Stm CJUMP(int relop, Tree.Expr l, Tree.Expr r, Label t,
            Label f) {
        return new Tree.CJUMP(relop, l, r, t, f);
    }

    private static Tree.Stm SEQ(Tree.Stm left, Tree.Stm right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return new Tree.SEQ(left, right);
    }

    private static Tree.Stm LABEL(Label label) {
        return new Tree.LABEL(label);
    }

    private static Tree.ExpList ExpList(Tree.Expr head, Tree.ExpList tail) {
        return new Tree.ExpList(head, tail);
    }

    private static Tree.ExpList ExpList(Tree.Expr head) {
        return ExpList(head, null);
    }
    
    private static Tree.ExpList ExpList(ExpList exp) {
        if (exp == null) {
            return null;
        }
        return ExpList(exp.head.unEx(), ExpList(exp.tail));
    }
	
    public Exp Error() {
        return new Ex(CONST(0));
    }
    
    public Exp SimpleVar(Access access, Level level) {
        Level d = access.home;
        Tree.Expr base = TEMP(level.frame.FP());
        for (; level != d; level = level.parent) {
            base = level.frame.formals.head.exp(base);
        }
        return new Ex(access.acc.exp(base));
    }

    public Exp FieldVar(Exp record, int index) {
    	//System.out.println("FieldVar");
    	Tree.Expr rec_addr = record.unEx();
    	Tree.Expr rec_off = CONST(index * frame.wordSize());
    	return new Ex(MEM(BINOP(Tree.BINOP.PLUS, rec_addr, rec_off)));
    }

    public Exp SubscriptVar(Exp array, Exp index) {
    	//System.out.println("SubscriptVar");
    	Tree.Expr arr_addr = array.unEx();
    	Tree.Expr arr_off = BINOP(Tree.BINOP.MUL, index.unEx(), CONST(frame.wordSize()));
    	return new Ex(MEM(BINOP(Tree.BINOP.PLUS, arr_addr, arr_off)));
    }
    
    public Exp NilExp(){
		//System.out.println("Nil");
		return new Ex(CONST(0));
	}
    
	public Exp IntExp(int value){
	//System.out.println("IntVar");
		return new Ex(CONST(value));
	}
	
	private java.util.HashMap strings = new java.util.HashMap();
	
	 public Exp StringExp(String lit) {
	    String u = lit.intern();
	    Label lab = (Label) strings.get(u);
	    if (lab == null) {
	        lab = new Label();
	        strings.put(u, lab);
	        DataFrag frag = new DataFrag(lab, frame.string(lab, u));
	        addFrag(frag);
	    }
	    return new Ex(NAME(lab));
	}
	
	 private Tree.Expr CallExp(Symbol f, ExpList args, Level from) {
	    return frame.externalCall(f.toString(), ExpList(args));
	}

	private Tree.Expr CallExp(Level f, ExpList args, Level from) {
	    return frame.externalCall(f.name().toString(), ExpList(args));
	}

	public Exp FunExp(Symbol f, ExpList args, Level from) {
	    return new Ex(CallExp(f, args, from));
	}

	public Exp FunExp(Level f, ExpList args, Level from) {
	    return new Ex(CallExp(f, args, from));
	}

	public Exp ProcExp(Symbol f, ExpList args, Level from) {
	    return new Nx(EXPR(CallExp(f, args, from)));
	}

	public Exp ProcExp(Level f, ExpList args, Level from) {
	    return new Nx(EXPR(CallExp(f, args, from)));
	}
	
	public Exp OpExp(int op, Exp left, Exp right) {
	    switch (op) {
	        case 0:
	        case 1:
            case 2:
	        case 3:
	            return new Ex(BINOP(op, left.unEx(), right.unEx()));
	        case 4:
	        case 5:
	        case 6:
	        case 7:
	        case 8:
	        case 9:
	            return new Ex(BINOP(op, left.unEx(), right.unEx()));
	            // return new RelCx(op - 4, left.unEx(), right.unEx());
	    }
	    return Error();
 }

	public Exp StrOpExp(int op, Exp left, Exp right) {
	    switch (op) {
	        case 4:
	        case 5:
	        case 6:
	        case 7:
	        case 8:
	        case 9:
	            return new Ex(BINOP(op, left.unEx(), right.unEx()));
	    }
	    return Error();
	}
	    
	public Exp RecordExp(ExpList init) {
		//return new Nx(null);
    	//System.out.println("RecordExp");
	    Temp addr = new Temp();
	    int cnt = 0;
	    for(ExpList h = init; h != null; h = h.tail){
	    	frame.allocLocal(true);
	    	cnt += 1;
	    }
	    Tree.Expr alloc = frame.externalCall("malloc", ExpList(CONST((cnt == 0?1:cnt)*frame.wordSize())));
	    Tree.Stm s = null;
	    int i = 0;
	    for (ExpList h = init; h != null; h = h.tail) {
	    	Tree.Expr offset = BINOP(Tree.BINOP.PLUS, TEMP(addr),CONST(i * frame.wordSize()));
	    	++i;
	    	Tree.Expr v = h.head.unEx();
	    	s = SEQ(MOVE(MEM(offset), v), s);
	    	}
	    	return new Ex(ESEQ(SEQ(MOVE(TEMP(addr), alloc), s),TEMP(addr)));
	}    
	
	public Exp combine2Exp(Exp e1, Exp e2){
		if (e1 == null)
			return new Ex(e2.unEx());
		else
			return new Ex(ESEQ(e1.unNx(), e2.unEx()));
	}
	
	public Exp combine2Stm(Exp e1, Exp e2){
		if (e1 == null)
			return new Nx(e2.unNx());
		else if (e2 == null)
			return new Nx(e1.unNx());
		else
			return new Nx(SEQ(e1.unNx(), e2.unNx()));
	}
	public Exp SeqExp(ExpList e) {
    	//System.out.println("SeqExp");
        Nx exp = null;
        if (e != null) {
            exp = new Nx(null);
        }
        while (e != null) {
            if (e.head != null) {
            	exp = new Nx(SEQ(e.head.unNx(), exp.unNx()));
            }
            e = e.tail;
        }
        return exp;
    }
	
	public Exp AssignExp(Exp lhs, Exp rhs) {
    	//System.out.println("AssignExp");
        return new Nx(MOVE(lhs.unEx(), rhs.unEx()));
    }

    public Exp IfExp(Exp cc, Exp aa, Exp bb) {
    	//System.out.println("IfExp");
        return new IfExp(cc, aa, bb);
    }
    
    public Exp WhileExp(Exp test, Exp body, Label done) {
    	//System.out.println("WhileExp");
        return new WhileExp(test, body, done);
    }

	
	
	public Exp ForExp(Access i, Exp lo, Exp hi, Exp body, Label done) {
    	//System.out.println("ForExp");
		Access limit = i;
		Label begin = new Label();
		Label goon = new Label();
		return new Nx(SEQ(MOVE(i.acc.exp(TEMP(frame.FP())),lo.unEx()), 
						SEQ(MOVE(limit.acc.exp(TEMP(frame.FP())),hi.unEx()), 
						SEQ(CJUMP(Tree.CJUMP.LE, i.acc.exp(TEMP(frame.FP())), 
						limit.acc.exp(TEMP(frame.FP())), begin,done), 
						SEQ(LABEL(begin), SEQ(body.unNx(), 
						SEQ(CJUMP(Tree.CJUMP.LT, i.acc.exp(TEMP(frame.FP())),
						limit.acc.exp(TEMP(frame.FP())), goon, done), 
						SEQ(LABEL(goon), SEQ(MOVE( i.acc.exp(
						TEMP(frame.FP())), BINOP(Tree.BINOP.PLUS, i.acc.exp(
						TEMP(frame.FP())), CONST(1))), SEQ(JUMP(begin),
						LABEL(done)))))))))));
	}

	public Exp BreakExp(Label done) {
    	//System.out.println("BreakExp");
	    return new Nx(JUMP(done));
	}

	public Exp LetExp(ExpList lets, Exp body) {
    	//System.out.println("LetExp");
	    ExpList tmp = null;
	    ExpList iter = lets;
	    Exp exp = body;
	    while (iter != null) {
	        tmp = new ExpList(iter.head, tmp);
	        iter = iter.tail;
	    }
	    while (tmp != null) {
	        exp = new Nx(SEQ(tmp.head.unNx(), exp.unNx()));
	        tmp = tmp.tail;
	    }
	    return (exp);
	}
	
	public Exp ArrayExp(Exp size, Exp init) {
    	//System.out.println("ArrayExp");
        Label initArray = new Label("initArray");
        return new Ex(new Tree.CALL(NAME(initArray), new Tree.ExpList(size
                .unEx(), new Tree.ExpList(init.unEx(), null))));
    }

    public Exp VarDec(Access a, Exp init) {
    	//System.out.println("VarDec");
        return new Nx(MOVE(a.acc.exp(TEMP(a.home.frame.FP())),init.unEx()));
    }
    
    

    public Exp TypeDec() {
    	//System.out.println("TypeDec");
        return new Nx(null);
    }

    public Exp FunctionDec() {
    	//System.out.println("FuncDec");
        return new Nx(null);
    }
}
