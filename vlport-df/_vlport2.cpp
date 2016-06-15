#include <iostream>
#include <fstream>
#include <malloc.h>

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <pthread.h>

#include "GlobalConfiguration.h"
#include "IModule.h"
#include "IComm.h"
#include "MessageQueue.h"
#include "TokenString.h"
#include "ReactorClient.h"

#define HERE(NUM) cout << "HERE " << #NUM << endl
#define HERE2(VAR) cout<< "HERE " << #VAR  << ": " << VAR << endl

#define HOME "/home/reggie/VLPORT2/"

#define GC: GlobalConfiguration:


using namespace std;

//func protos
int parse_args(int argc,char* argv[]);
void load_conf(char* filename);
void *icomm_start(void *icomm);
void *module_start(void *imodule);
int load_comm_lib(char* path,char* name);
void* queue_listener(void* ptr);
void mqueue_bind();
bool server_entry_compare(const ServerEntry* se1, const ServerEntry* se2);
void server_entries_start_up();
void reactor_queue_start_up();
void* reactor_queue_listener(void* ptr);
void port_in_start_up(QueueConfig* queue_c);
void port_out_start_up(QueueConfig* queue_c);
ServerEntry* get_best_server();

//Main
int main(int argc, char *argv[])
{
	cout << "Starting vlport2\n";
	parse_args(argc,argv);

	gReactorClient = new ReactorClient(gReactorIP,gReactorPort);
	string conf_path("/tmp/" + gInstanceID + ".conf");
	gReactorClient->GetConfigFile(gInstanceID,conf_path.c_str());
	load_conf(conf_path.c_str());

	server_entries_start_up();
	reactor_queue_start_up();
	module_start_up();

	
//	sleep(1);
	//gReactorClient = new ReactorClient(gReactorIP,gReactorPort);
	//gReactorClient->SendCommand(REACTOR_CHECK_MAIL);
	//exit(1);
	
	
//	port_start_up();
	mqueue_bind();
	
	//join threads
	map<string, pthread_t, less<string> >::iterator itr;
	for(itr = gThreadPool.begin(); itr != gThreadPool.end(); ++itr)
	{
		pthread_t t = itr->second;
		if(t != 0)
		pthread_join(t,NULL);
		else cout << "found 0 pthread for " << itr->first  <<endl;
	}

	//close *.so libs
	map<string, void*, less<string> >::iterator litr;
	for(litr = gLibPool.begin(); litr != gLibPool.end(); ++litr)
		dlclose(litr->second);

	cout << "exiting bye!" << endl;	

	return 0;
}


//Load *.so communication library 
int load_comm_lib(const char* path,const char* name)
{
	void* dlib;
	if(gLibPool[name] == NULL)
	{
		dlib = dlopen(path,RTLD_NOW);
    	if(dlib == NULL){
        	char *errstr;
        	errstr = dlerror();
        	if (errstr != NULL)
        	cerr << "A dynamic linking error occurred: " << errstr << endl;
			return -1;
    	}
		//reference to open *.so libs so as
		//to close and cleanup later.
    	gLibPool[name] = dlib;
	}else return 1;
	return 0;	
}//load_comm_lib

void port_out_start_up(QueueConfig* queue_c){
	ServerEntry* server_e = get_best_server();


	string s(HOME + server_e->serverType + ".so");
	
	if(gCommFactory[server_e->serverType] == NULL)
		load_comm_lib(s.c_str(), server_e->serverType.c_str());
	int port = queue_c->queue_no;
	int priority = 1; //TODO: remove hardcoded priority
	//call object constructor
	IComm *cport = gCommFactory[server_e->serverType]();

	gOutPort[port][priority].push_back(cport);

	TokenString::TokenString *tsp = new TokenString::TokenString( server_e->params, "?");

	cport->init(tsp->tokens);
	pthread_t t;
	//comm->start() thread.
	pthread_create(&t,NULL,icomm_start,cport);
	//register thread.
	gThreadPool[queue_c->queue_uid] = t;		


}

void port_in_start_up(QueueConfig* queue_c)
{
	
	TokenString::TokenString *ts = new TokenString::TokenString( queue_c->message, ":");
	//ts->PrintTokens();	
	string s(HOME + ts->tokens.at(MESSAGE_TYPE) + ".so");
	//cout << "LOADING LIB: " << s << endl;

	if( gCommFactory[ts->tokens.at(MESSAGE_TYPE)] == NULL )
		load_comm_lib(s.c_str(), ts->tokens.at(MESSAGE_TYPE).c_str());
	//else
	//	cout << "LIB NOT NULL" << endl;
	int port = queue_c->queue_no;
	int priority = 1; //TODO: remove hardcoded priority
	//call object constructor
	IComm *cport = gCommFactory[ts->tokens.at(MESSAGE_TYPE)]();


	gInPort[port][priority].push_back(cport);


	TokenString::TokenString *tsp = new TokenString::TokenString( ts->tokens.at(MESSAGE_PARAM), "?");

	//tsp->PrintTokens();

	cport->init(tsp->tokens);
	pthread_t t;
	//comm->start() thread.
	pthread_create(&t,NULL,icomm_start,cport);
	//register thread.
	gThreadPool[queue_c->queue_uid] = t;		

}//port_start_up2

