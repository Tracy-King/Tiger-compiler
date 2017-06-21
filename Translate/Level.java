package Translate;

import Mips.MipsFrame;
import Temp.Label;
import Util.BoolList;

public class Level {
	public Level parent;
	Frame.Frame frame;
	public AccessList formals;
	public AccessList frameFormals;
	
	public Level(Level parent, Label name, BoolList formals){
		this(parent, name, formals, false);
	}
	
	public Level(Level parent, Label name, BoolList formals, boolean leaf) {
	    this.parent = parent;
	    //is.frame = parent.frame.newFrame(name, new BoolList(!leaf, formals));
		Label label;
		if (name == null)
			label = new Label();
		else if (parent.frame.name != null)
			label = new Label(parent.frame.name + "." + name);
		else
			label = name;
	    this.frame = new MipsFrame(label, new BoolList(!leaf,formals));
	    frameFormals = allocFormals(this.frame.formals);
	    this.formals = frameFormals.tail;
	  }
	
	  private AccessList allocFormals(Frame.AccessList formals) {
	    if (formals == null)
	      return null;
	    return new AccessList(new Access(this, formals.head),
				  allocFormals(formals.tail));
	  }
	
	  public Level(Frame.Frame f) {
		    frame = f;
		  }

		  public Label name() {
		    return frame.name;
		  }

		  public Access allocLocal(boolean escape) {
		    return new Access(this, frame.allocLocal(escape));
		  }
}
