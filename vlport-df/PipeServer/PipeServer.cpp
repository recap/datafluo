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

using namespace std;
int gPort;
int parse_args(int argc, char* argv[]);
void* worker(void* ptr);
int i;
int main(int argc, char* argv[])
{
	i = 0;
	parse_args(argc,argv);

	int sockfd, *newsockfd, portno;
    socklen_t clilen;

    struct sockaddr_in serv_addr, cli_addr;

    sockfd = socket(AF_INET,SOCK_STREAM,0);
    if(sockfd < 0)
	    cout << "socket error!" << endl;
    bzero((char *) &serv_addr,sizeof(serv_addr));
    portno = gPort;
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);
    if(bind(sockfd, (struct sockaddr* )&serv_addr,sizeof(serv_addr)) < 0)
    	cout << "bind error!" << endl;
	while(1){
		pthread_t t;
    	listen(sockfd,5);
    	clilen = sizeof(cli_addr);
		newsockfd = (int*)malloc(sizeof(int));
    	*newsockfd = accept(sockfd,(struct sockaddr*)&cli_addr, &clilen);
		i++;
		if(newsockfd < 0)
            cout << "accept error!" << endl;
		else
			pthread_create(&t,NULL,worker,newsockfd);
	}



		
	
	

	
}//main

void *worker(void* ptr)
{
	if(i == 1)
		sleep(5);
	cout << "worker" <<endl;
	int* tmp = (int* )ptr;
	int sock = *tmp;
	
	int n = 0;	
	short cmd = 0;
	n = read(sock,&cmd,sizeof(short));
	if(n < 0)
		cout << "read error!" << endl;

	switch(cmd){
		case 1:
			cout << "OPEN" << endl;
			break;
		case 2:
			cout << "WRITE" << endl;
			break;
		case 3:
			cout << "READ" << endl;
			break;
		case 4:
			cout << "PEEK" << endl;
			break;
		case 5:
			cout << "CLOSE" << endl;
			break;
		default:
			cout << "unkwon command" << endl;
			
	}//switch
	return NULL;
		
}//worker

int parse_args(int argc, char* argv[])
{
	 int largc = 0;

    char *p;
    for(largc=1;largc < argc;largc++)
    {
        p = argv[largc];
        if(!strcmp(p,"-p"))
        {
			gPort = atoi(argv[largc+1]);

		}//if
	}//for
	return 0;

}//parse_args