//Thread wrapper for communication start
void *icomm_start(void *icomm)
{
	IComm *c = (IComm*)icomm;
	c->start();
	
}
//Thread wrapper for module start
void *module_start(void *imodule)
{
	IModule *m = (IModule*)imodule;
	m->start();
}

//Start module component, load so file, create instance
//start main module thread.
void module_start_up()
{
	void* dlib;
	pthread_t t;
	cout << "OPENING: " << MODULE.c_str() << endl;
	dlib = dlopen(MODULE.c_str(),RTLD_NOW);
	if(dlib == NULL){
		char *errstr;
		errstr = dlerror();
		if (errstr != NULL)
		cerr << "A dynamic linking error occurred: " << errstr << endl;
	}
	module = gModuleFactory[MODULE_NAME]();
	TokenString::TokenString *ts = new TokenString::TokenString(gModuleParam->tokens.at(MODULE_PARAM),"?");
	module->init(ts->tokens);
	gLibPool[MODULE_NAME] = dlib;

	pthread_create(&t,NULL,module_start,module);
	gThreadPool[MODULE_NAME] = t;	
}

//Bind communication ports to module ports
void mqueue_bind()
{
	cout << "binding queues" << endl;
	for(int i = 1; i < 32; i++)
	{
		if(module->rx_ports[i] != NULL)
		{
			ProdCons *pc = new ProdCons();
			pc->c = (MessageQueue*) module->rx_ports[i];
			vector<IComm*> vic;
			vector<IComm*>::iterator itr;
			vic = gInPort[i][1];	
			int k = 0;	
			for(itr = vic.begin(),k=0; itr < vic.end(); ++itr, k++)
			{
				IComm* c = (*itr);
				//cout << "BIND:" << (*itr)->s << endl;
				pthread_t t;
				char *s = (char*)malloc(20);
				//char s[25];
				sprintf(s,"IN:%d:%d:%d",i,1,k);
				//cout << s << endl;
				pc->name = s;
				pc->p = (MessageQueue*)(*itr)->mQueue;
				pthread_create(&t, NULL, queue_listener, (void*)pc);
				gThreadPool[s] = t;	
			}//for
		}//if
		if(module->tx_ports[i] != NULL)
		{
			ProdCons *pc = new ProdCons();
			pc->p = (MessageQueue*) module->tx_ports[i];
			vector<IComm*> vic;
			vector<IComm*>::iterator itr;
			vic = gOutPort[i][1];	
			int k = 0;	
			for(itr = vic.begin(),k=0; itr < vic.end(); ++itr, k++)
			{
				pthread_t t;
				//char s[25];
				//cout << "BIND:" << (*itr)->s << endl;
				char *s = (char*)malloc(20);
				sprintf(s,"OUT:%d:%d:%d",i,1,k);
				//cout << s << endl;
				pc->name = s;
				pc->c = (MessageQueue*)(*itr)->mQueue;

				pc->c->setName( (char*)gOutQueue[i].c_str() );

				pthread_create(&t, NULL, queue_listener, (void*)pc);
				gThreadPool[s] = t;	
			}//for
		}//if
	}//for
	
}

static  string get_message_id()
{
	static int i = 0;
	i++;
	char si[100];
	sprintf(si,"%s_file_%d.rst",MODULE_NAME.c_str(),i);
	string sk(si);
	string s(HOME + sk);
	return s;

}

//Thread for producer/consumer activity on each queue
void* queue_listener(void* ptr)
{
	ProdCons *pc;
	pc = (ProdCons*)ptr;
		cout << "L: " << pc->name << endl;
	while(1)
	{
		MessageQueue::Message *m = pc->p->Read();
		//set filename for out message
		if(m->mMessageId == ""){
			//out going message	
			m->mMessageId = get_message_id();	
			pc->c->Write(m);
			ReactorClient::Mail* mail = new ReactorClient::Mail();
			string s("InputFile://" + m->mMessageId);
			//mail->mMessage=m->mMessageId;
			mail->mMessage=s;
			mail->mQueueUid = pc->c->getName();
	
			gReactorClient->SendMail(mail);
			
		}
		else
			pc->c->Write(m);
		
		

		break;
	}
}

