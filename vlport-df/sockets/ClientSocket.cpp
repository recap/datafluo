/* Implementation of the ClientSocket class
 * by Rob Tougher
 * http://linuxgazette.net/74/tougher.html
 */


#include "ClientSocket.h"
#include "SocketException.h"
#include "LogManager.H"


ClientSocket::ClientSocket ( std::string host, int port )
{
  if ( ! Socket::create() )
    {
      throw SocketException ( "Could not create client socket." );
    }

  if ( ! Socket::connect ( host, port ) )
    {
      throw SocketException ( "Could not bind to port." );
    }

}

/*ClientSocket::~ClientSocket(){
	if( Socket::is_valid() )
		Socket::~Socket();
}*/


const ClientSocket& ClientSocket::write(const char* buf, const int len) const{
	if( ! Socket::send(buf,len) )
	{
		LOG_PTAG(Error) << "Throwing socket exception";
      throw SocketException ( "Could not write to socket." );
    }

  return *this;

}

const ClientSocket& ClientSocket::read(char* buf, const int len)const{
	if( ! Socket::recv(buf,len) )
	{
		throw SocketException ("Could not read from socket.");
	}
	return *this;
}

const ClientSocket& ClientSocket::operator << ( const std::string& s ) const
{
  if ( ! Socket::send ( s ) )
    {
      throw SocketException ( "Could not write to socket." );
    }

  return *this;

}


const ClientSocket& ClientSocket::operator >> ( std::string& s ) const
{
  if ( ! Socket::recv ( s ) )
    {
      throw SocketException ( "Could not read from socket." );
    }

  return *this;
}
