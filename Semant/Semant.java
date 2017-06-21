package Semant;

import java.io.PrintStream;

import Absyn.FieldList;
import Absyn.OpExp;
import Frag.Frag;
import Mips.MipsFrame;
import Symbol.Symbol;
import Temp.Label;
import Translate.Access;
import Translate.AccessList;
import Translate.Exp;
import Translate.ExpList;
import Translate.Level;
import Translate.Translate;
import Types.Type;
import Util.BoolList;

public class Semant {
	private Level level;
    private Translate translate;
    private ExpTy expTy;
    private Env env;

    private Type ty;
    public static final Types.VOID VOID = new Types.VOID();
    static final Types.INT INT = new Types.INT();
    static final Types.STRING STRING = new Types.STRING();
    static final Types.NIL NIL = new Types.NIL();
	public int errorNum;
	private PrintStream errorOut;
	public Semant(ErrorMsg.ErrorMsg err, PrintStream errorOut){
		translate = new Translate(new MipsFrame());
        level = new Level(translate.frame);
		env = new Env(err);
		errorNum = 0;
		this.errorOut = errorOut;
	}

    public Semant(Env env, Translate t, Level l, PrintStream errorOut) {
        this.translate = t;
        this.level = l;
        this.env = env;
        this.errorNum = 0;
        this.errorOut = errorOut;
    }
	
	public Frag transProg(Absyn.Exp exp){
		Label l = new Label(Symbol.symbol("t_main"));
        level = new Level(level, l, null);
		
		expTy = transExp(exp);
		translate.procEntryExit(level, expTy.exp);
		return translate.getResult();
	}
	
	public void error(int pos, String s){
		System.out.println("Error At Char "+pos+":"+s);
		errorOut.println("Error At Char "+pos+":"+s);
		++errorNum;
	}
	
	private BoolList escapes(FieldList f) {
        if (f == null) {
            return null;
        }
        return new BoolList(f.escape, escapes(f.tail));
    }
	
	ExpTy transVar(Absyn.Var e){
		if(e instanceof Absyn.SimpleVar)
			return transSimpleVar((Absyn.SimpleVar) e);
		if(e instanceof Absyn.SubscriptVar)
			return transSubscriptVar((Absyn.SubscriptVar) e);
		if(e instanceof Absyn.FieldVar)
			return transFieldVar((Absyn.FieldVar) e);
		return null;
	}
	
	ExpTy transExp(Absyn.Exp e){
		if(e instanceof Absyn.ArrayExp)
			return transArrayExp((Absyn.ArrayExp) e);
		if(e instanceof Absyn.AssignExp)
			return transAssignExp((Absyn.AssignExp) e);
		if(e instanceof Absyn.BreakExp)
			return transBreakExp((Absyn.BreakExp) e);
		if(e instanceof Absyn.CallExp)
			return transCallExp((Absyn.CallExp) e);
		if(e instanceof Absyn.ForExp)
			return transForExp((Absyn.ForExp) e);
		if(e instanceof Absyn.IfExp)
			return transIfExp((Absyn.IfExp) e);
		if(e instanceof Absyn.IntExp)
			return transIntExp((Absyn.IntExp) e);
		if(e instanceof Absyn.LetExp)
			return transLetExp((Absyn.LetExp) e);
		if(e instanceof Absyn.NilExp)
			return transNilExp((Absyn.NilExp) e);
		if(e instanceof Absyn.OpExp)
			return transOpExp((Absyn.OpExp) e);
		if(e instanceof Absyn.RecordExp)
			return transRecordExp((Absyn.RecordExp) e);
		if(e instanceof Absyn.SeqExp)
			return transSeqExp((Absyn.SeqExp) e);
		if(e instanceof Absyn.StringExp)
			return transStringExp((Absyn.StringExp) e);
		if(e instanceof Absyn.VarExp)
			return transVarExp((Absyn.VarExp) e);
		if(e instanceof Absyn.WhileExp)
			return transWhileExp((Absyn.WhileExp) e);
		return null;
	}
	Absyn.Dec transDec(Absyn.Dec e){
		if(e instanceof Absyn.VarDec)
			return transVarDec((Absyn.VarDec) e);
		if(e instanceof Absyn.TypeDec)
			return transTypeDec((Absyn.TypeDec) e);
		if(e instanceof Absyn.FunctionDec)
			return transFunctionDec((Absyn.FunctionDec) e);
		return null;
	}
	Types.Type transTy(Absyn.Ty e){
		if(e instanceof Absyn.NameTy)
			return transNameTy((Absyn.NameTy) e);
		if(e instanceof Absyn.ArrayTy)
			return transArrayTy((Absyn.ArrayTy) e);
		if(e instanceof Absyn.RecordTy)
			return transRecordTy((Absyn.RecordTy) e);
		return null;
	}
	
