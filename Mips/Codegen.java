package Mips;

import Assem.Instr;
import Assem.InstrList;
import Temp.Label;
import Temp.LabelList;
import Temp.Temp;
import Temp.TempList;
import Temp.TempMap;
import Tree.CALL;
import Tree.CJUMP;
import Tree.CONST;
import Tree.EXP;
import Tree.ExpList;
import Tree.Expr;
import Tree.JUMP;
import Tree.LABEL;
import Tree.MEM;
import Tree.NAME;
import Tree.Stm;
import Tree.StmList;
import Tree.TEMP;

public class Codegen {
	MipsFrame frame;
    static final String MOVE_INST = "move `d0, `s0";
    static final String LOADI_INST = "li `d0, ";
    static final String LOADW_INST = "lw `d0, ";
    static final String STOREW_INST = "sw `s0, ";

    public Codegen(MipsFrame f) {
        frame = f;
    }

    static Instr OPER(String a, TempList d, TempList s) {
        return new Assem.OPER(a, d, s);
    }

    private static CONST CONST16(Expr e) {
        if (e instanceof CONST) {
            CONST c = (CONST) e;
            int value = c.value;
            if (value == (short) value) {
                return c;
            }
        }
        return null;
    }
    private InstrList ilist = null, last = null;

    private void emit(Instr inst) {
        if (last != null) {
            last = last.tail = new InstrList(inst, null);
        } else {
            if (ilist != null) {
                throw new Error("Codegen.emit");
            }
            last = ilist = new InstrList(inst, null);
        }
    }

    InstrList codegen(Stm s) {
        munchStm(s);
        InstrList l = ilist;
        ilist = last = null;
        return l;
    }

    static Instr MOVE(String a, Temp d, Temp s) {
        return new Assem.MOVE(a, d, s);
    }

    static TempList L(Temp h) {
        return new TempList(h, null);
    }

    static TempList L(Temp h, TempList t) {
        return new TempList(h, t);
    }
    
    public void munchStms(StmList slist) {
        StmList list = slist;
        for (; list != null; list = list.tail) {
            munchStm(list.head);
        }
    }

    void munchStm(Stm s) {
        if (s instanceof Tree.MOVE) {
            munchStm((Tree.MOVE) s);
        } else if (s instanceof EXP) {
            munchStm((EXP) s);
        } else if (s instanceof JUMP) {
            munchStm((JUMP) s);
        } else if (s instanceof CJUMP) {
            munchStm((CJUMP) s);
        } else if (s instanceof LABEL) {
            munchStm((LABEL) s);
        } else {
            throw new Error("Codegen.munchStm");
        }
    }

    void munchStm(CJUMP s) {
        Temp temp_esq = munchExp(s.left);
        Temp temp_dir = munchExp(s.right);

        emit(new Assem.OPER("", null, new TempList(temp_esq, new TempList(temp_dir, null))));

        /**
         * Para jumps "longe" necessita um label auxiliar
         */
        Label label_aux = new Label();

        /**
         * Verifica o tipo de jump para criar o codigo
         */
        switch (s.relop) {
            case CJUMP.EQ:
                emit(new Assem.OPER("beq `d0,`d1,L0", L(temp_esq, L(temp_dir , null)), null, new LabelList(label_aux, new LabelList(s.iftrue, new LabelList(s.iffalse, null)))));
                break;
            case CJUMP.NE:
                TempList l = L(temp_esq, L(temp_dir, null));
                emit(new Assem.OPER("bne `d0,`d1,L0", L(temp_esq, L(temp_dir , null)), null, new LabelList(label_aux, new LabelList(s.iftrue, new LabelList(s.iffalse, null)))));
                break;
            case CJUMP.LT:
                emit(new Assem.OPER("bltz `d0,L0", L(temp_esq, L(temp_dir , null)), null, new LabelList(label_aux, new LabelList(s.iftrue, new LabelList(s.iffalse, null)))));
                break;
            case CJUMP.LE:
                emit(new Assem.OPER("blez `d0,L0", L(temp_esq, L(temp_dir , null)), null, new LabelList(label_aux, new LabelList(s.iftrue, new LabelList(s.iffalse, null)))));
                break;
            case CJUMP.GT:
                emit(new Assem.OPER("bgtz `d0,L0", L(temp_esq, L(temp_dir , null)), null, new LabelList(label_aux, new LabelList(s.iftrue, new LabelList(s.iffalse, null)))));
                break;
            case CJUMP.GE:
                emit(new Assem.OPER("bgez `d0,L0", L(temp_esq, L(temp_dir , null)), null, new LabelList(label_aux, new LabelList(s.iftrue, new LabelList(s.iffalse, null)))));
                break;
        }

        /* faz o jump para false */
        emit(new Assem.LABEL(label_aux.toString() + ":", label_aux));
        emit(new Assem.OPER("j L1", null, null, new LabelList(s.iftrue, null)));

    }

