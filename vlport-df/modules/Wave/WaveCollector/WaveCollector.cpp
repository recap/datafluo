#include <string>
#include <fstream>
#include <sstream>
#include "LogManager.H"
#include "TimeLag.H"
#include "CommonDefines.h"
#include "TokenString.h"
#include "IModule.h"
#include <string.h>
#include <stdio.h>
#include <ftw.h>

using namespace std;
int list_files(const char *name, const struct stat *status, int type); 

vector<string*> gMessages;


class WaveCollector : public IModule {
  private:
    stringstream *inStream; //> Input port: input file.
    stringstream *outStream; //> Output port: sequence of values.
    string directory;
	string host;	


  public:
    WaveCollector(){ 
		INIT_SYNCHRONIZE();
		putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");
		pthread_cond_init(&condSync,NULL);	
	//	inStream = new stringstream();
	//	outStream = new stringstream();
		for(int i = 0; i < MAXPORTS; i++){
			rx_ports[i] = NULL;
			tx_ports[i] = NULL;
		}		
		rx_ports[1] = new MessageQueue("input_file");
	    };
    
    virtual ~WaveCollector() throw() {
    	//delete outStream; 
    	//delete inStream;
		//sem_destroy(&mSync);
    }
	void init(vector<string>* rParam);
	void start();
	void stop();
   	

};


void WaveCollector::stop(){}

void WaveCollector::init(vector<string>* rParam){
	//1 = _farm
	//2 = _host
	//3 = directory
	host = rParam->at(2);
	directory = rParam->at(3);
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void WaveCollector::start()  {

    TimeLag timing;
    timing.start("WaveCollector");  
	//ftw((const char*)directory.c_str(),list_files,1);

	while(1){	
	MessageQueue::Message* om = rx_ports[1]->Read();
	if(om == NULL)
		break;
	
	char* s = (char*)om->mpData;
	s[om->mDataLength] = '\0';
	string url(s);
	TokenString::TokenString *ts = new TokenString::TokenString(url,"/");
	//string file_host = ts->tokens.at(1);
	string file_name = ts->tokens.back();
	//string file_dir;
	//for(int k = 2; k < ts->tokens.size()-1; k++)
	//	file_dir.append("/"+ts->tokens.at(k));

	string cmd("cp " + url +" "+directory+"/"+file_name);

	//string rurl("gsiftp://"+file_host+file_dir+"/"+file_name);
	//string lurl("gsiftp://"+host+directory+"/"+file_name);

	//string cmd("globus-url-copy " + rurl +" "+ lurl);
	LOG_PTAG(Debug) << "Command: " << cmd << endl;

	system(cmd.c_str());

	SYNCHRONIZE();
	
	}//while


   	/*vector<string*>::iterator itr;
	for(itr = gMessages.begin(); itr < gMessages.end(); ++itr){
		 string* s = (string*)(*itr);
		string file("PGsiFtp://"+host+"?"+ s->c_str());
		//LOG_PTAG(Info) << "File: " << file;

		MessageQueue::Message* om1 = new MessageQueue::Message();
		om1->mMessageId.assign("NONE");
		om1->mDataLength = file.size();
		om1->mpData = (void*)malloc(om1->mDataLength);
		memcpy(om1->mpData,file.c_str(),om1->mDataLength);
		//LOG_PTAG(Detail) << "Wrting file name: " << (char*)om1->mpData << endl;
		tx_ports[1]->Write(om1);
		//sleep(1);
	}
	tx_ports[1]->Write(NULL);*/
    
   
	 
   timing.finish("WaveCollector");


};    
    
int list_files(const char *name, const struct stat *status, int type) {
	if(type == FTW_F){
		string* file = new string();
		file->assign(name);
		gMessages.push_back(file);

	}

 return 0;
}

REGISTER_MODULE(WaveCollector);
//end_of_file
