CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../common/ -I../../messagequeue -I ../../common/
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<

default: clean InputFile.so OutputFile.so install

InputFile.so:  InputFile.o
	g++ -shared -Wl,-soname,InputFile.so -o InputFile.so InputFile.o
InputFile.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb InputFile.cpp
OutputFile.so:  OutputFile.o
	g++ -shared -Wl,-soname,OutputFile.so -o OutputFile.so OutputFile.o
OutputFile.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb OutputFile.cpp
install:
	cp *.so ../../
clean:
	rm -f *.so *.o
