CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean RankVL.so install

RankVL.so:  RankVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
	g++ -shared -Wl,-soname,RankVL.so -o RankVL.so RankVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
RankVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb RankVL.cpp
RankVLTest.so:  RankVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,RankVLTest.so -o RankVLTest.so RankVLTest.o LogManager.o Parser.o
RankVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb RankVLTest.cpp
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
