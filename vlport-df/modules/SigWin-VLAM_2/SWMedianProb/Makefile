CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean SWMedianProbVL.so install

SWMedianProbVL.so:  SWMedianProbVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
	g++ -shared -Wl,-soname,SWMedianProbVL.so -o SWMedianProbVL.so SWMedianProbVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
SWMedianProbVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SWMedianProbVL.cpp
SWMedianProbVLTest.so:  SWMedianProbVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,SWMedianProbVLTest.so -o SWMedianProbVLTest.so SWMedianProbVLTest.o LogManager.o Parser.o
SWMedianProbVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SWMedianProbVLTest.cpp
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
