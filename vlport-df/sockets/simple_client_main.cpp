#include "ClientSocket.h"
#include "SocketException.h"
#include <iostream>
#include <string>

int main ( int argc, int argv[] )
{
  try
    {

      //ClientSocket client_socket ( "localhost", 30000 );
      ClientSocket client_socket ( "localhost", 5555 );

      std::string reply;

      try
	{
		char b = 0x70;
		//b= htons(b);
	  client_socket.write((char *)&b,(int)sizeof(b));
	  //client_socket << b;
	  client_socket << "Test message.";
	  client_socket >> reply;
	}
      catch ( SocketException& ) {}

      std::cout << "We received this response from the server:\n\"" << reply << "\"\n";;

    }
  catch ( SocketException& e )
    {
      std::cout << "Exception was caught:" << e.description() << "\n";
    }

  return 0;
}
