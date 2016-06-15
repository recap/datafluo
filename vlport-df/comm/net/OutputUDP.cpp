#include <iostream>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdlib.h>

using namespace std;

class OutputUDP : public IComm {
private:
	string  mIP;
	int	mPort;
	FILE*  mUDP;
	char** mParam;
	void ParseParams(vector<string> rParam){
		mIP = rParam.at(1);
		mPort = atoi(rParam.at(2).c_str());
	}
	


public:
	OutputUDP(){
		cout << "Started OutputUDP" << endl;
	}
	void init(vector<string> rParam){
		ParseParams(rParam);
		mQueue = new MessageQueue("UDPQ");
	//	cout << "UDP set to " << mUDPStr << endl;
	}
	void start(){
			cout << "OutUDP: start!" << endl;	
			int sockfd, portno,n;
			socklen_t clilen;
			struct sockaddr_in serv_addr, cli_addr;
			//struct  sockaddr serv_addr;
			struct hostent* server;
			char buffer[256];

			portno = mPort;
			sockfd = socket(PF_INET,SOCK_DGRAM,IPPROTO_UDP);
			if (sockfd < 0) 
				cout << "socket error!" << endl;
			server = gethostbyname(mIP.c_str());
			bzero((char *) &serv_addr, sizeof(serv_addr));
		    serv_addr.sin_family = PF_INET;
			bcopy((char *)server->h_addr,(char *)&serv_addr.sin_addr.s_addr,server->h_length);
    		serv_addr.sin_port = htons(portno);
			//if(bind(sockfd, (struct sockaddr* )&serv_addr,sizeof(serv_addr)) < 0)
            //	cout << "bind error!" << endl;
			//clilen = sizeof(cli_addr);
			if (connect(sockfd,(struct sockaddr*)&serv_addr,sizeof(serv_addr)) < 0)
				cout << "connect error!" << endl;
		
			clilen = 0;
			cli_addr = {0};	
			MessageQueue::Message *im1 = mQueue->Read();
		//	n = sendto(sockfd,&im1->mDataLength,sizeof(int),0,(struct sockaddr*)&cli_addr, clilen);
			n = write(sockfd,&im1->mDataLength,sizeof(int));
			if(n < 0)
				cout << "write header error! " << n << endl;
		//	n = sendto(sockfd,(void*)im1->mpData,im1->mDataLength,0,(struct sockaddr*)&cli_addr, clilen);
			n = write(sockfd,(void*)im1->mpData,im1->mDataLength);
			if(n < 0)
				cout << "write header error!" << endl;

			
		while(1)
		{
			MessageQueue::Message *im1 = mQueue->Read();
	/*		fwrite(&im1->mDataLength,sizeof(int),1,fd);
			//fwrite(&l,sizeof(int),1,fd);
			fwrite((void*)im1->mpData,im1->mDataLength,1,fd);
			fflush(fd);
			cout << "WRITING PIPE" << endl;*/
			
		}//while
	}//start

};

REGISTER_LIB(OutputUDP);
