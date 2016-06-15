CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../messagequeue -I../include 
default: clean Wave64.so install

Wave64.so:  Wave64.o LogManager.o TimeLag.o Wave64.o
	$(CC) -shared -Wl,-soname,Wave64.so -o Wave64.so Wave64.o LogManager.o TimeLag.o
Wave64.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb Wave64.cpp 
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
