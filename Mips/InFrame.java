package Mips;
import Frame.Access;
import Tree.*;

public class InFrame extends Access{
	private MipsFrame frame;
	private int offset;
	public InFrame(int offset){
		this.offset = offset;
		frame = null;
	}
	
	public InFrame(MipsFrame frame, int offset){
		this.frame = frame;
		this.offset = offset;
	}
	
	public Expr exp(Expr framePtr){
		return new MEM(new BINOP(BINOP.PLUS, framePtr, new CONST(offset)));
	}
}
