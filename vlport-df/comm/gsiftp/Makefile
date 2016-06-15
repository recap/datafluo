CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../common/ -I../../messagequeue -I ../../common/
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<

default: clean InputGsiFtp.so OutputGsiFtp.so install

InputGsiFtp.so:  InputGsiFtp.o
	g++ -shared -Wl,-soname,InputGsiFtp.so -o InputGsiFtp.so InputGsiFtp.o
InputGsiFtp.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputGsiFtp.cpp
OutputGsiFtp.so:  OutputGsiFtp.o
	g++ -shared -Wl,-soname,OutputGsiFtp.so -o OutputGsiFtp.so OutputGsiFtp.o
OutputGsiFtp.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputGsiFtp.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
