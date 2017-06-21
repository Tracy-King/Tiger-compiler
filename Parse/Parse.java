package Parse;
import java.io.*;


public class Parse {

	public ErrorMsg.ErrorMsg errorMsg;
	public Absyn.Exp absyn;
	public PrintStream out;
	public PrintStream oup;
	public FileOutputStream f;
	public File file;
	public Absyn.Print print = new Absyn.Print(out);
	java_cup.runtime.Symbol tok;

	public Parse(String filename, String shortFilename) throws IOException 
	{
		errorMsg = new ErrorMsg.ErrorMsg(filename);
		{
			InputStream inp;
			try {
				inp=new FileInputStream(filename); 
			}
			catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: " + filename);
			}
       
			try {
				oup=new PrintStream(new FileOutputStream(new File(shortFilename + ".lex")), true);
			}
			catch (java.io.FileNotFoundException e) {
				inp.close();
				throw new Error("File not found: " + shortFilename + ".lex");
			}
       
			Lexer lexer = new Yylex(inp, errorMsg);
			do { 
				tok=lexer.nextToken();
				oup.print(symnames[tok.sym] + " " + tok.left +"\r\n");
			} while (tok.sym != sym.EOF);
			System.out.println("Lexer analysing finished");
			try{
				inp.close();
				inp=new FileInputStream(filename); 
			}
			catch (java.io.FileNotFoundException e) {
				throw new Error("File not found: " + filename);
			}
			lexer = new Yylex(inp, errorMsg);
       
			parser parser = new parser(lexer, errorMsg);
			/* open input files, etc. here */

			try {
				parser./*debug_*/parse();
			} catch (Throwable e) {
				e.printStackTrace();
				throw new Error(e.toString());
			} 
			finally {
				try {inp.close();} catch (java.io.IOException e) {}
			}
			System.out.println("Parsing finished");
			absyn = parser.parseResult;
		}
	}
  
	final static String symnames[] = new String[100];
	static 
	{
     symnames[sym.FUNCTION] = "FUNCTION";
     symnames[sym.EOF] = "EOF";
     symnames[sym.INT] = "INT";
     symnames[sym.GT] = "GT";
     symnames[sym.DIVIDE] = "DIVIDE";
     symnames[sym.COLON] = "COLON";
     symnames[sym.ELSE] = "ELSE";
     symnames[sym.OR] = "OR";
     symnames[sym.NIL] = "NIL";
     symnames[sym.DO] = "DO";
     symnames[sym.GE] = "GE";
     symnames[sym.error] = "error";
     symnames[sym.LT] = "LT";
     symnames[sym.OF] = "OF";
     symnames[sym.MINUS] = "MINUS";
     symnames[sym.ARRAY] = "ARRAY";
     symnames[sym.TYPE] = "TYPE";
     symnames[sym.FOR] = "FOR";
     symnames[sym.TO] = "TO";
     symnames[sym.TIMES] = "TIMES";
     symnames[sym.COMMA] = "COMMA";
     symnames[sym.LE] = "LE";
     symnames[sym.IN] = "IN";
     symnames[sym.END] = "END";
     symnames[sym.ASSIGN] = "ASSIGN";
     symnames[sym.STRING] = "STRING";
     symnames[sym.DOT] = "DOT";
     symnames[sym.LPAREN] = "LPAREN";
     symnames[sym.RPAREN] = "RPAREN";
     symnames[sym.IF] = "IF";
     symnames[sym.SEMICOLON] = "SEMICOLON";
     symnames[sym.ID] = "ID";
     symnames[sym.WHILE] = "WHILE";
     symnames[sym.LBRACK] = "LBRACK";
     symnames[sym.RBRACK] = "RBRACK";
     symnames[sym.NEQ] = "NEQ";
     symnames[sym.VAR] = "VAR";
     symnames[sym.BREAK] = "BREAK";
     symnames[sym.AND] = "AND";
     symnames[sym.PLUS] = "PLUS";
     symnames[sym.LBRACE] = "LBRACE";
     symnames[sym.RBRACE] = "RBRACE";
     symnames[sym.LET] = "LET";
     symnames[sym.THEN] = "THEN";
     symnames[sym.EQ] = "EQ";
   }
}




