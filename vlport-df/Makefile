CC = g++
LIBS = -ldl
CFLAGS = -c -Wall -fPIC
INCLUDES = -I modules/common/ -I comm/common/ -I messagequeue/ -I common/ -I reactor/ -I sockets/
VLINCLUDES = -I common/
#.cc.o: 
#	$(CC) -ggdb -c $<
default: clean vlport2

OBJS = vlport2.o

vlport2: vlport2.o MessageQueue.o TokenString.o ReactorClient.o ClientSocket.o GlobalConfiguration.o LogManager.o
	$(CC)  -ggdb -rdynamic -o vlport2 vlport2.o ReactorClient.o MessageQueue.o TokenString.o  ClientSocket.o Socket.o GlobalConfiguration.o LogManager.o -lpthread $(LIBS)
vlport2.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb vlport2.cpp
ClientSocket.o: Socket.o
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb sockets/Socket.cpp sockets/ClientSocket.cpp
Socket.o: 
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb sockets/Socket.cpp
ReactorClient.o: ClientSocket.o
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb reactor/ReactorClient.cpp
MessageQueue.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb messagequeue/MessageQueue.cpp
TokenString.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb common/TokenString.cpp
GlobalConfiguration.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb common/GlobalConfiguration.cpp
LogManager.o:
	$(CC) $(CFLAGS) $(INCLUDES)  -ggdb common/LogManager.C

clean:
	rm -f *.o vlport2 vlportlogs/*.log *.rst myvlamlogs/*.log
allclean: clean
	rm *.so 
home:
	cp vlport2 ~/
install-all:
	cp *.so vlport2 ~/local/vlport2/
