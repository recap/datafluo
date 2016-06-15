CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean SWMedianVL.so install

SWMedianVL.so:  SWMedianVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
	g++ -shared -Wl,-soname,SWMedianVL.so -o SWMedianVL.so SWMedianVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
SWMedianVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SWMedianVL.cpp
SWMedianVLTest.so:  SWMedianVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,SWMedianVLTest.so -o SWMedianVLTest.so SWMedianVLTest.o LogManager.o Parser.o
SWMedianVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SWMedianVLTest.cpp
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
