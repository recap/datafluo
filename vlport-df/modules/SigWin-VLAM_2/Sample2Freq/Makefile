CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean Sample2FreqVL.so install

Sample2FreqVL.so:  Sample2FreqVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
	g++ -shared -Wl,-soname,Sample2FreqVL.so -o Sample2FreqVL.so Sample2FreqVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
Sample2FreqVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb Sample2FreqVL.cpp
Sample2FreqVLTest.so:  Sample2FreqVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,Sample2FreqVLTest.so -o Sample2FreqVLTest.so Sample2FreqVLTest.o LogManager.o Parser.o
Sample2FreqVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb Sample2FreqVLTest.cpp
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
Parser.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/Parser.C
HeaderIO.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/HeaderIO.C
GnuConfig.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/GnuConfig.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
