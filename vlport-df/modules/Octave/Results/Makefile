CC = g++
OCTAVE_INCLUDE_DIR=/home/rcushing/local/octave-3.2.4/include/octave-3.2.4
OCTAVE_LIB_DIR=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../ -I../../../messagequeue -I../include -I$(OCTAVE_LIB_DIR) -I$(OCTAVE_INCLUDE_DIR)
default: clean Results.so install

Results.so:  Results.o LogManager.o TimeLag.o Results.o TokenString.o
	$(CC) -L$(OCTAVE_LIB_DIR)  -loctinterp -lcruft -loctave -shared -Wl,-soname,Results.so -o Results.so Results.o LogManager.o TimeLag.o TokenString.o
Results.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb Results.cpp 
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
