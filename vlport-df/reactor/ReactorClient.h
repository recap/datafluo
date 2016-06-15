#ifndef _REACTORCLIENT_H_
#define _REACTORCLIENT_H_

#include <string.h>
#include <string>
#include "Socket.h"
#include "ClientSocket.h"
#include "ReactorCommands.h"

using namespace std;

class ReactorClient{
private:
	string mIP;
	int mPort;

	void SendCommand(char cmd,ClientSocket* cs);
	void SendCommand(char* cmd,int len,ClientSocket* cs);
	ClientSocket* CreateSocket();

public:
	struct Mail{
		//size_t mMessageLength;
		//char* mMessage;
		string mInstanceId;
		string mQueueUid;
		string mMessage;
		string toString(){
			string rs("Instance: " + mInstanceId + "\n" + 
						"QueueUID: " + mQueueUid + "\n" +
						"Message: " + mMessage);
			return rs;
		}
	};

	ReactorClient(string rIP, int rPort);
	ReactorClient(string rIP);
	ReactorClient();
	void GetConfigFile(char* instanceId, const char* filename);
	Mail* CheckMail(char* queue_uid);
	void SendMail(Mail* mail);
	void SendComplete(char* instanceId);
	void SendHeartBeat(char* instanceId);
};

#endif