	ExpTy transSimpleVar(Absyn.SimpleVar v){
		Entry x = (Entry)env.vEnv.get(v.name);
		if(x == null || !(x instanceof VarEntry)){
			error(v.pos, "Undefined variable "+v.name);
			expTy = new ExpTy(translate.Error(), VOID);
			return expTy;
		}
		else {
			VarEntry ent = (VarEntry) x;
			expTy = new ExpTy(translate.SimpleVar(x.getAccess(), level), ent.getTy());
			return expTy;
		}
	}
	
	ExpTy transSubscriptVar(Absyn.SubscriptVar v){
		ExpTy e = transExp(v.index);
		ExpTy var = transVar(v.var);
		if(var.ty instanceof Types.ARRAY){
			if(e.ty instanceof Types.INT){
				expTy = new ExpTy(translate.SubscriptVar(var.exp, e.exp), ((Types.ARRAY)var.ty).element);
				return expTy;
			}
			else{
				error(v.pos, "Index of a subscript variable must be an integar (actually "+e.ty.getClass()+")");
				expTy = new ExpTy(translate.Error(), VOID);
				return expTy;
			}
		}
		error(v.pos, "Undefined Array Type "+var.ty.getClass());
		expTy = new ExpTy(translate.Error(), VOID);
		return expTy;
	}
	
	ExpTy transFieldVar(Absyn.FieldVar v){
		ExpTy var = transVar(v.var);
		//if(var != null && var.ty != null)System.out.println("FieldVar: "+v.pos+" "+(var.ty instanceof Types.RECORD));
		if(var.ty instanceof Types.RECORD){
			int cnt = 0;
			for(Types.RECORD tmp = (Types.RECORD)var.ty;tmp != null; tmp = tmp.tail){
				//System.out.println("FieldVar Name:"+tmp.fieldName+" "+tmp.fieldType.getClass());
				if(tmp.fieldName == v.field){
					expTy = new ExpTy(translate.FieldVar(var.exp, cnt), tmp.fieldType);
					return expTy;
				}
				 ++cnt;
			}
			error(v.pos, "Field "+v.field+" not found");
			expTy = new ExpTy(null, VOID);
			return expTy;
		}
		else{
			if(var.ty != null)
				error(v.pos, "Undefined Record Type "+var.ty.getClass());
			else error(v.pos, "Undefined Record Type null");
			expTy = new ExpTy(null, VOID);
			return expTy;
		}
	}
	
	Types.Type transNameTy(Absyn.NameTy t){
		Types.Type tt = (Types.Type)env.tEnv.get(t.name);
		if(tt != null){
			ty = tt;
			return tt;
		}
		else{
			error(t.pos, "Undefined Type "+t.name);
			ty = VOID;
			return ty;
		}
	}
	
	Types.Type transArrayTy(Absyn.ArrayTy t){
		Types.Type at = (Types.Type)env.tEnv.get(t.typ);
		if(at != null)
			return new Types.ARRAY(at);
		else{
			error(t.pos, "Undefined Type "+t.typ);
			ty = new Types.ARRAY(null);
			return ty;
		}
	}
	
	Types.Type transRecordTy(Absyn.RecordTy t){
		ty = transFieldListTy(t.fields);
		return ty;
	}
	
