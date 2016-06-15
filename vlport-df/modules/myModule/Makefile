CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../common/ -I../../messagequeue
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean myModule.so install

myModule.so:  myModule.o
	g++ -shared -Wl,-soname,myModule.so -o myModule.so myModule.o
myModule.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb myModule.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