    void munchStm(Tree.MOVE s) {
        if (s.dst instanceof TEMP) {
            Temp dest = ((TEMP) s.dst).temp;
            emit(OPER("add `d0,`s0,$0", L(dest),
                    L(munchExp(s.src))));
            return;
        }
        if (s.dst instanceof MEM) {
            Expr memDst = ((MEM) s.dst).exp;
            if (memDst instanceof CONST) {
                emit(OPER("sw `s1,0(`s0)", null, L(
                        munchExp(s.dst), L(munchExp(s.src), null))));
                return;
            }
            if (memDst instanceof Tree.BINOP && ((Tree.BINOP) memDst).binop == 0) {
                Tree.BINOP b = (Tree.BINOP) memDst;
                if (b.left instanceof CONST) {
                    emit(OPER("sw `s1," + ((CONST) b.left).value
                            + "(`s0)", null, L(munchExp(b.right), L(
                            munchExp(s.src)))));
                    return;
                }
                if (b.right instanceof CONST) {
                    emit(OPER("sw `s1,"
                            + ((CONST) b.right).value + "(`s0)", null, L(
                            munchExp(b.left), L(munchExp(s.src)))));
                    return;
                }
            }
            emit(OPER("sw `s1,0(`s0)", null, L(
                    munchExp(s.dst), L(munchExp(s.src), null))));
            return;
        }
        throw new Error("move node has illegal destination type");
    }

    void munchStm(EXP s) {
        munchExp(s.exp);
    }

    void munchStm(JUMP s) {
        if (s.exp instanceof NAME) {
            NAME name = (NAME) s.exp;
            Label label = name.label;
            emit(new Assem.OPER("j `j0", null, null, s.targets));
        } else {
            throw new Error("can't JUMP to a non-label");
        }
    }
 
    void munchStm(LABEL l) {
        emit(new Assem.LABEL(l.label.toString() + ":", l.label));
    }

    Temp munchExp(Expr s) {
        if (s instanceof CONST) {
            return munchExp((CONST) s);
        } else if (s instanceof NAME) {
            return munchExp((NAME) s);
        } else if (s instanceof TEMP) {
            return munchExp((TEMP) s);
        } else if (s instanceof Tree.BINOP) {
            return munchExp((Tree.BINOP) s);
        } else if (s instanceof MEM) {
            return munchExp((MEM) s);
        } else if (s instanceof CALL) {
            return munchExp((CALL) s);
        } else {
            throw new Error("Codegen.munchExp");
        }
    }

    Temp munchExp(CONST e) {
        Temp r = new Temp();
        emit(OPER("addi `d0,$0," + e.value, L(r), null));
        return r;
    }

    Temp munchExp(NAME e) {
        Temp r = new Temp();
        emit(OPER("la `d0," + e.label.toString() + "", L(r), null));
        return r;
    }

    Temp munchExp(TEMP e) {
        if (e.temp == frame.FP) {
            Temp t = new Temp();
            emit(OPER("addu `d0,`s0," + frame.name + "_framesize", L(t),
                    L(frame.SP)));
            return t;
        }
        return e.temp;
    }
    private static String[] BINOP = new String[10];

