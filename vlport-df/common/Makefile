CC = g++
LIBS = -ldl
CFLAGS = -c -Wall
INCLUDES = -I modules/common/ -I comm/common/ -I messagequeue/
#.cc.o: 
#	$(CC) -ggdb -c $<
default: clean test 

OBJS = test.o

test: test.o TokenString.o
	$(CC) -ggdb -rdynamic -o test test.o TokenString.o -lpthread $(LIBS)
test.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb test.cpp
TokenString.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb TokenString.cpp

clean:
	rm -f *.o test
