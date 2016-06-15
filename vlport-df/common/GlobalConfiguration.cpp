#include "GlobalConfiguration.h"

#define GC GlobalConfiguration
#define MQ MessageQueue

map<string, pthread_t* , less<string> > 		GC::gThreadPool;
map<string, IModule *, less<string> > 			GC::gModuleInstances;
map<string, MQ::MQ *, less<string> > 			GC::gModulePortMap;
map<string, void* , less<string> > 				GC::gLibPool;
map<string, maker_t *, less<string> >::iterator GC::fitr;


TokenString::TokenString* 						GC::gModuleParam;
//@Depreciated
//vector<TokenString::TokenString*> 				GC::gPortParam;
vector<TokenString::TokenString*> 				GC::gQueueParam;
vector<TokenString::TokenString*> 				GC::gServerParam;

map< string, map< int, vector<IComm*> > > 		GC::gInPort;
map< string, map< int, vector<IComm*> > > 		GC::gOutPort;

vector< Server* > 								GC::gServerList;
map<string, Queue *, less<string> > 			GC::gInQueue;
map<string, Queue *, less<string> > 			GC::gOutQueue;


IModule* 										GC::module;
char* 											GC::gReactorIP	=	"127.0.0.1";
int 											GC::gReactorPort=	5555;
char* 											GC::gInstanceID;
char*											GC::gHost;

ReactorClient* 									GC::gReactorClient;
WarnType										GC::logLevel(Extreme);
