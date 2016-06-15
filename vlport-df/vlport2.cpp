#include <iostream>
#include <fstream>
#include <malloc.h>
#include <math.h>
#include <sys/stat.h>
#include <time.h>

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <pthread.h>
#include <time.h>

#include "GlobalConfiguration.h"
#include "IModule.h"
#include "IComm.h"
#include "MessageQueue.h"
#include "TokenString.h"
#include "ReactorClient.h"
#include "LogManager.H"


#define HERE(NUM) cout << "HERE " << #NUM << endl
#define HERE2(VAR) cout<< "HERE " << #VAR  << ": " << VAR << endl

#define HOME "/home/reggie/workspace/vlport2/vlport2d/src/"
#define VL_HOME "/home/rcushing/local/vlport2/"


//#define _LOCAL
#define _LOOP
#define GC GlobalConfiguration

using namespace std;


//FUNC PROTOTYPES
int 			parse_args(int argc,char* argv[]);
int 			load_conf(char* filename);
int 			module_startup();
int 			server_entries_startup();
int 			reactor_queue_startup();
Server::Server* get_best_server();
//UTILITY PROTOTYPES
//template<class T>
int 			util_map_exists(string key, map<string, MessageQueue::MessageQueue *, less<string> > m);
//int 			util_map_exists(string key, map<string, T, less<string> > m);
int 			load_comm_lib(const char* path,const char* name);
//THREAD PROTOTYPES
void* 			module_start(void* ptr);
void*			icomm_start(void* icomm);
void* 			mailbox_listener(void* ptr);
void* 			module_output_listener(void* ptr);
void*			heart_beat_t(void* ptr);
//DEBUG PROTOTYPES
int 			debug_print_token_vector(vector<TokenString::TokenString*>* ts);
//GLOBALS
map<string, maker_t *, less<string> >           gModuleFactory;
map<string, cmaker_t *, less<string> >          gCommFactory;
map<string, void *, less<string> >				gSharedVariables;

string vl_home;

