CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall
INCLUDES = -I../common/ -I../../messagequeue
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean InputTCP.so OutputTCP.so InputUDP.so OutputUDP.so install

InputTCP.so:  InputTCP.o
	g++ -shared -Wl,-soname,InputTCP.so -o InputTCP.so InputTCP.o
InputTCP.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputTCP.cpp
OutputTCP.so:  OutputTCP.o
	g++ -shared -Wl,-soname,OutputTCP.so -o OutputTCP.so OutputTCP.o
OutputTCP.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputTCP.cpp
InputUDP.so:  InputUDP.o
	g++ -shared -Wl,-soname,InputUDP.so -o InputUDP.so InputUDP.o
InputUDP.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputUDP.cpp
OutputUDP.so:  OutputUDP.o
	g++ -shared -Wl,-soname,OutputUDP.so -o OutputUDP.so OutputUDP.o
OutputUDP.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputUDP.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