	Types.RECORD transFieldListTy(Absyn.FieldList t){
		Types.RECORD he = null,ta = null;
		he = new Types.RECORD(null, null, null);
		ta = he;
		for(Absyn.FieldList tmp = t; tmp != null; tmp = tmp.tail){
			Types.Type tt = (Types.Type)env.tEnv.get(tmp.typ);
			if(tt == null){
				env.tEnv.put(tmp.typ, new Types.NAME(tmp.typ));
			}
			//env.vEnv.put(tmp.name, new VarEntry(tt));
			//System.out.println(tmp.pos+" "+tt.getClass()+"\r\n");
			ta.tail = new Types.RECORD(tmp.name, tt, null);
			//System.out.println("ta =" + ta);
			ta = ta.tail;
		}
		//System.out.println("after he=" + he.tail);
		return he.tail;
	}
	
	Absyn.Dec transVarDec(Absyn.VarDec d){
		if(d.init == null){
			error(d.init.pos, "Variable must be initialized");
			expTy = new ExpTy(translate.Error(), VOID);
			return null;
		}
		
		ExpTy e = transExp(d.init);
		Access access = null;
		if(e.exp == null){
			error(d.init.pos, "Variable must be initialized");
			expTy = new ExpTy(translate.Error(), VOID);
			return null;
		}
		if(d.typ != null){
			Types.Type t = transTy(d.typ);
			if(!t.getClass().equals(e.ty.getClass()) && !(t instanceof Types.RECORD && e.ty.coerceTo(NIL))){
				error(d.pos, "Type dismatching in variable declaration( "+t.getClass().toString()+" and "+e.ty.getClass().toString()+")");
				expTy = new ExpTy(translate.Error(), VOID);
				return null;
			}
			else if(e.ty instanceof Types.VOID){
				error(d.pos,  "Variable initialization error("+d.name+" can not be initialized by the type of "+e.ty.getClass().toString()+")");
				expTy = new ExpTy(translate.Error(), VOID);
				return null;
			}
			access = level.allocLocal(d.escape);
			env.vEnv.put(d.name, new VarEntry(access, t));
		}
		else{
			if(e.ty instanceof Types.NIL){
				error(d.pos, "Variable initialization error("+d.name+" can not be initialized by the type of "+e.ty.getClass().toString()+")");
				expTy = new ExpTy(translate.Error(), VOID);
				return null;
			}
				access = level.allocLocal(d.escape);
			env.vEnv.put(d.name, new VarEntry(access, e.ty));
		}
		//if(e != null && e.ty != null)
			//System.out.println("Type: "+d.pos+" "+e.ty.getClass());
		//System.out.println("Type: "+d.pos+" "+d.init.toString());
		
		expTy = new ExpTy(translate.VarDec(access, e.exp), VOID);
		return null;
	}
	
	Absyn.Dec transTypeDec(Absyn.TypeDec d){
		Absyn.TypeDec td = d;
		Types.Type tt = null;
		while(td!= null){
			tt = (Type)env.tEnv.get(td.name);
			if(tt != null && !(tt instanceof Types.NAME)){
				error(td.pos, "Duplicated declaration of type "+td.name);
				expTy = new ExpTy(translate.Error(), VOID);
				return null;
			}
			else env.tEnv.put(td.name, new Types.NAME(td.name));
			Types.Type t = transTy(td.ty);
			Types.NAME tn = (Types.NAME)env.tEnv.get(td.name);
			tn.bind(t);
			if(tn.isLoop()){
				error(td.pos, "Loop declaration error ");
				expTy = new ExpTy(translate.Error(), VOID);
				return null;
			}
			//if(t!=null)System.out.println("TypeName: "+td.name+" "+t.getClass());
			env.tEnv.put(td.name, t);
			//Types.Type test = (Types.Type)env.tEnv.get(td.name);
			//System.out.println("AfterTypeName: "+td.name+" "+test.getClass());
			td = td.next;
		}
		expTy = new ExpTy(translate.TypeDec(), VOID);
		return null;
	}
	
