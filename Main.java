import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import Parse.Parse;

public class Main {
	static public PrintStream out, irOut, absOut,errorOut;
	static public File outFile, irOutFile, absOutFile,errorOutFile;

	public static void main(String[] args) throws IOException {	
		String originFilename = "";
		int select = 41;
		if(args.length != 0){
			originFilename = args[0];
			select = 1;
		}
		for(int j = 0; j < select; ++j){
			if(select == 41)
				originFilename = fileArgv[j];
			String filename = "";
			int i;
			for(i = 0; originFilename.charAt(i) != '\\'; ++i);
			filename = "Output\\" + originFilename.substring(i+1, originFilename.lastIndexOf(".tig"));
			System.out.println("Start compiling "+originFilename+"...");
			try {
				irOutFile = new File(filename + ".ir");
				irOut = new PrintStream(new FileOutputStream(irOutFile), true);
			}
			catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: " + filename + ".ir");
			}
			try {
				absOutFile = new File(filename + ".abs");
				absOut = new PrintStream(new FileOutputStream(absOutFile), true);
			}
			catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: " + filename + ".abs");
			}
			try {
				outFile = new File(filename + ".s");
				out = new PrintStream(new FileOutputStream(outFile), true);
			}
			catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: " + filename + ".s");
			}
			try{
				errorOutFile = new File(filename + ".err");
				errorOut = new PrintStream(new FileOutputStream(errorOutFile), true);
			} catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: " + filename + ".abs");
			}
		
			Parse parse = new Parse(originFilename, filename);
			Absyn.Print print = new Absyn.Print(absOut);
			print.prExp((Absyn.Exp)parse.absyn,1);
			absOut.close();
			Semant.Semant semant = new Semant.Semant(parse.errorMsg, errorOut);
			Frag.Frag frags = semant.transProg(parse.absyn);
			System.out.println("Semantic Ananlysing finished. Total error Number: "+semant.errorNum);
			if(semant.errorNum == 0)
				errorOut.println("No Semantic Error");
			
			errorOut.close();
			if(semant.errorNum != 0){
				System.out.println("Compiling Halted");
				out.close();
				irOut.close();
				continue;
			}
		
			out.print(".globl main");
			for (Frag.Frag f = frags; f != null; f = f.next){
				if (f instanceof Frag.ProcFrag)
					emitProc((Frag.ProcFrag) f);
				else if (f instanceof Frag.DataFrag)
					out.print(".data\r\n" + ((Frag.DataFrag) f).data);
			}
			out.print("\r\n");
			try{
				Reader reader=new FileReader("runtime.s");
				int ch = reader.read();
				while(ch!=-1){
					out.print((char)ch);
					ch=reader.read();
				}
				reader.close();
			}catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: runtime.s");
			}
			System.out.println("Compiling Success!");
			out.close();
			irOut.close();
		}
  } 
	
	static void emitProc(Frag.ProcFrag f) {
		Temp.TempMap tempMap = new Temp.CombineMap(f.frame, new Temp.DefaultMap());
		Tree.Print print = new Tree.Print(irOut, tempMap);
		irOut.print("function " + f.frame.name);
		print.prStm(f.body);
		irOut.println("\r\n");
		
		Tree.StmList stms = Canon.Canon.linearize(f.body);
		Canon.BasicBlocks b = new Canon.BasicBlocks(stms);
		Tree.StmList traced = (new Canon.TraceSchedule(b)).stms;
		Assem.InstrList instrs = codegen(f.frame, traced);
		
		
		instrs = f.frame.procEntryExit2(instrs);
	   /*RegAlloc.RegAlloc regAlloc = new RegAlloc.RegAlloc(f.frame, instrs, System.err, false);
		instrs = f.frame.procEntryExit3(instrs).body;
		Temp.TempMap tempmap = new Temp.CombineMap(f.frame, regAlloc);
		out.print(f.frame.pre());
        for (Assem.InstrList p = instrs; p != null; p = p.tail) {
            out.println(p.head.format(tempmap));
            out.flush();
        }
        out.print(f.frame.post());
        out.print(".end " + f.frame.name);
        out.flush()*/
		}
	
	static Assem.InstrList codegen(Frame.Frame f, Tree.StmList stms) {
        Assem.InstrList first = null, last = null;
        for (Tree.StmList s = stms; s != null; s = s.tail) {
            Assem.InstrList i = f.codegen(s.head);
            if (last == null) {
                if (first != null) {
                    throw new Error("Main.codegen");
                }
                first = last = i;
            } else {
                while (last.tail != null) {
                    last = last.tail;
                }
                last = last.tail = i;
            }
        }
        return first;
    }
	
	final static String fileArgv[] = new String[41];
	static {
		fileArgv[0] = "testcases\\Good\\test1.tig";
		fileArgv[1] = "testcases\\Good\\test2.tig";
		fileArgv[2] = "testcases\\Good\\test3.tig";
		fileArgv[3] = "testcases\\Good\\test4.tig";
		fileArgv[4] = "testcases\\Good\\test5.tig";
		fileArgv[5] = "testcases\\Good\\test6.tig";
		fileArgv[6] = "testcases\\Good\\test7.tig";
		fileArgv[7] = "testcases\\Good\\test8.tig";
		fileArgv[8] = "testcases\\Good\\test12.tig";
		fileArgv[9] = "testcases\\Good\\test30.tig";
		fileArgv[10] = "testcases\\Good\\test37.tig";
		fileArgv[11] = "testcases\\Good\\test41.tig";
		fileArgv[12] = "testcases\\Good\\test42.tig";
		fileArgv[13] = "testcases\\Good\\test44.tig";
		fileArgv[14] = "testcases\\Good\\test46.tig";
		fileArgv[15] = "testcases\\Good\\test47.tig";
		fileArgv[16] = "testcases\\Good\\test48.tig";
		fileArgv[17] = "testcases\\Good\\queens.tig";
		fileArgv[18] = "testcases\\Good\\merge.tig";
		
		fileArgv[19] = "testcases\\Bad\\test9.tig";
		fileArgv[20] = "testcases\\Bad\\test10.tig";
		fileArgv[21] = "testcases\\Bad\\test11.tig";
		fileArgv[22] = "testcases\\Bad\\test13.tig";
		fileArgv[23] = "testcases\\Bad\\test14.tig";
		fileArgv[24] = "testcases\\Bad\\test15.tig";
		fileArgv[25] = "testcases\\Bad\\test16.tig";
		fileArgv[26] = "testcases\\Bad\\test17.tig";
		fileArgv[27] = "testcases\\Bad\\test18.tig";
		fileArgv[28] = "testcases\\Bad\\test19.tig";
		fileArgv[29] = "testcases\\Bad\\test20.tig";
		fileArgv[30] = "testcases\\Bad\\test31.tig";
		fileArgv[31] = "testcases\\Bad\\test32.tig";
		fileArgv[32] = "testcases\\Bad\\test33.tig";
		fileArgv[33] = "testcases\\Bad\\test34.tig";
		fileArgv[34] = "testcases\\Bad\\test35.tig";
		fileArgv[35] = "testcases\\Bad\\test36.tig";
		fileArgv[36] = "testcases\\Bad\\test38.tig";
		fileArgv[37] = "testcases\\Bad\\test39.tig";
		fileArgv[38] = "testcases\\Bad\\test40.tig";
		fileArgv[39] = "testcases\\Bad\\test43.tig";
		fileArgv[40] = "testcases\\Bad\\test45.tig";
	}

}


