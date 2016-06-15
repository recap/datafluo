CC = g++
OCTAVE_INCLUDE_DIR=/home/rcushing/local/octave-3.2.4/include/octave-3.2.4
OCTAVE_LIB_DIR=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../messagequeue -I../include -I$(OCTAVE_LIB_DIR) -I$(OCTAVE_INCLUDE_DIR)
default: clean rgb2I1I2I3.so install

rgb2I1I2I3.so:  rgb2I1I2I3.o LogManager.o TimeLag.o rgb2I1I2I3.o
	$(CC) -L$(OCTAVE_LIB_DIR)  -loctinterp -lcruft -loctave -shared -Wl,-soname,rgb2I1I2I3.so -o rgb2I1I2I3.so rgb2I1I2I3.o LogManager.o TimeLag.o
rgb2I1I2I3.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb rgb2I1I2I3.cpp 
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
