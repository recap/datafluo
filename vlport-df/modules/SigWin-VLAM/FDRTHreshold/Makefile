CC = g++
LIBS = -ldl
COMMON = ../common/
CFLAGS = -c -Wall $(GFLAGS)
INCLUDES = -I../../common/ -I../../../messagequeue -I../include
#.cc.o: 
#	$(CC) $(CCFLAGS) -ggdb -c  $<
default: clean FDRThresholdVL.so install

FDRThresholdVL.so:  FDRThresholdVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
	g++ -shared -Wl,-soname,FDRThresholdVL.so -o FDRThresholdVL.so FDRThresholdVL.o LogManager.o Parser.o HeaderIO.o GnuConfig.o TimeLag.o
FDRThresholdVL.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb FDRThresholdVL.cpp
FDRThresholdVLTest.so:  FDRThresholdVLTest.o LogManager.o Parser.o
	g++ -shared -Wl,-soname,FDRThresholdVLTest.so -o FDRThresholdVLTest.so FDRThresholdVLTest.o LogManager.o Parser.o
FDRThresholdVLTest.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb FDRThresholdVLTest.cpp
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/LogManager.C
Parser.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/Parser.C
HeaderIO.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/HeaderIO.C
GnuConfig.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/GnuConfig.C
TimeLag.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb ../include/TimeLag.C
install:
	cp *.so ../../../
clean:
	rm -f *.so *.o