	Absyn.Dec transFunctionDec(Absyn.FunctionDec d){
		Absyn.FunctionDec fd = d;
		while(fd != null){
			//System.out.println("FuncDec: "+fd.name);
			if(env.vEnv.get(fd.name) != null){
				error(fd.pos, "Duplication declaration of "+fd.name);
			}
			Types.Type result = null;
			if(fd.result != null)
				result = transTy(fd.result);
			else result = VOID;
			Types.RECORD formals = transFieldListTy(fd.params);
			Level newLevel = new Level(level, new Label(fd.name), escapes(fd.params));
			env.vEnv.put(fd.name, new FunEntry(newLevel, formals, result));
			fd = fd.next;
		}
		fd = d;
		while(fd != null){
			//if(env.vEnv.get(fd.name)!=null)System.out.println("Dec "+fd.name);
			env.vEnv.beginScope();
			FunEntry fe = (FunEntry)env.vEnv.get(fd.name);
			AccessList a = fe.getLevel().formals;
			Types.RECORD re = fe.getFormals();
			for(; re != null; re = re.tail){ 
				env.vEnv.put(re.fieldName, new VarEntry(a.head, re.fieldType));
			}
			Semant fun = new Semant(env, translate, fe.getLevel(), errorOut);
			fun.transProg(fd.body);
			errorNum += fun.errorNum;
			ExpTy body = fun.expTy;
			if(fe.getResult() == null)
				fe.setResult(body.ty);
			if(!body.ty.coerceTo(fe.getResult())){
				error(fd.pos, "Return type error (should be "+fe.getResult().getClass()+", actually is "+body.ty.getClass()+")");
			}
			translate.procEntryExit(fe.getLevel(), body.exp);
			env.vEnv.endScope();
			//System.out.println("EndFuncDec: "+fd.name);
			fd = fd.next;
		}
		expTy = new ExpTy(translate.FunctionDec(), VOID);
		return null;		
	}
	
	
	
	
	
	
	
	ExpTy transArrayExp(Absyn.ArrayExp e){
		ExpTy size = transExp(e.size);
		ExpTy init = transExp(e.init);
		Types.Type t = (Types.Type)env.tEnv.get(e.typ);
		if(t == null || !(t instanceof Types.ARRAY)){
			error(e.pos, "Undefined array type "+t.getClass());
		}
		if(!(size.ty instanceof Types.INT)){
			error(e.size.pos, "Size of an array must be an integar (actually "+size.ty.getClass()+")");
			return new ExpTy(translate.Error(), VOID);
		}
		Types.ARRAY at = (Types.ARRAY) t;
		if(!(init.ty instanceof Types.VOID) && !init.ty.getClass().equals(at.element.getClass())){
			error(e.init.pos, "Type dismatching in array ("+init.ty.getClass()+" and "+at.element.getClass()+")");
			return new ExpTy(translate.Error(), VOID);
		}
		expTy = new ExpTy(translate.ArrayExp(size.exp, init.exp), new Types.ARRAY(init.ty));
		return expTy;
	}
	
	ExpTy transAssignExp(Absyn.AssignExp e){
		ExpTy v = transVar(e.var);
		ExpTy exp = transExp(e.exp);
		if(e.var instanceof Absyn.SimpleVar){
			Absyn.SimpleVar sv = (Absyn.SimpleVar) e.var;
			Entry ent = (Entry)env.vEnv.get(sv.name);
			if(ent instanceof VarEntry){
				VarEntry vent = (VarEntry) ent;
				if(vent.getTy() instanceof Types.NAME)
					if(((Types.NAME)vent.getTy()).isLoop()){
						error(e.pos, "Loop variable can not be assigned");
					}
			}
		}
		if(exp.ty instanceof Types.VOID){
			error(e.pos, "Can not assign a void exp to a var");
			return new ExpTy(translate.Error(), VOID);
		}
		if(v.ty!=null && exp.ty!=null && !v.ty.getClass().equals(exp.ty.getClass()) && !(v.ty instanceof Types.RECORD && exp.ty.coerceTo(NIL))){
			//System.out.println("TypeName:"+e.exp.pos);
			error(e.pos, "Type dismatching in assigning("+v.ty.getClass()+" and "+exp.ty.getClass()+")");
			return new ExpTy(translate.Error(), VOID);
		}
		expTy = new ExpTy(translate.AssignExp(v.exp, exp.exp), VOID);
		return expTy;
	}
	
