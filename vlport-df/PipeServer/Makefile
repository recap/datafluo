CC = g++
LIBS = -ldl
CFLAGS = -c -Wall
#INCLUDES = -I modules/common/ -I comm/common/ -I messagequeue/ -I common/
INCLUDES = 
#.cc.o: 
#	$(CC) -ggdb -c $<
default: clean PipeServer PipeServerTest

OBJS = PipeServer.o PipeServerTest.o

PipeServer: PipeServer.o
	$(CC) -ggdb -rdynamic -o PipeServer PipeServer.o -lpthread $(LIBS)
PipeServer.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb PipeServer.cpp

PipeServerTest: PipeServerTest.o
	$(CC) -ggdb -rdynamic -o PipeServerTest PipeServerTest.o -lpthread $(LIBS)
PipeServerTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb PipeServerTest.cpp
clean:
	rm -f *.o PipeServerTest PipeServer
