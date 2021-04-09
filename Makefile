all: distclean
	javac machine/*.java app/*.java
	jar cfe Stately.jar app.Stately machine/*.class app/*.class

run: all
	java -jar Stately.jar

clean:
	-rm -f machine/*.class app/*.class

distclean: clean
	-rm -f Stately.jar
