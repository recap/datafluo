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
#include "LogManager.H"


#define HERE(NUM) cout << "HERE " << #NUM << endl
#define HERE2(VAR) cout<< "HERE " << #VAR  << ": " << VAR << endl

//#define HOME "/home/reggie/VLPORT2/"
#define HOME "/home/rcushing/VLPORT2/"

#define GC GlobalConfiguration

using namespace std;


//FUNC PROTOTYPES
int 			parse_args(int argc,char* argv[]);
int 			load_comm_lib(const char* path,const char* name);
//THREAD PROTOTYPES
void*			icomm_start(void* icomm);
//DEBUG PROTOTYPES
int 			debug_print_token_vector(vector<TokenString::TokenString*>* ts);
//GLOBALS
map<string, maker_t *, less<string> >           gModuleFactory;
map<string, cmaker_t *, less<string> >          gCommFactory;
string mLibName;
string mLibParams;

string mLibNameO;
string mLibNameO;
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
int main(int argc, char *argv[]){

	int ret = 0;
	
	if( (ret = parse_args(argc,argv)) < 0)
		cerr << "Error" << endl;
	string libpath("../" + mLibName +".so");

	load_comm_lib(libpath.c_str(),mLibName.c_str());
	IComm *cport = gCommFactory[mLibName]();
    	TokenString::TokenString *tsp = new TokenString::TokenString( mLibParams, "?");
	
	cport->init(tsp->tokens);
	cport->start();

	MessageQueue::Message* m = cport->mQueue->Read();

	cout << "Message read\n" ;
	string libpathO("../" + mLibNameO +".so");

	load_comm_lib(libpathO.c_str(),mLibNameO.c_str());
	IComm *cportO = gCommFactory[mLibNameO]();
    	TokenString::TokenString *tspO = new TokenString::TokenString( mLibParamsO, "?");
	
	cportO->init(tspO->tokens);

	
	cportO->start();


		


}//main


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

int parse_args(int argc,char* argv[]){
	int largc=0;
	int ret=0;
	char* p;
	for(largc=1;largc < argc;largc++)
    {
        p = argv[largc];
        if(!strcmp(p,"-l"))
		mLibName = argv[largc+1];
        if(!strcmp(p,"-p"))
		mLibParams = argv[largc+1];
        if(!strcmp(p,"-lo"))
		mLibNameO = argv[largc+1];
        if(!strcmp(p,"-po"))
		mLibParamsO = argv[largc+1];

    }//for


    return 0;
}//parse_args
/////////////////////////////////////////////////////////////////////////////////////////////////////
int debug_print_token_vector(vector<TokenString::TokenString*>* ts){
	vector<TokenString::TokenString*>::iterator itr;
	for(itr = ts->begin(); itr != ts->end(); ++itr){
		(*itr)->PrintTokens();
	}
	return 0;
}//debug_print_token_vector
/////////////////////////////////////////////////////////////////////////////////////////////////////
