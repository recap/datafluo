#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <iostream>
#include <pthread.h>
#include <string.h>
#include <string>
#include <sys/stat.h>
#include <fcntl.h>
#include <malloc.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

using namespace std;
string gIP;
int gPort;
short gCmd;

int parse_args(int argc, char* argv[]);

int main(int argc, char* argv[])
{
	parse_args(argc,argv);

	int sockfd, portno;
	struct hostent* server;


    struct sockaddr_in serv_addr;

    sockfd = socket(AF_INET,SOCK_STREAM,0);
    if(sockfd < 0)
	    cout << "socket error!" << endl;
	server = gethostbyname(gIP.c_str());
    bzero((char *) &serv_addr,sizeof(serv_addr));
    portno = gPort;
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);
	if (connect(sockfd,(struct sockaddr*)&serv_addr,sizeof(serv_addr)) < 0)
                cout << "connect error!" << endl;

	int n = 0;
	n = write(sockfd,&gCmd,sizeof(short));
	if(n < 0)
                cout << "write error!" << endl;

	sleep(2);

}//main


int parse_args(int argc, char* argv[])
{
	 int largc = 0;

    char *p;
    for(largc=1;largc < argc;largc++)
    {
        p = argv[largc];
        if(!strcmp(p,"-p"))
			gPort = atoi(argv[largc+1]);
        if(!strcmp(p,"-h"))
			gIP = argv[largc+1];
        if(!strcmp(p,"-c"))
			gCmd = (short)atoi(argv[largc+1]);

	}//for
	return 0;

}//parse_args
