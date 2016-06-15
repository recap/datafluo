#include <stdio.h>
#include <zmq.hpp>
 
int main ()
{
    try {
        // Initialise 0MQ context with one application and one I/O thread
        zmq::context_t ctx (1, 1);
        // Create a ZMQ_REQ socket to send requests and receive replies
        zmq::socket_t s (ctx, ZMQ_REQ);
        // Connect it to port 5555 on localhost using the TCP transport
        s.connect ("tcp://localhost:5555");
 
        // Construct an example zmq::message_t with our query
        const char *query_string = "SELECT * FROM mytable";
        zmq::message_t query (strlen (query_string) + 1);
        memcpy (query.data (), query_string, strlen (query_string) + 1);
        // Send the query
        s.send (query);
 
        // Receive and display the result
        zmq::message_t resultset;
        s.recv (&resultset);
        const char *resultset_string = (const char *)resultset.data ();
        printf ("Received response: '%s'\n", resultset_string);
    }
    catch (std::exception &e) {
        // 0MQ throws standard exceptions just like any other C++ API
        printf ("An error occurred: %s\n", e.what());
        return 1;
    }
 
    return 0;
}
