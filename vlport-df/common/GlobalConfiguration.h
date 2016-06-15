#ifndef _GLOBALCONFIGURATION_H_
#define _GLOBALCONFIGURATION_H_

#include <map>
#include <utility>
#include <vector>
#include <algorithm>
#include <string>
#include <list>

#include <pthread.h>

#include "IModule.h"
#include "IComm.h"
#include "MessageQueue.h"
#include "TokenString.h"
#include "ReactorClient.h"
#include "LogManager.H"

#define QUEUE_PORT_NAME 1
#define QUEUE_UID 2
#define QUEUE_DIRECTION 3
#define QUEUE_PORT_PRIORITY 4

#define MESSAGE_TYPE 0
#define MESSAGE_PARAM 1

#define SERVER_ID 1
#define SERVER_TYPE 2
#define SERVER_METRIC 3
#define SERVER_PARAMS 4

#define MODULE GlobalConfiguration::gModuleParam->tokens.at(3)
#define MODULE_NAME GlobalConfiguration::gModuleParam->tokens.at(1)
#define MODULE_PARAM 4 

#define MAX_PORTS 32

using namespace std;

class ProducerConsumer{
	public:
		MessageQueue* p;
		MessageQueue* c;
};

class Queue{
	public:
		string queue_uid;
		string port_name;
		string direction;
		int priority;
		vector<ReactorClient::Mail *> mailq;
		ProducerConsumer *pc;
};
class Server{
	public:
		string id;
		string type;
		int metric;
		string params;
};


class GlobalConfiguration
{
	public:
		static map<string, pthread_t *, less<string> > gThreadPool;
		static map<string, IModule *, less<string> > gModuleInstances;
		static map<string, MessageQueue::MessageQueue *, less<string> > gModulePortMap;
		static map<string, void* , less<string> > gLibPool;
		static map<string, maker_t *, less<string> >::iterator fitr;

		
		static TokenString::TokenString *gModuleParam;
		static vector<TokenString::TokenString*> gPortParam;
		static vector<TokenString::TokenString*> gQueueParam;
		static vector<TokenString::TokenString*> gServerParam;

		static map< string, map< int, vector<IComm*> > > gInPort;
		static map< string, map< int, vector<IComm*> > > gOutPort;

		static vector< Server* > gServerList;

		static map<string, Queue *, less<string> > gInQueue;
		static map<string, Queue *, less<string> > gOutQueue;

		static IModule* module;
		static char* gReactorIP;
		static int gReactorPort;
		static char* gInstanceID;
		static char* gHost; 

		static ReactorClient *gReactorClient;

		static WarnType logLevel;
};

#endif