	ExpTy transBreakExp(Absyn.BreakExp e){
		if(translate.isInLoop()){
			expTy = new ExpTy(translate.BreakExp(translate.LoopLabel()), VOID);
			return expTy;
		}
		else {
			error(e.pos, "Break outside of loop");
			expTy = new ExpTy(translate.Error(), VOID);
			return expTy;
		}
	}
	
	ExpTy transCallExp(Absyn.CallExp e){
		//System.out.println("Call "+e.func);
		int ecnt = 0, rcnt = 0;
		Entry ent = (Entry)env.vEnv.get(e.func);
		
		if(ent == null || !(ent instanceof FunEntry)){
			error(e.pos, "Undefined function "+e.func);
			expTy = new ExpTy(translate.Error(), VOID);
			return expTy;
		}
		FunEntry fent = (FunEntry) ent;
		Types.RECORD r = fent.getFormals();
		Absyn.ExpList ep = e.args;
		ExpList args = null;
		for(; ep != null && r != null; ep=ep.tail,r = r.tail){
			ExpTy et = transExp(ep.head);
			args = new ExpList(et.exp, args);
			if(et.ty instanceof Types.VOID){
				error(e.pos, "Can not assign a void exp to a var");
				expTy = new ExpTy(translate.Error(), VOID);
				return expTy;
			}
			if(et.ty!=null && r.fieldType!=null && !et.ty.getClass().equals(r.fieldType.getClass())){
				//System.out.println("Formal: "+r.fieldName+" "+r.fieldType.getClass());
				//System.out.println("Argu: "+et.ty.getClass());
				error(e.pos, "Type dismatching in function assigning("+et.ty.getClass()+" and "+r.fieldType.getClass()+")");
				expTy = new ExpTy(translate.Error(), VOID);
				return expTy;
			}
			ecnt++;
			rcnt++;
		}
		if(ep != null){
			while(ep!=null){
				ep = ep.tail;
				++ecnt;
			}
			error(e.args.head.pos, "Arguments are more than formals(Required "+rcnt+", actually "+ecnt+")");
			expTy = new ExpTy(translate.Error(), VOID);
			return expTy;
		}
		if(r != null){
			while(r!=null){
				r = r.tail;
				++rcnt;
			}
			error(e.args.head.pos, "Arguments are less than formals(Required "+rcnt+", actually "+ecnt+")");
			expTy = new ExpTy(translate.Error(), VOID);
			return expTy;
		}
		
		if (fent.getLevel() == null) {
            expTy = new ExpTy(fent.getResult().coerceTo(VOID)
                    ? translate.ProcExp(e.func, args, level)
                    : translate.FunExp(e.func, args, level),
                    fent.getResult());
        } else {
            expTy = new ExpTy(fent.getResult().coerceTo(VOID)
                    ? translate.ProcExp(fent.getLevel(), args, level)
                    : translate.FunExp(fent.getLevel(), args, level),
                    fent.getResult());
        }
		return expTy;
	}
	
	ExpTy transForExp(Absyn.ForExp e){
		ExpTy h = transExp(e.hi);
		ExpTy in = transExp(e.var.init);
		Access access = level.allocLocal(e.var.escape);
		if(!(in.ty instanceof Types.INT)){
			error(e.pos, "Initial value should be an integar");
			return new ExpTy(translate.Error(), VOID);
		} if(!(h.ty instanceof Types.INT)){
			error(e.pos, "Terminal value should be an integar");
			return new ExpTy(translate.Error(), VOID);
		}
		env.vEnv.beginScope();
		transVarDec(e.var);
		Label done = new Label();
		translate.newLoop(done);
		/*ExpTy b=*/ExpTy body = transExp(e.body);
		translate.exitLoop();
		env.vEnv.endScope();
		expTy = new ExpTy(translate.ForExp(access, in.exp, h.exp, body.exp, done), VOID);
		return expTy;
	}
	
