CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../common/ -I../../messagequeue
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean myModule2.so install

myModule2.so:  myModule2.o
	g++ -shared -Wl,-soname,myModule2.so -o myModule2.so myModule2.o
myModule2.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb myModule2.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
