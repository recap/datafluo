CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../ -I../../../messagequeue -I../include
default: clean WaveCollector.so install

WaveCollector.so:  WaveCollector.o LogManager.o TimeLag.o WaveCollector.o TokenString.o
	$(CC) -shared -Wl,-soname,WaveCollector.so -o WaveCollector.so WaveCollector.o LogManager.o TimeLag.o TokenString.o
WaveCollector.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb WaveCollector.cpp 
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
TokenString.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../../../common/TokenString.cpp
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
