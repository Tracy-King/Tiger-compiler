JFLAGS=-g


Parse/Main.class: Parse/*.java
	javac ${JFLAGS} Parse/*.java

ErrorMsg/ErrorMsg.class:  ErrorMsg/*.java
	javac ${JFLAGS} ErrorMsg/*.java


