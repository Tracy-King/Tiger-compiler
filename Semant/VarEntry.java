package Semant;

import Translate.Access;
import Types.Type;

public class VarEntry extends Entry {
    private Type ty;
    
    public VarEntry(Access a, Type t) {
       
        ty = t;
        super.setAccess(a);
    }


    public Type getTy() {
        return ty;
  }

    public void setTy(Type ty) {
        this.ty = ty;
    }  
}