package Mips;
import Frame.Access;
import Tree.*;
import Temp.*;

public class InReg extends Access{
	private Temp reg;
	public InReg() {reg = new Temp();}
	public InReg(InReg inReg){
		this.reg = inReg.reg;
	}
	public InReg(Temp tmp){
		reg = tmp;
	}
	public Expr exp(Expr framePtr) {return new TEMP(reg);}
	public Expr expFromStack(Expr stackPtr) {return new TEMP(reg);}
}
