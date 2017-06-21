package Tree;

public abstract class Expr {
	  abstract public ExpList kids();
	  abstract public Expr build(ExpList kids);
}
