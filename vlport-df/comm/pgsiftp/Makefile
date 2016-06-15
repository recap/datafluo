CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC $(GFLAGS)
INCLUDES = -I../common/ -I../../messagequeue -I ../../common/
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<

default: clean InputPGsiFtp.so OutputPGsiFtp.so install

InputPGsiFtp.so:  InputPGsiFtp.o
	g++ -shared -Wl,-soname,InputPGsiFtp.so -o InputPGsiFtp.so InputPGsiFtp.o
InputPGsiFtp.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputPGsiFtp.cpp
OutputPGsiFtp.so:  OutputPGsiFtp.o
	g++ -shared -Wl,-soname,OutputPGsiFtp.so -o OutputPGsiFtp.so OutputPGsiFtp.o
OutputPGsiFtp.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputPGsiFtp.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
