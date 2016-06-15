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


class ImageCollector : public IModule {
  private:
    string directory;
	string host;	


  public:
    ImageCollector(){ 

		//putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");
		//INIT_SYNCHRONIZE();
		INIT_PORTS();
		MAP_RX_PORT(1,input_file);
		//rx_ports[1] = new MessageQueue("input_file");
	    };
    
    virtual ~ImageCollector() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();
   	

};


void ImageCollector::stop(){}

void ImageCollector::init(vector<string>* rParam){
	//1 = _farm
	//2 = _host
	//3 = directory
	host = rParam->at(2);
	directory = rParam->at(3);
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void ImageCollector::start()  {

    TimeLag timing;
    timing.start("ImageCollector");  
	//ftw((const char*)directory.c_str(),list_files,1);

	while(1){	
	//MessageQueue::Message* om = rx_ports[1]->Read();
	MessageQueue::Message* om = READ_PORT(1);
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

	//SYNCHRONIZE();
	SIGNAL_RX_PORT(1);
	
	}//while

	 
   timing.finish("ImageCollector");


};    
    
int list_files(const char *name, const struct stat *status, int type) {
	if(type == FTW_F){
		string* file = new string();
		file->assign(name);
		gMessages.push_back(file);

	}

 return 0;
}

REGISTER_MODULE(ImageCollector);
//end_of_file
