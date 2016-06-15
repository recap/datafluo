CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../common/ -I../../messagequeue -I ../../common/
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<

default: clean InputRaw.so OutputRaw.so install

InputRaw.so:  InputRaw.o
	g++ -shared -Wl,-soname,InputRaw.so -o InputRaw.so InputRaw.o
InputRaw.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputRaw.cpp
OutputRaw.so:  OutputRaw.o
	g++ -shared -Wl,-soname,OutputRaw.so -o OutputRaw.so OutputRaw.o
OutputRaw.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputRaw.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
home:
	cp *.so ~/
