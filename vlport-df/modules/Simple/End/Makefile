CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall -fPIC
INCLUDES = -I../../common/ -I../../../ -I../../../messagequeue -I../include 
default: clean End.so install

End.so:  End.o LogManager.o TimeLag.o End.o TokenString.o
	$(CC)    -shared -Wl,-soname,End.so -o End.so End.o LogManager.o TimeLag.o TokenString.o
End.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb End.cpp 
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
install-all:
	cp *.so ~/local/vlport2/
