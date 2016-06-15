CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../common/ -I../../messagequeue
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean PipeServer.so install

PipeServer.so:  PipeServer.o
	g++ -shared -Wl,-soname,PipeServer.so -o PipeServer.so PipeServer.o
PipeServer.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb PipeServer.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