bool server_entry_compare(const ServerEntry* se1, const ServerEntry* se2){

		if( ( se1 != NULL ) && ( se2 != NULL) )
		{
			if(se1->metric < se2->metric) return true;
			else if(se1->metric > se2->metric) return false;
		}

		return true;
}

void server_entries_start_up(){

	vector<TokenString::TokenString*>::iterator itr;

	for(itr = gServerParam.begin(); itr < gServerParam.end(); ++itr){
		ServerEntry *server_e = new ServerEntry();

		server_e->serverId = ((*itr)->tokens.at(SERVER_ID));
		server_e->serverType = ((*itr)->tokens.at(SERVER_TYPE));
		server_e->metric = atoi( ((*itr)->tokens.at(SERVER_METRIC).c_str()) );
		server_e->params = ((*itr)->tokens.at(SERVER_PARAMS));

		gServerList.push_back(server_e);
	}

	//sort(gServerList.begin(), gServerList.end(), server_entry_compare);
	
}//server_entrie_start_up

void server_recalculate_metric()
{
	vector< ServerEntry* >::iterator itr;
	for(itr = gServerList.begin(); itr < gServerList.end(); ++itr)
	{
		ServerEntry* server_e = (ServerEntry*)(*itr);
		//TODO: get ping results
	}
	sort(gServerList.begin(), gServerList.end(), server_entry_compare);
}

ServerEntry* get_best_server(){
		ServerEntry* server_e;
		server_e = gServerList.back();
		return server_e;
}

void reactor_queue_start_up(){
	vector<TokenString::TokenString*>::iterator itr;
	
	for(itr = GC::gQueueParam.begin(); itr < GC::gQueueParam.end(); ++itr){
		Queue *queue_c = new Queue();
		//queue_c->queue_no = atoi((*itr)->tokens.at(QUEUE_NO).c_str());

		queue_c->queue_uid = (*itr)->tokens.at(QUEUE_UID);
		queue_c->port_name = (*itr)->tokens.at(QUEUE_PORT_NAME);
		queue_c->direction = (*itr)->tokens.at(QUEUE_DIRECTION);

		if(queue_c->direction == "in"){
			
			gInQueue[queue_c->queue_no] = queue_c->queue_uid;
			//Check Mail
			ReactorClient::Mail* mail = gReactorClient->CheckMail((char*)queue_c->queue_uid.c_str());
			cout << "Message: " << mail->mMessage << endl;
			
			queue_c->message = mail->mMessage;

			port_in_start_up(queue_c);
			
		}
		if((*itr)->tokens.at(QUEUE_DIR) == "out"){
			gOutQueue[queue_c->queue_no] = queue_c->queue_uid;

			port_out_start_up(queue_c);
			//TODO: Setup out queue from config servers
		}


	}//for
	
}


//thread
void* reactor_queue_listener(void* ptr){
	char *queue_uid = (char*)ptr;
	gReactorClient->CheckMail(queue_uid);
}//reactor_queue_listener


void load_conf(char* filename)
{
	string line;
    ifstream file(filename);
    while(!file.eof())
    {
    	getline(file,line);
        if(line.size() > 0)
        {
        	  TokenString::TokenString *ts = new TokenString::TokenString(line,":");
              if(ts->tokens.at(0) == "Module")
            	  gModuleParam = ts;
              if(ts->tokens.at(0) == "Port")
                  gPortParam.push_back(ts);
			  if(ts->tokens.at(0) == "Queue")
				  gQueueParam.push_back(ts);			  
			  if(ts->tokens.at(0) == "Server")
				  gServerParam.push_back(ts);			
				  
         }//if
    }//while
}//load_conf

//Parse command line arguments.
int parse_args(int argc, char *argv[])
{
	int largc = 0;
	char *p;

	//Defaults
	GC::gReactorIP = "127.0.0.1";
	GC::gReactorPort = 5555;
	for(largc=1;largc < argc;largc++)
	{
		p = argv[largc];
		if(!strcmp(p,"-c"))
		{
			char *fdstr = argv[largc+1];
			load_conf(fdstr);
			
		}//if
		if(!strcmp(p,"-s"))
			GC::gReactorIP = argv[largc+1];
		if(!strcmp(p,"-p"))
			GC::gReactorPort = atoi(argv[largc+1]);
		if(!strcmp(p,"-i"))
			GC::gInstanceID = argv[largc+1];
		
	}//for

	return 0;
}//parse_args

/////////////////////////////////////////////////////////////////////////////////////////////////////
