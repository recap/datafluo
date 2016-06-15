#include <iostream>
#include <fstream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "ClientSocket.h"
#include "SocketException.h"
#include "ReactorClient.h"
#include "LogManager.H"


using namespace std;


ReactorClient::ReactorClient(string rIP, int rPort){
	mIP = rIP;
	mPort = rPort;
}

ReactorClient::ReactorClient(){
	mIP = "127.0.0.1";
	mPort = 5555;
	//ReactorClient::ReactorClient("127.0.0.1", 5555);
}
ReactorClient::ReactorClient(string rIP){
	mIP = rIP;
	mPort = 5555;
	//ReactorClient::ReactorClient(rIP, 5555);
}


ClientSocket* ReactorClient::CreateSocket(){
	return new ClientSocket(mIP,mPort);
}

void ReactorClient::SendCommand(char* cmd, int len, ClientSocket* cs){
	try{

		ClientSocket* client_socket = cs;

		client_socket->write(cmd,len);

	}catch ( SocketException& ex) {
		LOG_PTAG(Error) << "Exception was caught: " << ex.description() << endl;
	}
}



void ReactorClient::SendCommand(char cmd, ClientSocket* cs)
{
	try{

		ClientSocket* client_socket = cs;

		client_socket->write((char *)&cmd,(int)sizeof(cmd));

	}catch ( SocketException& ex) {
		LOG_PTAG(Error) << "Exception was caught: " << ex.description() << endl;
	}

}

void ReactorClient::SendComplete(char* instanceId){
	char cmd[42];
	sprintf(cmd,"%c%s",REACTOR_COMPLETE,instanceId);
	ClientSocket* cs = CreateSocket();

	SendCommand( (char*) &cmd, 42,cs);

	delete cs;
	
}
void ReactorClient::SendHeartBeat(char* instanceId){
	char cmd[42];
	sprintf(cmd,"%c%s",REACTOR_HEART_BEAT,instanceId);
	ClientSocket* cs = CreateSocket();

	SendCommand( (char*) &cmd, 42,cs);

	delete cs;
	
}
ReactorClient::Mail* ReactorClient::CheckMail(char* queue_uid){

	int len = strlen(queue_uid);
	int nlen = htonl(len);
	int cmd_recv = 0;
	//mClientSocket = NULL;
	char c[1];
	sprintf(c,"%c",REACTOR_CHECK_MAIL);
	//printf("COMMAND: %s",c);
	ClientSocket* cs = CreateSocket();
	SendCommand((char*)&c,1,cs);
	SendCommand((char*)&nlen,sizeof(int),cs);
	SendCommand((char*)queue_uid,len,cs);

	int msg_len = -1;

	//read command (port_destroyed || sending_mail || no_mail)

	Mail* mail = new Mail();
	
	char cmd;
	cs->read((char*)&cmd,1);
	if(cmd == REACTOR_PORT_DESTROYED){
		LOG_PTAG(Info) << "Port Destroyed";
		mail = NULL;
		cmd_recv = 1;
	}
	if(cmd == REACTOR_SENDING_MAIL){
			LOG_PTAG(Info) << "Receiveing Mail";
			cs->read( (char *) &msg_len,sizeof(int));
			msg_len = ntohl(msg_len);

			char buf[msg_len];
			string *s1 = new string();
			cs->read( (char *) &buf, msg_len);
			
			//Null terminate
			buf[msg_len] = '\0';
			s1->append((char*)&buf,msg_len);
			

			string tmpstr(queue_uid);
			mail->mQueueUid = tmpstr;
			mail->mMessage = (string)*s1;
			cmd_recv = 1;

			//printf("Message: %s\n", buf);

	}//if
	if(cmd == REACTOR_NO_MAIL){
		LOG_PTAG(Info) << "No Mail";
		Mail* mail = new Mail();
		string s;// = new string();
		s.erase();	
		mail->mMessage = s;
		cmd_recv = 1;
	}
	if(cmd_recv == 0)
		LOG_PTAG(Info) << "Unknown Command: "  << cmd;

	delete cs;

	return mail;

	
}

void ReactorClient::GetConfigFile(char* instanceId, const char* filename){
	char cmd[42];
	ClientSocket* client_socket = CreateSocket();
	sprintf(cmd,"%c%s",REACTOR_GET_CONFIG,instanceId);
	SendCommand( (char*) &cmd, 42,client_socket);
	
	int return_length = 0;

	client_socket->read((char*)&return_length,sizeof(int));
	int con_int  = ntohl(return_length);

	//printf("DATA  LENGTH: 0x%8x %d %d\n", return_length, return_length, con_int);

	

	char buf[con_int];

	client_socket->read((char*)&buf,con_int);
	buf[con_int] = '\0';

	LOG_PTAG(Debug) << "configuration: " << buf << endl;


	ofstream file(filename, ios::out|ios::ate);
	file << buf << flush;

	file.close();
	delete client_socket;

}

void ReactorClient::SendMail(Mail* mail)
{
	int len = strlen(mail->mQueueUid.c_str());
	int len2 = strlen(mail->mMessage.c_str());
//	LOG_PTAG(Debug) << "Mail UID len: " << len << endl;
//	LOG_PTAG(Debug) << "Mail message len: " << len2 << endl;
	LOG_PTAG(Debug) << "Mail message: " << mail->mMessage;
	int nlen = htonl(len);
	int nlen2 = htonl(len2);
	char c[1];
	sprintf(c,"%c",REACTOR_POST_MAIL);
	ClientSocket* cs = CreateSocket();
	SendCommand((char*)&c,1,cs);
	SendCommand((char*)&nlen,sizeof(int),cs);
	SendCommand((char*)mail->mQueueUid.c_str(),len,cs);
	SendCommand((char*)&nlen2,sizeof(int),cs);
	SendCommand((char*)mail->mMessage.c_str(),len2,cs);

	delete cs;

}//SendMail
