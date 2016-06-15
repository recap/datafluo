#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include <sys/stat.h>
#include <fcntl.h>
#include <malloc.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>


using namespace std;

class InputUDP : public IComm {
private:
	string mIP;
	int mPort;
	FILE*  mUDP;
	char* mParam[30];
	void ParseParams(vector<string> rParam){
		mIP = rParam.at(1);
		mPort = atoi(rParam.at(2).c_str());
	}
	


public:
	InputUDP(){
		cout << "Started InputUDP" << endl;
	}
	void init(vector<string> rParam){
		cout << "InputUDP: init" << endl;
		ParseParams(rParam);
		mQueue = new MessageQueue("UDPQ");
		
	}
	void start(){
		cout << "InputUDP: start" << endl;

		int sockfd, newsockfd, portno;
		socklen_t clilen;
		char buffer[256];

		struct sockaddr_in serv_addr, cli_addr;
		int n;

		sockfd = socket(PF_INET,SOCK_DGRAM,IPPROTO_UDP);

		if(sockfd < 0)
			cout << "socket error!" << endl;
		bzero((char *) &serv_addr,sizeof(serv_addr));
		portno = mPort;
		serv_addr.sin_family = PF_INET;
		serv_addr.sin_addr.s_addr = INADDR_ANY;
		serv_addr.sin_port = htons(portno);
		if(bind(sockfd, (struct sockaddr* )&serv_addr,sizeof(serv_addr)) < 0)
			cout << "bind error!" << endl;
	//	listen(sockfd,5);

		clilen = sizeof(cli_addr);
	//	newsockfd = accept(sockfd,(struct sockaddr*)&cli_addr, &clilen);
		int size;
		int sz = recvfrom(sockfd,&size,sizeof(int),0,(struct sockaddr*)&cli_addr, &clilen);
		if(sz < 0)
			cout << "recvfrom error!" << endl;

		MessageQueue::Message *im1 = new MessageQueue::Message();
		im1->mDataLength = size;
		im1->mpData = malloc(im1->mDataLength);
		sz = recvfrom(sockfd,(void*)im1->mpData,im1->mDataLength,0,(struct sockaddr*)&cli_addr, &clilen);		
		mQueue->Write(im1);
		
		//cout << "Size: " << size << endl;

		//if(newsockfd < 0)
		//	cout << "accept error!" << endl;
		
		//bzero(buffer,256);
		//n = read(newsockfd,&size,sizeof(int));
		//if(n < 0 )
		//	cout << "read error!" << endl;

		//cout << "SIZE: " << size << endl;
		//MessageQueue::Message *im1 = new MessageQueue::Message();
		//im1->mDataLength = size;
		//im1->mpData = malloc(im1->mDataLength);
		//n = read(newsockfd,(void*)im1->mpData,im1->mDataLength);

		//mQueue->Write(im1);

		//cout << "message: " << buffer << endl;

		
		/*struct stat stFileInfo;
		int istat;
		FILE *fd = fopen(mUDPStr.c_str(),"rb");
		while(1){
		MessageQueue::Message* im1 = new MessageQueue::Message();
		
		//cout << "Opening reader!" << endl;
		//cout << "Opend!" << endl;
		fread(&im1->mDataLength,sizeof(int),1,fd);
	//	cout << "Data Length: " << im1->mDataLength << endl;
		im1->mpData = malloc(im1->mDataLength);
		fread((void*)im1->mpData,im1->mDataLength,1,fd);
	//	cout << "READ MESSAGE " << (char *) im1->mpData  << endl;

		mQueue->Write(im1);
		}*/
		
	}//start

};

REGISTER_LIB(InputUDP);
