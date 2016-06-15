CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../messagequeue -I../include 
default: clean WaveParameters.so install

WaveParameters.so:  WaveParameters.o LogManager.o TimeLag.o WaveParameters.o
	$(CC)  -shared -Wl,-soname,WaveParameters.so -o WaveParameters.so WaveParameters.o LogManager.o TimeLag.o
WaveParameters.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb WaveParameters.cpp 
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
home: 
	cp *.so ~/
