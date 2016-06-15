CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean SigWinPlotGraceVL.so install

SigWinPlotGraceVL.so:  SigWinPlotGraceVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o Serializer.o
	g++ -shared -Wl,-soname,SigWinPlotGraceVL.so -o SigWinPlotGraceVL.so SigWinPlotGraceVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o serializer.o
SigWinPlotGraceVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SigWinPlotGraceVL.cpp
SigWinPlotGraceVLTest.so:  SigWinPlotGraceVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,SigWinPlotGraceVLTest.so -o SigWinPlotGraceVLTest.so SigWinPlotGraceVLTest.o LogManager.o Parser.o
SigWinPlotGraceVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb SigWinPlotGraceVLTest.cpp
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
Serializer.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/serializer.cc

install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
