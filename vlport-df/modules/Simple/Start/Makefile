CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../messagequeue -I../include 
default: clean Start.so install

Start.so:  Start.o LogManager.o TimeLag.o Start.o
	$(CC) -L$(OCTAVE_LIB_DIR)  -loctinterp -lcruft -loctave -shared -Wl,-soname,Start.so -o Start.so Start.o LogManager.o TimeLag.o
Start.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb Start.cpp 
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
install_home:
	cp *.so ~/
install-all:
	cp *.so ~/local/vlport2/
