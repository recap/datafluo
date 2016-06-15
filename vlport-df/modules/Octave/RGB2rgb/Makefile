CC = g++
OCTAVE_INCLUDE_DIR=/home/rcushing/local/octave-3.2.4/include/octave-3.2.4
OCTAVE_LIB_DIR=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../messagequeue -I../include -I$(OCTAVE_LIB_DIR) -I$(OCTAVE_INCLUDE_DIR)
default: clean RGB2rgb.so install

RGB2rgb.so:  RGB2rgb.o LogManager.o TimeLag.o RGB2rgb.o
	$(CC) -L$(OCTAVE_LIB_DIR)  -loctinterp -lcruft -loctave -shared -Wl,-soname,RGB2rgb.so -o RGB2rgb.so RGB2rgb.o LogManager.o TimeLag.o
RGB2rgb.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb RGB2rgb.cpp 
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
