all: distclean
	javac *.java
	jar cfe Stately.jar Stately *.class

run: all
	java -jar Stately.jar

clean:
	-rm *.class

distclean: clean
	-rm Stately.jar