	ExpTy transIfExp(Absyn.IfExp e){
		ExpTy test = transExp(e.test);
		if(!(test.ty instanceof Types.INT)){
			if(test != null && test.ty != null)
				error(e.test.pos, "If Test condition error (Required Types.INT, actually is " + test.ty.getClass()+")");
			else error(e.test.pos, "If Test condition error (Required Types.INT, actually is null)");
			return new ExpTy(translate.Error(), VOID);
		}
		ExpTy then = transExp(e.thenclause);
		ExpTy el = null;
		if(e.elseclause == null && then!=null && then.ty!=null && !(then.ty instanceof Types.VOID)){
			error(e.thenclause.pos, "Then clause error (Then clause has the return of "+then.ty.getClass()+")");
			return new ExpTy(translate.Error(), VOID);
		}
		else {
			el = transExp(e.elseclause);
			if(el != null && then != null && el.ty!=null && then.ty!=null && !el.ty.getClass().equals(then.ty.getClass())){
				if((el.ty instanceof Types.NIL && then.ty instanceof Types.RECORD)
					|| (el.ty instanceof Types.RECORD && then.ty instanceof Types.NIL));
				else {
					error(e.pos, "Type dismatching in then-else ("+el.ty.getClass()+" and "+then.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
			}
		}
		if(then != null &&  then.ty != null){
			if(el != null){
				expTy = new ExpTy(translate.IfExp(test.exp, then.exp, el.exp), then.ty);
				return expTy;
			}
			else {
				expTy = new ExpTy(translate.IfExp(test.exp, then.exp, null), then.ty);
				return expTy;
			}
		}
		else {
			expTy = new ExpTy(translate.IfExp(test.exp, then.exp, el.exp), VOID);
			return expTy;
		}
	}
	
	ExpTy transIntExp(Absyn.IntExp e){
		expTy = new ExpTy(translate.IntExp(e.value),INT);
		return expTy;
	}
	
	ExpTy transLetExp(Absyn.LetExp e){
		env.tEnv.beginScope();
		env.vEnv.beginScope();
		Exp exp = null;
		for(Absyn.DecList dl=e.decs; dl!=null; dl=dl.tail){
			transDec(dl.head);
			exp = translate.combine2Stm(exp, expTy.exp);
			//System.out.println(exp.toString());
		}		
		ExpTy r = transExp(e.body);
		env.vEnv.endScope();
		env.tEnv.endScope();
		if(r.ty.coerceTo(VOID) || r.ty == null)
			//System.out.println(e.body.getClass());
			expTy =  new ExpTy(translate.combine2Stm(exp, r.exp), VOID);
		else 
			//System.out.println(r.ty.getClass());
			expTy = new ExpTy(translate.combine2Exp(exp, r.exp), r.ty);
		return expTy;
	}
	
	ExpTy transNilExp(Absyn.NilExp e){
		expTy = new ExpTy(translate.NilExp(), NIL);
		return expTy;
	}
	
	ExpTy transOpExp(Absyn.OpExp e){
		ExpTy l = transExp(e.left);
		ExpTy r = transExp(e.right);
		switch(e.oper)
		{
			case OpExp.PLUS:
				if(!(l.ty instanceof Types.INT)){
					error(e.left.pos, "Type dismatching in + (must be integar, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT)){
					error(e.right.pos, "Type dismatching in + (must be integar, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.DIV:
				if(!(l.ty instanceof Types.INT)){
					error(e.left.pos, "Type dismatching in / (must be integar, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT)){
					error(e.right.pos, "Type dismatching in / (must be integar, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.MINUS:
				if(!(l.ty instanceof Types.INT)){
					error(e.left.pos, "Type dismatching in - (must be integar, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT)){
					error(e.right.pos, "Type dismatching in - (must be integar, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.MUL:
				if(!(l.ty instanceof Types.INT)){
					error(e.left.pos, "Type dismatching in * (must be integar, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT)){
					error(e.right.pos, "Type dismatching in * (must be integar, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.GE:
				if(!(l.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.left.pos, "Type dismatching in >= (must be integar or string, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.right.pos, "Type dismatching in >= (must be integar or string, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.LE:
				if(!(l.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.left.pos, "Type dismatching in <= (must be integar or string, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.right.pos, "Type dismatching in <= (must be integar or string, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.GT:
				if(!(l.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.left.pos, "Type dismatching in > (must be integar or string, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.right.pos, "Type dismatching in > (must be integar or string, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.LT:
				if(!(l.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.left.pos, "Type dismatching in < (must be integar or string, actually "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				else if(!(r.ty instanceof Types.INT || l.ty instanceof Types.STRING)){
					error(e.right.pos, "Type dismatching in < (must be integar or string, actually "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				break;
			case OpExp.EQ:
				if(l.ty instanceof Types.VOID){
					error(e.left.pos, "Type dismatching in = (can not be "+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				if(r.ty instanceof Types.VOID){
					error(e.right.pos, "Type dismatching in = (can not be "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				if(l.ty instanceof Types.NIL && r.ty instanceof Types.NIL){
					error(e.left.pos, "Type dismatching in = (both can not be "+l.ty.getClass()+")");
					error(e.right.pos, "Type dismatching in = (both can not be "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				if(!l.ty.getClass().equals(r.ty.getClass())){
					if(l.ty instanceof Types.NIL && r.ty instanceof Types.RECORD){
						expTy = new ExpTy(translate.OpExp(e.oper, l.exp, r.exp), INT);
						return expTy;
					}
					if(r.ty instanceof Types.NIL && l.ty instanceof Types.RECORD){
						expTy = new ExpTy(translate.OpExp(e.oper, l.exp, r.exp), INT);
						return expTy;
					}
					else {
						error(e.left.pos, "Type dismatching in = ("+l.ty.getClass()+" and "+r.ty.getClass()+")");
						error(e.right.pos, "Type dismatching in = ("+l.ty.getClass()+" and "+r.ty.getClass()+")");
						return new ExpTy(translate.Error(), VOID);
					}
				}
				break;
			case OpExp.NE:
				if(l.ty instanceof Types.VOID){
					error(e.left.pos, "Type dismatching in != (can not be"+l.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				if(r.ty instanceof Types.VOID){
					error(e.right.pos, "Type dismatching in != (can not be"+r.ty.getClass()+")");
				}
				if(l.ty instanceof Types.NIL && r.ty instanceof Types.NIL){
					error(e.left.pos, "Type dismatching in != (both can not be "+l.ty.getClass()+")");
					error(e.right.pos, "Type dismatching in != (both can not be "+r.ty.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
				if(!l.ty.getClass().equals(r.ty.getClass())){
					if(l.ty instanceof Types.NIL && r.ty instanceof Types.RECORD){
						expTy = new ExpTy(translate.OpExp(e.oper, l.exp, r.exp), INT);
						return expTy;
					}
					if(r.ty instanceof Types.NIL && l.ty instanceof Types.RECORD){
						expTy = new ExpTy(translate.OpExp(e.oper, l.exp, r.exp), INT);
						return expTy;
					}
					else {
						error(e.left.pos, "Type dismatching in != ("+l.ty.getClass()+" and "+r.ty.getClass()+")");
						error(e.right.pos, "Type dismatching in != ("+l.ty.getClass()+" and "+r.ty.getClass()+")");
						return new ExpTy(translate.Error(), VOID);
					}
				}
				break;
		}
		expTy = new ExpTy(translate.OpExp(e.oper, l.exp, r.exp), INT);
		return expTy;
	}
	
	ExpTy transRecordExp(Absyn.RecordExp e){
		int rcnt = 0, fcnt = 0;
		Types.Type t = (Types.Type)env.tEnv.get(e.typ);
		if(t == null){
			error(e.pos, "Type dismatching in record (must be record, actually is unknown)");
			return new ExpTy(translate.Error(), VOID);
		}
		else if(!(t instanceof Types.RECORD)){
			error(e.pos, "Type dismatching in record (must be record, actually is "+t.getClass()+")");
			return new ExpTy(translate.Error(), VOID);
		}
		Types.RECORD r = (Types.RECORD) t;
		Absyn.FieldExpList f = e.fields;
		ExpList ep = null;
		for(;r!=null && f!=null;r=r.tail,f=f.tail){
			if(r.fieldName != f.name){
				error(f.pos, "Field name dismatch(Required "+r.fieldName+", actually is "+f.name+")");
				return new ExpTy(translate.Error(), VOID);
			}
			ExpTy fet = transExp(f.init);
			ep = new ExpList(fet.exp, ep);
			if(fet.ty instanceof Types.VOID){
				error(e.pos, "Can not assign a void exp to a var");
				return new ExpTy(translate.Error(), VOID);
			}
			if(fet.ty!=null && r.fieldType!=null && !fet.ty.getClass().equals(r.fieldType.getClass()))
				if(r.fieldType instanceof Types.NAME){
					Types.NAME tmp = (Types.NAME) r.fieldType;
					if(!fet.ty.getClass().equals(tmp.actual().getClass()) && !(fet.ty.coerceTo(NIL) && (tmp.actual() instanceof Types.RECORD)))
						error(e.pos, "Type dismatching in record assigning("+fet.ty.getClass()+" and "+tmp.actual().getClass()+")");
					else{
						Types.RECORD tr = (Types.RECORD) t;
						for(;tr.tail!=r;tr=tr.tail);
							r = new Types.RECORD(r.fieldName, tmp.actual(), r.tail);
							tr.tail = r;
						//System.out.println("Correct: "+r.fieldName+" "+r.fieldType.getClass());
					}
				}
				else {
					error(e.pos, "Type dismatching in record assigning("+fet.ty.getClass()+" and "+r.fieldType.getClass()+")");
					return new ExpTy(translate.Error(), VOID);
				}
			++rcnt;
			++fcnt;
		}
		if(f != null){
			while(f!=null){
				f=f.tail;
				++fcnt;
			}
			error(e.fields.pos, "Fields are more than records(Required "+rcnt+", actually "+fcnt+")");
			return new ExpTy(translate.Error(), VOID);
		}
		if(r != null){
			while(r!=null){
				r=r.tail;
				++rcnt;
			}
			error(e.fields.pos, "Fields are less than records(Required "+rcnt+", actually "+fcnt+")");
			return new ExpTy(translate.Error(), VOID);
		}
		expTy = new ExpTy(translate.RecordExp(ep), (Types.RECORD) t);
		return expTy;
	}
	
	ExpTy transSeqExp(Absyn.SeqExp e){
		ExpTy result = null;
		Exp exp = null;
		for(Absyn.ExpList el = e.list;el != null;el = el.tail){
			result = transExp(el.head);
			if(el.tail == null && (result.ty != null && !result.ty.coerceTo(VOID))){
				//System.out.println(result.ty.getClass());
				exp = translate.combine2Exp(exp, result.exp);
			}
			else exp = translate.combine2Stm(exp, result.exp);
		}
		if(result == null)
			expTy = new ExpTy(exp, VOID);
		else expTy = new ExpTy(exp, result.ty);
		return expTy;
	}
	
	ExpTy transStringExp(Absyn.StringExp e){
		expTy = new ExpTy(translate.StringExp(e.value), STRING);
		return expTy;
	}
	
	ExpTy transVarExp(Absyn.VarExp e){
		expTy = transVar(e.var);
		return expTy;
	}
	
	ExpTy transWhileExp(Absyn.WhileExp e){
		ExpTy test = transExp(e.test);
		if(!(test.ty instanceof Types.INT)){
			if(test != null && test.ty != null)
				error(e.test.pos, "While Test condition error (Required Types.INT, actually is " + test.ty.getClass()+")");
			else error(e.test.pos, "While Test condition error (Required Types.INT, actually is null)");
			return new ExpTy(translate.Error(), VOID);
		}
		Label done = new Label();
		translate.newLoop(done);
		ExpTy body = transExp(e.body);
		translate.exitLoop();
		if(body.ty != VOID){
			error(e.body.pos, "Body type shouldn't be "+body.ty.getClass());
			return new ExpTy(translate.Error(), VOID);
		}
		expTy = new ExpTy(translate.WhileExp(test.exp, body.exp, done), VOID);
		return expTy;
	}
}






