package Semant;

import Symbol.*;
import Translate.Level;
import Types.*;

public class Env {
	Table vEnv = null;
	Table tEnv = null;
	ErrorMsg.ErrorMsg errorMsg = null;
	
	static Types.VOID VOID = new Types.VOID();
	static Types.INT INT = new Types.INT();
	static Types.STRING STRING = new Types.STRING();
	static Types.NIL NIL = new Types.NIL();
	
	Env(ErrorMsg.ErrorMsg errorMsg)
	{
		this.errorMsg = errorMsg;
		initTEnv();
		initVEnv();
	}
	void initTEnv()
	{
		tEnv = new Table();
		tEnv.put(Symbol.symbol("int"), INT);
		tEnv.put(Symbol.symbol("string"), STRING);
		tEnv.put(Symbol.symbol("nil"), NIL);
		tEnv.put(Symbol.symbol("void"), VOID);
	}
	
	public void initVEnv()
	{
		vEnv = new Table();
		
		Symbol sym = null;
		RECORD formals = null;
		Type result = null;
		//print(s:string)
		sym = Symbol.symbol("print");		
		formals = new Types.RECORD(Symbol.symbol("s"), STRING, null);
		result = VOID;
		vEnv.put(sym, new FunEntry(formals, result));
		//printi(i:int)
		sym = Symbol.symbol("printi");
		formals = new Types.RECORD(Symbol.symbol("i"), INT, null);
		result = VOID;
		vEnv.put(sym, new FunEntry(formals, result));
		//flush()
		sym = Symbol.symbol("flush");
		formals = null;
		result = VOID;
		vEnv.put(sym, new FunEntry(formals, result));
		//getchar():string
		sym = Symbol.symbol("getchar");
		formals = null;
		result = STRING;
		vEnv.put(sym, new FunEntry(formals, result));
		//ord(s:string):int
		sym = Symbol.symbol("ord");
		formals = new Types.RECORD(Symbol.symbol("s"), STRING, null);
		result = INT;
		vEnv.put(sym, new FunEntry(formals, result));
		//chr(i:int):string
		sym = Symbol.symbol("chr");
		formals = new Types.RECORD(Symbol.symbol("i"), INT, null);
		result = STRING;
		vEnv.put(sym, new FunEntry(formals, result));
		//size(s:string):int
		sym = Symbol.symbol("size");
		formals = new Types.RECORD(Symbol.symbol("s"), STRING, null);
		result = INT;
		vEnv.put(sym, new FunEntry(formals, result));
		//substring(s:string, f:int, n:int):string
		sym = Symbol.symbol("substring");
		formals = new Types.RECORD(Symbol.symbol("s"), STRING,
				new Types.RECORD(Symbol.symbol("f"), INT, 
				new Types.RECORD(Symbol.symbol("n"), INT, null)));
		result = STRING;
		vEnv.put(sym, new FunEntry(formals, result));
		//concat(s1:sring, s2:string):string
		sym = Symbol.symbol("concat");
		formals = new Types.RECORD(Symbol.symbol("s1"), STRING,
				new Types.RECORD(Symbol.symbol("s2"), STRING, null));
		result = STRING;
		vEnv.put(sym, new FunEntry(formals, result));
		//not(i:int):int
		sym = Symbol.symbol("not");
		formals = new Types.RECORD(Symbol.symbol("i"), INT, null);
		result = INT;
		vEnv.put(sym, new FunEntry(formals, result));
		//exit(i:int)
		sym = Symbol.symbol("exit");
		formals = new Types.RECORD(Symbol.symbol("i"), INT, null);
		result = VOID;
		vEnv.put(sym, new FunEntry(formals, result));
	}
}


