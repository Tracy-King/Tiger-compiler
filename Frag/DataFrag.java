package Frag;

import Temp.Label;

public class DataFrag extends Frag {
	Label label = null;
	public String data = null;
	public DataFrag(Label label, String data)
	{
		this.label = label;
		this.data = data;
	}
	
	public DataFrag(String data)
	{
		this.data = data;
		label = null;
	}
}
