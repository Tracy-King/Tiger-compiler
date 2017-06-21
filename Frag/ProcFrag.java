package Frag;

import Frame.Frame;
import Tree.Stm;

public class ProcFrag extends Frag{
	public Stm body;
	public Frame frame;
	public ProcFrag(Stm body, Frame frame)
	{
		this.body = body;
		this.frame = frame;
	}
}
