package Parse;
import ErrorMsg.ErrorMsg;

%% 

%implements Lexer
%function nextToken
%type java_cup.runtime.Symbol
%char

%{
StringBuffer string = new StringBuffer();
int count;
private void newline() {
  errorMsg.newline(yychar);
}

private void err(int pos, String s) {
  errorMsg.error(pos,s);
}

private void err(String s) {
  err(yychar,s);
}

private java_cup.runtime.Symbol tok(int kind, Object value) {
    return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

private ErrorMsg.ErrorMsg errorMsg;

Yylex(java.io.InputStream s, ErrorMsg.ErrorMsg e) {
  this(s);
  errorMsg=e;
}

%}

%eofval{
	{if (yystate()==COMMENT) err("Comment symbol don't match");
	 if (yystate()==STRING) err("String presentatioin error!");
	 if (yystate()==STRING1) err("String presentation error!");
	 return tok(sym.EOF, null);
        }
%eofval}       

LineTerminator = \n | \r | \r\n | \n\r
Identifier = [a-zA-Z][:jletterdigit:]*
DecIntegerLiteral = [0-9]+
WhiteSpace = \n | \r | \r\n | \t | \f | \n\r

%state STRING
%state COMMENT
%state STRING1

%%

<YYINITIAL> \" {string.setLength(0); yybegin(STRING);}
<YYINITIAL> "/*" {count = 1; yybegin(COMMENT);}
<YYINITIAL> "*/" {err("Comment symbol don't match!");}
<YYINITIAL> " " {}
<YYINITIAL> {WhiteSpace} {}
<YYINITIAL> "," {return tok(sym.COMMA, null);}
<YYINITIAL> "/" {return tok(sym.DIVIDE, null);}
<YYINITIAL> ":" {return tok(sym.COLON, null);}
<YYINITIAL> "else" {return tok(sym.ELSE, null);}
<YYINITIAL> "do" {return tok(sym.DO, null);}
<YYINITIAL> "nil" {return tok(sym.NIL,null);}
<YYINITIAL> "|" {return tok(sym.OR, null);}
<YYINITIAL> ">" {return tok(sym.GT, null);}
<YYINITIAL> ">=" {return tok(sym.GE, null);}
<YYINITIAL> "<" {return tok(sym.LT, null);}
<YYINITIAL> "<=" {return tok(sym.LE, null);}
<YYINITIAL> "of" {return tok(sym.OF, null);}
<YYINITIAL> "-" {return tok(sym.MINUS, null);}
<YYINITIAL> "array" {return tok(sym.ARRAY, null);}
<YYINITIAL> "type" {return tok(sym.TYPE, null);}
<YYINITIAL> "for" {return tok(sym.FOR, null);}
<YYINITIAL> "to" {return tok(sym.TO, null);}
<YYINITIAL> "in" {return tok(sym.IN, null);}
<YYINITIAL> "end" {return tok(sym.END, null);}
<YYINITIAL> ":=" {return tok(sym.ASSIGN, null);}
<YYINITIAL> "." {return tok(sym.DOT, null);}
<YYINITIAL> "if" {return tok(sym.IF, null);}
<YYINITIAL> ";" {return tok(sym.SEMICOLON, null);}
<YYINITIAL> "while" {return tok(sym.WHILE, null);}
<YYINITIAL> "var" {return tok(sym.VAR, null);}
<YYINITIAL> "(" {return tok(sym.LPAREN, null);}
<YYINITIAL> ")" {return tok(sym.RPAREN, null);}
<YYINITIAL> "[" {return tok(sym.LBRACK, null);}
<YYINITIAL> "]" {return tok(sym.RBRACK, null);}
<YYINITIAL> "{" {return tok(sym.LBRACE, null);}
<YYINITIAL> "}" {return tok(sym.RBRACE, null);}
<YYINITIAL> "let" {return tok(sym.LET, null);}
<YYINITIAL> "+" {return tok(sym.PLUS, null);}
<YYINITIAL> "then" {return tok(sym.THEN, null);}
<YYINITIAL> "function" {return tok(sym.FUNCTION, null);}
<YYINITIAL>  "=" {return tok(sym.EQ, null);}
<YYINITIAL> "break" {return tok(sym.BREAK, null);}
<YYINITIAL> "&" {return tok(sym.AND, null);}
<YYINITIAL> "*" {return tok(sym.TIMES, null);}
<YYINITIAL> "<>" {return tok(sym.NEQ, null);}
<YYINITIAL> {Identifier} {return tok(sym.ID, yytext());}
<YYINITIAL> {DecIntegerLiteral} {return tok(sym.NUM, new Integer(yytext() ));}
<YYINITIAL> [^] {err("Illegal character < "+yytext()+" >!");}

<STRING>{
	\"	{yybegin(YYINITIAL); return tok(sym.STR, string.toString());}
	\\[0-9][0-9][0-9]
		{ int tmp = Integer.parseInt(yytext().substring(1,4));
		if(tmp > 255) err("exceed \\ddd");
		else string.append((char) tmp); }
	[^\n\t\"\\]+	{string.append(yytext());}
	\\t		{string.append('\t');}
	\\n		{string.append('\n');}
	\\\"	{string.append('\"');}
	\\\\	{string.append('\\');}
	{LineTerminator}	{err("String presentation error!");}
	\\		{yybegin(STRING1);}
}

<STRING1>{
	{WhiteSpace}	{}
	" "	{}
	\\	{yybegin(STRING);}
	\"	{err("\\don't match");}
	[^]	{string.append(yytext());}
}

<COMMENT>{
	"/*"	{count++;}
	"*/"	{count--; if(count == 0) {yybegin(YYINITIAL);}}
	[^]		{}
}