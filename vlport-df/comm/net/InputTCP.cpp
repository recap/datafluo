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

class InputTCP : public IComm {
private:
	string mIP;
	int mPort;
	FILE*  mTCP;
	char* mParam[30];
	void ParseParams(vector<string> rParam){
		mIP = rParam.at(1);
		mPort = atoi(rParam.at(2).c_str());
	}
	


public:
	InputTCP(){
		cout << "Started InputTCP" << endl;
	}
	void init(vector<string> rParam){
		cout << "InputTCP: init" << endl;
		ParseParams(rParam);
		mQueue = new MessageQueue("TCPQ");
		
	}
	void start(){
		cout << "InputTCP: start" << endl;

		int sockfd, newsockfd, portno;
		socklen_t clilen;
		char buffer[256];

		struct sockaddr_in serv_addr, cli_addr;
		int n;

		sockfd = socket(AF_INET,SOCK_STREAM,0);
		if(sockfd < 0)
			cout << "socket error!" << endl;
		bzero((char *) &serv_addr,sizeof(serv_addr));
		portno = mPort;
		serv_addr.sin_family = AF_INET;
		serv_addr.sin_addr.s_addr = INADDR_ANY;
		serv_addr.sin_port = htons(portno);
		if(bind(sockfd, (struct sockaddr* )&serv_addr,sizeof(serv_addr)) < 0)
			cout << "bind error!" << endl;
		listen(sockfd,5);

		clilen = sizeof(cli_addr);
		newsockfd = accept(sockfd,(struct sockaddr*)&cli_addr, &clilen);

		if(newsockfd < 0)
			cout << "accept error!" << endl;
		
		bzero(buffer,256);
		size_t size;
		n = read(newsockfd,&size,sizeof(size_t));
		if(n < 0 )
			cout << "read error!" << endl;

		cout << "SIZE: " << size << endl;
		MessageQueue::Message *im1 = new MessageQueue::Message();
		im1->mDataLength = size;
		im1->mpData = malloc(im1->mDataLength);
		n = read(newsockfd,(void*)im1->mpData,im1->mDataLength);

		char* first = (char*)im1->mpData;
		cout << "FIRST :" << first << endl;

		mQueue->Write(im1);

		//cout << "message: " << buffer << endl;

		
		/*struct stat stFileInfo;
		int istat;
		FILE *fd = fopen(mTCPStr.c_str(),"rb");
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

REGISTER_LIB(InputTCP);
