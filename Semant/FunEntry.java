package Semant;

import Translate.Level;
import Types.RECORD;
import Types.Type;

public class FunEntry extends Entry {

    private RECORD formals;
    private Type result;
    private Level level;

    public FunEntry(RECORD f, Type r) {
        formals = f;
        result = r;
        level = null;
    }

    FunEntry(Level newLevel, RECORD fields, Type type) {
        level = newLevel;
        formals = fields;
        result = type;
    }

    public RECORD getFormals() {
        return formals;
    }


    public void setFormals(RECORD formals) {
        this.formals = formals;
    }


    public Type getResult() {
        return result;
    }


    public void setResult(Type result) {
        this.result = result;
    }


    public Level getLevel() {
        return level;
    }
}
