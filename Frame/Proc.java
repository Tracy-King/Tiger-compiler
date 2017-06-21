package Frame;

import Assem.InstrList;


public class Proc {
    String a, b;
    public InstrList body;

    public Proc(String name, InstrList bd, String t) {
	a = name; body = bd; b = t; 
    }
}
