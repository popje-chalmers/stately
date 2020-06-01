all: distclean
	javac machine/*.java app/*.java
	jar cfe Stately.jar app.Stately machine/*.class app/*.class

run: all
	java -jar Stately.jar

clean:
	-rm machine/*.class app/*.class

distclean: clean
	-rm Stately.jar