    static {
        BINOP[Tree.BINOP.PLUS] = "add";
        BINOP[Tree.BINOP.MINUS] = "sub";
        BINOP[Tree.BINOP.MUL] = "mulo";
        BINOP[Tree.BINOP.DIV] = "div";
        BINOP[Tree.BINOP.AND] = "and";
        BINOP[Tree.BINOP.OR] = "or";
        BINOP[Tree.BINOP.LSHIFT] = "sll";
        BINOP[Tree.BINOP.RSHIFT] = "srl";
        BINOP[Tree.BINOP.ARSHIFT] = "sra";
        BINOP[Tree.BINOP.XOR] = "xor";
    }

    private static int shift(int i) {
        int shift = 0;
        if ((i >= 2) && ((i & (i - 1)) == 0)) {
            while (i > 1) {
                shift += 1;
                i >>= 1;
            }
        }
        return shift;
    }

    Temp munchExp(Tree.BINOP e) {
        Temp r = new Temp();
        if (e.binop == 0) {
            if (e.left instanceof CONST) {
                emit(OPER("addi `d0,`s0," + ((CONST) e.left).value + "", L(r),
                        L(munchExp(e.right))));
                return r;
            }
            if (e.right instanceof CONST) {
                emit(OPER("addi `d0,`s0," + ((CONST) e.right).value + "", L(r),
                        L(munchExp(e.left))));
                return r;
            }
            emit(OPER("add `d0,`s0,`s1", L(r), L(munchExp(e.left),
                    L(munchExp(e.right)))));
            return r;
        }
        if (e.binop == 1) {
            emit(OPER("sub `d0,`s0,`s1", L(r), L(munchExp(e.left),
                    L(munchExp(e.right)))));
            return r;
        }
        emit(OPER(BINOP[e.binop] + " `d0,`s0,`s1", L(r), L(
                munchExp(e.left), L(munchExp(e.right)))));
        return r;
    }

    Temp munchExp(MEM e) {
        Temp r = new Temp();
        if (e.exp instanceof CONST) {
            emit(OPER("lw `d0," + ((CONST) e.exp).value
                    + "(`s0)", L(r), null));
            return r;
        }
        if (e.exp instanceof Tree.BINOP && ((Tree.BINOP) e.exp).binop == 0) {
            Tree.BINOP b = (Tree.BINOP) e.exp;
            if (b.left instanceof CONST) {
                emit(OPER("lw `d0,"
                        + ((CONST) b.left).value + "(`s0)", L(r),
                        L(munchExp(b.right))));
            }
            if (b.right instanceof CONST) {
                emit(OPER("lw `d0,"
                        + ((CONST) b.right).value + "(`s0)", L(r),
                        L(munchExp(b.left))));
            }
            return r;
        }
        emit(OPER("lw `d0,0(`s0)", L(r), L(munchExp(e.exp))));
        return r;
    }

    Temp munchExp(CALL s) {
        if (!(s.func instanceof NAME)) {
            throw new Error("Method name is not a NAME");
        }
        TempList argTemps = munchArgs(0, s.args);
        emit(new Assem.OPER("jal " + ((NAME) s.func).label,
                MipsFrame.calldefs, L(munchExp(s.func), argTemps)));
        return MipsFrame.V0;

    }

    private TempList munchArgs(int i, ExpList args) {
        if (args == null) {
            return null;
        }
        Temp src = munchExp(args.head);
        if (i > frame.maxArgs) {
            frame.maxArgs = i;
        }
        switch (i) {
            case 0:
                emit(MOVE("move `d0,`s0", frame.A0, src));
                break;
            case 1:
                emit(MOVE("move `d0,`s0", frame.A1, src));
                break;
            case 2:
                emit(MOVE("move `d0,`s0", frame.A2, src));
                break;
            case 3:
                emit(MOVE("move `d0,`s0", frame.A3, src));
                break;
            default:
                emit(OPER("sw `s0" + (i - 1) * frame.wordSize() + "(`s1)", null,
                        L(src, L(frame.SP))));
                break;
        }
        return L(src, munchArgs(i + 1, args.tail));
    }

    public InstrList getBareResult() {
        return ilist.reverse();
    }

    public String format(InstrList is, TempMap f) {
        String s = "";
        s = is.head.toString() + "";
        System.out.println(is.head);
        System.out.println(is.head.format(f));
        if (is.tail == null) {
            return s + is.head.format(f);
        } else {
            return s + is.head.format(f) + "" + format(is.tail, f);
        }
    }
}
