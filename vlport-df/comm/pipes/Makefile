CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../common/ -I../../messagequeue
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean InputPipe.so OutputPipe.so install

InputPipe.so:  InputPipe.o
	g++ -shared -Wl,-soname,InputPipe.so -o InputPipe.so InputPipe.o
InputPipe.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputPipe.cpp
OutputPipe.so:  OutputPipe.o
	g++ -shared -Wl,-soname,OutputPipe.so -o OutputPipe.so OutputPipe.o
OutputPipe.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputPipe.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
