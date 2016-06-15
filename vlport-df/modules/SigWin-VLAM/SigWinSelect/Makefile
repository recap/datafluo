CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean SigWinSelectVL.so install

SigWinSelectVL.so:  SigWinSelectVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
	g++ -shared -Wl,-soname,SigWinSelectVL.so -o SigWinSelectVL.so SigWinSelectVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
SigWinSelectVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SigWinSelectVL.cpp
SigWinSelectVLTest.so:  SigWinSelectVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,SigWinSelectVLTest.so -o SigWinSelectVLTest.so SigWinSelectVLTest.o LogManager.o Parser.o
SigWinSelectVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SigWinSelectVLTest.cpp
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