/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
int main(int argc, char *argv[]){
	LOG_PTAG(Info) << "Staring VLport2." <<  flush;

	int ret = 0;

	
	if( (ret = parse_args(argc,argv)) < 0)
		LOG_PTAG(Fatal) << "Parsing Arguments!" <<  flush;

	char *env = getenv("HOME");
	string env_v;
    env_v.append("LD_LIBRARY_PATH=");
    env_v.append((char *)env);
    env_v.append("/local/octave/lib/octave");
    putenv((char *)env_v.c_str());

	vl_home.assign((char *) env);
	vl_home.append("/local/vlport2/");

//	LOG_PTAG(Info) << "VL_HOME: " << vl_home << flush;
//	cout << "VL_HOME: " << vl_home << endl;


	//Get configuration file from server
	GC::gReactorClient = new ReactorClient(GC::gReactorIP,GC::gReactorPort);
	//if(ret != 1){
			string conf_path("/tmp/VL_");
			conf_path.append(GC::gInstanceID);
			conf_path.append(".conf");
			GC::gReactorClient->GetConfigFile(GC::gInstanceID,conf_path.c_str());
			load_conf((char*)conf_path.c_str());
	
	//}
	char* hostname=(char*)malloc(50);
	gethostname(hostname,50);	
	string log_file(MODULE_NAME+"_"+hostname+".log");	
	Log::open(log_file,Extreme);
	LOG_PTAG(Info) << "Running on host: " << hostname << flush;
	free(hostname);
	LOG_PTAG(Info) << "Module Instance: " << GC::gInstanceID;

	//create private working directory
	stringstream dirs;
	string inst_str(GC::gInstanceID);
	dirs << "VLDIR" << inst_str.substr(0,7);
	string dir(dirs.str());
	mkdir(dir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
	stringstream cmds;
	//cmds << "cp *.so " << dir << "/";
	//string cmd(cmds.str());
	//system(cmd.c_str());
	chdir(dir.c_str());
	pthread_t* heart_beat = (pthread_t*)malloc(sizeof(pthread_t));
	if(pthread_create(heart_beat,NULL,heart_beat_t,NULL))
				LOG_PTAG(Error) << "Failed to start heart beat.";

	//GC::gThreadPool["HEART_BEAT"] = heart_beat;
	

#ifdef _DEBUG
	GC::gModuleParam->PrintTokens();	
	debug_print_token_vector(&GC::gServerParam);
	debug_print_token_vector(&GC::gQueueParam);
#endif

	//Process server list
	if(server_entries_startup() < 0)
		LOG_PTAG(Fatal) << "Loading Server List!";
	//Startup module
	if(module_startup() < 0)
		LOG_PTAG(Fatal) << "Loading Module!";
	//Setup queues
	if(reactor_queue_startup() < 0)
		LOG_PTAG(Fatal) << "Loading Reactor Queues!";

		
//	sleep(1);
	//join threads
    map<string, pthread_t* , less<string> >::iterator itr;
    for(itr = GC::gThreadPool.begin(); itr != GC::gThreadPool.end(); ++itr)
	{
		int ret;
        pthread_t* t = itr->second;
        if(*t != 0)
        	ret = pthread_join(*t,NULL);
        else 
			LOG_PTAG(Info) << "found 0 pthread for " << itr->first;

		free(t);
		LOG_PTAG(Info) << "Joined Thread return value: " << ret;
    }

	sleep(5);
	
	LOG_PTAG(Info) << "Ending Module!";
	//system("rm -rf /tmp/VL_*");
	
	GC::gReactorClient->SendComplete((char*)GC::gInstanceID);
	LOG_PTAG(Info) << "Sent Complete!";

    //close *.so libs
//    map<string, void*, less<string> >::iterator litr;
//    for(litr = GC::gLibPool.begin(); litr != GC::gLibPool.end(); ++litr)
  //      dlclose(litr->second);

	Log::close();
	return 0;
}//main

/////////////////////////////////////////////////////////////////////////////////////////////////////
//template<class T> //TODO fix template
int util_map_exists(string key, map< string, MessageQueue::MessageQueue *, less<string> > m){
	map<string, MessageQueue::MessageQueue *, less<string> >::iterator found;
	found = m.find(key);
	if(found != m.end())
		return 0;
	else
	 	return -1;	
	
}//util_map_exists
/////////////////////////////////////////////////////////////////////////////////////////////////////

int reactor_queue_startup(){

	vector<TokenString::TokenString*>::iterator itr;
    for(itr = GC::gQueueParam.begin(); itr < GC::gQueueParam.end(); ++itr){
        Queue *queue_c = new Queue();
        queue_c->queue_uid = (*itr)->tokens.at(QUEUE_UID);
        queue_c->port_name = (*itr)->tokens.at(QUEUE_PORT_NAME);
        queue_c->direction = (*itr)->tokens.at(QUEUE_DIRECTION);
        queue_c->priority  = atoi((*itr)->tokens.at(QUEUE_PORT_PRIORITY).c_str());


        if(queue_c->direction == "in"){
	
            GC::gInQueue[queue_c->queue_uid] = queue_c;
		LOG_PTAG(Info) << queue_c->queue_uid <<" "<<queue_c->port_name<<" "<<queue_c->direction<<" "<<queue_c->priority;
			if(util_map_exists(queue_c->port_name, GC::gModulePortMap) < 0)
				LOG_PTAG(Error) << "Port " << queue_c->port_name << " not found on module!";

			ProducerConsumer::ProducerConsumer* pc = new ProducerConsumer::ProducerConsumer();
			pc->c = GC::gModulePortMap[queue_c->port_name];
			pc->c->setName((*itr)->tokens.at(QUEUE_UID).c_str());
			queue_c->pc = pc;
			//Start mailbox listener thread
			pthread_t* t = (pthread_t*)malloc(sizeof(pthread_t));

			if(pthread_create(t,NULL,mailbox_listener,queue_c))
				LOG_PTAG(Error) << "Failed to start mailbox thread for " << queue_c->queue_uid;
			GC::gThreadPool[queue_c->queue_uid + "MB"] = t;
			//sleep(1);
        }//if
	}//for

	//TODO synch with module after init
	sleep(3);


    for(itr = GC::gQueueParam.begin(); itr < GC::gQueueParam.end(); ++itr){
        Queue *queue_c = new Queue();
        queue_c->queue_uid = (*itr)->tokens.at(QUEUE_UID);
        queue_c->port_name = (*itr)->tokens.at(QUEUE_PORT_NAME);
        queue_c->direction = (*itr)->tokens.at(QUEUE_DIRECTION);
        queue_c->priority  = atoi((*itr)->tokens.at(QUEUE_PORT_PRIORITY).c_str());

	//start setting up output ports
        if((*itr)->tokens.at(QUEUE_DIRECTION) == "out"){
			GC::gOutQueue[queue_c->queue_uid] = queue_c;
			if(util_map_exists(queue_c->port_name, GC::gModulePortMap) < 0)
                LOG_PTAG(Error) << "Port " << queue_c->port_name << " not found on module!";

            ProducerConsumer::ProducerConsumer* pc = new ProducerConsumer::ProducerConsumer();
            pc->p = GC::gModulePortMap[queue_c->port_name];
			pc->p->setName((*itr)->tokens.at(QUEUE_UID).c_str());
			queue_c->pc = pc;
			pthread_t* t = (pthread_t*)malloc(sizeof(pthread_t));
			if(pthread_create(t,NULL,module_output_listener,queue_c))
				LOG_PTAG(Error) << "Failed to start output queue listener for " << queue_c->queue_uid;
			GC::gThreadPool[queue_c->queue_uid + "MB"] = t;
        }//if
    }//for

	//sleep(1);

	return 0;

}//reactor_queue_startup
/////////////////////////////////////////////////////////////////////////////////////////////////////
string message_id(Queue::Queue* queue_c){
	static int i = 0;
	i++;
	string name;
#ifdef _LOCAL
	name.append(HOME);
#else
	name.append(vl_home);
#endif
	name.append(MODULE_NAME + "_" + queue_c->port_name);
	stringstream ss;
	ss << i;
	name.append(ss.str());
	name.append(".rst");
	return name;	
}//message_id
/////////////////////////////////////////////////////////////////////////////////////////////////////

//@THREAD
void* heart_beat_t(void* ptr){
	srand(time(NULL));
	int rnd_num = 0;
	int sleep_time = 30;

	GC::gReactorClient->SendHeartBeat((char*)GC::gInstanceID);
	
	while(1){
		sleep_time = 30;
		rnd_num = rand() % 15 + 1;
		sleep_time += rnd_num;
		sleep(sleep_time);
		
			
		GC::gReactorClient->SendHeartBeat((char*)GC::gInstanceID);
		LOG_PTAG(Info) << "Send Heart Beat after " << sleep_time << "s" << endl;
		
	}//while

}//hear_beat
/////////////////////////////////////////////////////////////////////////////////////////////////////
//@THREAD
void* module_output_listener(void* ptr){
	Queue::Queue* queue_c = (Queue::Queue*)ptr;
	ProducerConsumer::ProducerConsumer* pc = queue_c->pc;

	if(pc->p == NULL)
		LOG_PTAG(Fatal) << "Output producer NULL!" << endl;
	//pre calculate pings
	get_best_server();

	//if(pc->c == NULL)
	//	LOG_PTAG(Fatal) << "Output consumer NULL!" << endl;

	LOG_PTAG(Info) << "Started output listener!";
	//TODO
#ifdef _LOOP
	while(1){
		MessageQueue::Message *m = pc->p->Read();
		if(m == NULL)
		{
			LOG_PTAG(Info) << "Read NULL!";
			break;
		}
#else
		MessageQueue::Message *m = pc->p->Read();
#endif
	
		if(m->mMessageId.compare("NONE") != 0){
			Server::Server* server_e = get_best_server();
			string lib_name;
			//TODO
			lib_name.append("Output");
			lib_name.append(server_e->type);
			string lib_path;
#ifdef _LOCAL
			lib_path.append(HOME + lib_name + ".so");
#else
			//lib_path.append("./"+lib_name + ".so");
			lib_path.append(vl_home+lib_name + ".so");
#endif

			if( gCommFactory[lib_name] == NULL )
        	load_comm_lib(lib_path.c_str(), lib_name.c_str());

			IComm *cport = gCommFactory[lib_name]();
			GC::gOutPort[queue_c->port_name][queue_c->priority].push_back(cport);
			TokenString::TokenString *tsp = new TokenString::TokenString( server_e->params, "?");
			cport->init(tsp->tokens);
			pc->c = cport->mQueue;
			m->mMessageId = message_id(queue_c);
			pc->c->Write(m);

		    //maybe thread this
			cport->start();
		
			ReactorClient::Mail* mail = new ReactorClient::Mail();
			mail->mMessage  = m->mMessageId; //This has been modified by cport
			mail->mQueueUid = pc->p->getName();

			GC::gReactorClient->SendMail(mail);
		}//if
		else{
			ReactorClient::Mail* mail = new ReactorClient::Mail();
			mail->mMessage.assign( (char*)m->mpData );
			mail->mQueueUid = pc->p->getName();
			GC::gReactorClient->SendMail(mail);
					
		}//esle

#ifdef _LOOP
	}//while
#endif
	
	//	sleep(5);

	return NULL;
	
}//module_output_listener
/////////////////////////////////////////////////////////////////////////////////////////////////////
//@THREAD
void* mailbox_listener(void* ptr){
	Queue* queue_c = (Queue*)ptr;
	static int count = 0;
	static long sleep_counter = 1;
	static int back_off = 1;
	LOG_PTAG(Info) << "Started listener: " << queue_c->queue_uid; 
#ifdef _LOOP
	while(1){
#endif
	//Check Mail
	if(queue_c->pc->c->state == 1){
		LOG_PTAG(Info) << "Consumer Aborted!";
		break;
	}
	ReactorClient::Mail* mail = GC::gReactorClient->CheckMail((char*)queue_c->queue_uid.c_str());
	if(mail == NULL){
		LOG_PTAG(Info) << "MAIL_NULL" << endl;
		queue_c->pc->c->Write(NULL);
		//sleep(5);
		#ifdef _LOOP	
		break;
		#endif
	}else{

	if(mail->mMessage.empty() == true){
		LOG_PTAG(Info) << "MAIL_EMPTY" << endl;

		back_off = (int)exp(sleep_counter)/(sleep_counter+1);
		
		if((back_off > 30) || (back_off < 0))
			back_off = 30;

		sleep_counter++;
		LOG_PTAG(Debug) << "Sleeping: " << back_off;
		sleep(back_off);
	}
	else{

	sleep_counter = 0;
	count++;
	LOG_PTAG(Detail) << "Received Message for queue: " << queue_c->port_name 
						<< "\n" << mail->toString() << endl;

	TokenString::TokenString *ts = new TokenString::TokenString( mail->mMessage, ":");
	//if(count == 1){
	string lib_name;
	lib_name.append("Input");
	lib_name.append(ts->tokens.at(MESSAGE_TYPE));
#ifdef _LOCAL
	string lib_path(HOME + lib_name + ".so");
#else
	//string lib_path("./" + lib_name + ".so");
	string lib_path(vl_home + lib_name + ".so");
#endif
	
	LOG_PTAG(Detail) << "Lib path: " << lib_path << endl;

	//TODO better loading
	if( gCommFactory[lib_name] == NULL )
        load_comm_lib(lib_path.c_str(), lib_name.c_str());

	IComm* cport = gCommFactory[lib_name]();
	GC::gInPort[queue_c->port_name][queue_c->priority].push_back(cport);

	//}//if

	//IComm* cport = (IComm*)GC::gInPort[queue_c->port_name][queue_c->priority].front();
	//cport = (IComm*)GC::gInPort[queue_c->port_name][queue_c->priority].front();

	TokenString::TokenString *tsp = new TokenString::TokenString( ts->tokens.at(MESSAGE_PARAM), "/");
	vector<string> str_params;
	str_params.push_back(tsp->tokens.at(0));//hostname
	string dir_param;
	for(int k = 1; k < tsp->tokens.size()-1; k++)//dir
		dir_param.append("/" + tsp->tokens.at(k));
	str_params.push_back(dir_param);
	str_params.push_back(tsp->tokens.back());//file

	LOG_PTAG(Debug) << "Params: " << str_params.at(0) << " " << str_params.at(1) << " "  << str_params.at(2) ;

	cport->init(str_params);
	queue_c->pc->p = (MessageQueue::MessageQueue*)cport->mQueue;

	//maybe thread this
	cport->start();
	
	//pthread_t* t = (pthread_t*)malloc(sizeof(pthread_t));
	//pthread_create(t,NULL,icomm_start,cport);
	//register thread
    //    stringstream countstr;
	//countstr << count;
	//GC::gThreadPool[queue_c->queue_uid+countstr.str()] = t;

	LOG_PTAG(Info) << "waiting to read" << flush;
	MessageQueue::Message* m = queue_c->pc->p->Read();
	queue_c->pc->c->Write(m);
	//wait for module to process message
	LOG_PTAG(Info) << "waiting for module";

	queue_c->pc->c->Wait();

/*	pthread_mutex_lock(&GC::module->condMutex);
	if(GC::module->condCounter <= 0) 
		pthread_cond_wait(&GC::module->condSync,&GC::module->condMutex);
	else
		GC::module->condCounter--;
	pthread_mutex_unlock(&GC::module->condMutex); */
	//sem_getvalue(GC::module->mSync, &sem_value);
	LOG_PTAG(Info) << "module go ahead";
	}//else
	}//else
#ifdef _LOOP
	}//while
#endif
	LOG_PTAG(Info) << "Ending MailBox listener!";

	//sleep(1);	
	return 0;

}//mailbox_listener


////////////////////////////////////////////////////////////////////////////////////////////////////
int load_comm_lib(const char* path,const char* name)
{
    void* dlib;
    if(GC::gLibPool[name] == NULL)
    {
        dlib = dlopen(path,RTLD_NOW);
        if(dlib == NULL){
            char *errstr;
            errstr = dlerror();
            if (errstr != NULL)
            LOG_PTAG(Error) << "A dynamic linking error occurred: " << errstr;
            return -1;
        }
        //reference to open *.so libs so as
        //to close and cleanup later.
        GC::gLibPool[name] = dlib;
    }else return 1;
    return 0;
}//load_comm_lib

/////////////////////////////////////////////////////////////////////////////////////////////////////
//@THREAD
void* icomm_start(void* icomm)
{
    IComm *c = (IComm*)icomm;
    c->start();
	return NULL;
}//icomm_start
/////////////////////////////////////////////////////////////////////////////////////////////////////

int module_startup(){
    void* dlib;
	//IModule* module;
	MODULE.insert(0,vl_home);
	LOG_PTAG(Info) << "Opening module: " << MODULE.c_str();
    dlib = dlopen(MODULE.c_str(),RTLD_NOW);
    if(dlib == NULL){
        char *errstr;
        errstr = dlerror();
        if (errstr != NULL)
        LOG_PTAG(Fatal) << "A dynamic linking error occurred: " << errstr;
		return -1;
    }
    GC::module = gModuleFactory[MODULE_NAME]();
    TokenString::TokenString *ts = new TokenString::TokenString(GC::gModuleParam->tokens.at(MODULE_PARAM),"?");
	//Initialise module with parameters
	
   	GC::module->init(&ts->tokens);
	//sleep(5);
	
	//exit(0);
	GC::gModuleInstances[GC::gInstanceID] = GC::module;
	
	for(int i = 1; i < MAX_PORTS; i++){
		if(GC::module->rx_ports[i] != NULL){
			MessageQueue::MessageQueue* mq = GC::module->rx_ports[i];
			GC::gModulePortMap[mq->getName()] = mq;
		}//if
		if(GC::module->tx_ports[i] != NULL){
			MessageQueue::MessageQueue* mq = GC::module->tx_ports[i];
			GC::gModulePortMap[mq->getName()] = mq;
		}//if
	}//for
	
	pthread_t* t = (pthread_t*)malloc(sizeof(pthread_t));
	//IModule* module = (IModule*)GC::gModuleInstances[GC::gInstanceID];
    pthread_create(t,NULL,module_start,GC::module);
    GC::gThreadPool[MODULE_NAME] = t;

    GC::gLibPool[MODULE_NAME] = dlib;

	sleep(1);

	return 0;
}//module_startup

/////////////////////////////////////////////////////////////////////////////////////////////////////
int server_entries_startup(){
    vector<TokenString::TokenString*>::iterator itr;

    for(itr = GC::gServerParam.begin(); itr < GC::gServerParam.end(); ++itr){
      	Server::Server *server_e = new Server::Server();

        server_e->id = ((*itr)->tokens.at(SERVER_ID));
        server_e->type = ((*itr)->tokens.at(SERVER_TYPE));
        server_e->metric = atoi( ((*itr)->tokens.at(SERVER_METRIC).c_str()) );
        server_e->params = ((*itr)->tokens.at(SERVER_PARAMS));

        GC::gServerList.push_back(server_e);
    }

    //sort(gServerList.begin(), gServerList.end(), server_entry_compare);

	return 0;

}//server_entrie_startup

/////////////////////////////////////////////////////////////////////////////////////////////////////
Server::Server* get_best_server(){
	static Server::Server* server_e;
	static int run_c;
	const int pingreps = 4;

	if(run_c == 1)
		return server_e;
	//GC::gReactorIP
	
	//vector<TokenString::TokenString*>::iterator itr;
	//for(itr = ts->begin(); itr != ts->end(); ++itr){
	//	(*itr)->PrintTokens();
	//}

	float best_ping = 10000;
	//Server::Server* best_server = NULL;

	vector<Server::Server*>::iterator itr;
	for(itr = GC::gServerList.begin(); itr != GC::gServerList.end(); ++itr){
		FILE* fstdout;
		char* sstdout;
		
		Server::Server* srv = (Server::Server*)(*itr);
		TokenString::TokenString *tsp = new TokenString::TokenString( srv->params, "?");
		string hostname = tsp->tokens.at(1);
		stringstream tmp;
		tmp << pingreps;
		string cmd;
		cmd.append("ping -c ");
		cmd.append(tmp.str());
		cmd.append(" ");
		cmd.append(hostname);
		cmd.append(" | awk '{print $8}' | egrep ^time | awk -F\"=\" '{print $2}'");

		fstdout = popen(cmd.c_str(),"r");
		sstdout = (char*)malloc(25);
		float pings[pingreps];

		LOG_PTAG(Info) << "CMD: " << cmd.c_str();

		for(int k =0;k < pingreps;k++){
			fgets(sstdout, 25, fstdout);
			pings[k] = atof(sstdout);
			//LOG_PTAG(Info) << "PING " << k << ": " << pings[k];
			//LOG_PTAG(Info) << "PING STR " << k << ": " << sstdout;
		}
		pclose(fstdout);

		float mean_ping = 0;
		for(int k =0;k < pingreps;k++)
			mean_ping += pings[k];

		mean_ping /= pingreps;

		LOG_PTAG(Info) << "server: " << hostname << " ping: " << mean_ping;

		if(mean_ping < best_ping){
			best_ping = mean_ping;
			server_e = srv;
			run_c = 1;
		}		
	}//for

	if(server_e == NULL){
		srand((unsigned)time(NULL));
		int rnd = int((double(rand())/RAND_MAX)*GC::gServerList.size()-1);	
		server_e = GC::gServerList[rnd];
	}

	return server_e;
}//get_best_server

/////////////////////////////////////////////////////////////////////////////////////////////////////

//@THREAD
void *module_start(void *imodule)
{
    IModule *m = (IModule*)imodule;
	if(m == NULL){
		LOG_PTAG(Fatal) << "Module pointer NULL!";
		exit(-1);
	}
		
    m->start();
	return NULL;
}//module_start

/////////////////////////////////////////////////////////////////////////////////////////////////////
int parse_args(int argc,char* argv[]){
	int largc=0;
	int ret=0;
	char* p;
	for(largc=1;largc < argc;largc++)
    {
        p = argv[largc];
       /* if(!strcmp(p,"-c"))
        {
            char *fdstr = argv[largc+1];
            if(load_conf(fdstr)<0)
				return -1;
			ret = 1;

        }//if*/
        if(!strcmp(p,"-s"))
            GC::gReactorIP = argv[largc+1];
        if(!strcmp(p,"-p"))
            GC::gReactorPort = atoi(argv[largc+1]);
        if(!strcmp(p,"-i"))
            GC::gInstanceID = argv[largc+1];
        if(!strcmp(p,"-h")){
            GC::gHost = argv[largc+1];
			gSharedVariables["HEAD_NODE"] = GC::gHost;
		}
		

    }//for

	LOG_PTAG(Info) << "Server: " <<GC::gReactorIP ;
	LOG_PTAG(Info) << "Port: " <<GC::gReactorPort ;
	LOG_PTAG(Info) << "Instance: " <<GC::gInstanceID ;
    LOG_PTAG(Info) << "HeadNode: " << GC::gHost ;	
	if(GC::gInstanceID == NULL)
		return -1;

	return ret;

    return 0;
}//parse_args
/////////////////////////////////////////////////////////////////////////////////////////////////////
int load_conf(char* filename){
	static int loaded = 0;
    string line;
	if(loaded > 0)
		return 0;
    ifstream file(filename);
	if(file.is_open() != true)
		return -1;
    while(!file.eof())
    {
        getline(file,line);
        if(line.size() > 0)
        {
              TokenString::TokenString *ts = new TokenString::TokenString(line,":");
              if(ts->tokens.at(0) == "Module")
                  GC::gModuleParam = ts;
			  //@Depreciated
              //if(ts->tokens.at(0) == "Port")
              //    GC::gPortParam.push_back(ts);
              if(ts->tokens.at(0) == "Queue")
                  GC::gQueueParam.push_back(ts);
              if(ts->tokens.at(0) == "Server")
                  GC::gServerParam.push_back(ts);

         }//if
    }//while
	loaded = 1;
	return 0;
}//load_conf

/////////////////////////////////////////////////////////////////////////////////////////////////////
int debug_print_token_vector(vector<TokenString::TokenString*>* ts){
	vector<TokenString::TokenString*>::iterator itr;
	for(itr = ts->begin(); itr != ts->end(); ++itr){
		(*itr)->PrintTokens();
	}
	return 0;
}//debug_print_token_vector
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
