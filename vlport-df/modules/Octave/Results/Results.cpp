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


class Results : public IModule {
  private:
    string results_file;
	string host;	


  public:
    Results(){ 

	//	putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");
	//	INIT_SYNCHRONIZE();
		INIT_PORTS();
		MAP_RX_PORT(1,results_string);
		//rx_ports[1] = new MessageQueue("results_string");
	    };
    
    virtual ~Results() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();
   	

};


void Results::stop(){}

void Results::init(vector<string>* rParam){
	//1 = reulsts_file
	//2 = _farm
	//3 = _host
	results_file = rParam->at(1);
	//host = rParam.at(2);
	//directory = rParam.at(3);
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void Results::start()  {

    TimeLag timing;
    timing.start("Results");  
	//ftw((const char*)directory.c_str(),list_files,1);
	ofstream results(results_file.c_str());
	results << "Image One\t" << "Image Two\t" << "Euclidean Diff\t" << "Bin Size" << endl << flush;	
	
	while(1){	
	//MessageQueue::Message* om = rx_ports[1]->Read();
	MessageQueue::Message* om = READ_PORT(1);
	if(om == NULL)
		break;
	
	char* s = (char*)om->mpData;
	s[om->mDataLength] = '\0';
	
	string result(s);
	results << result << endl << flush;
	//sleep(1);

//	SYNCHRONIZE();
	SIGNAL_RX_PORT(1);
	
	}//while

	results.close();

	timing.finish("Results");

};    
    
int list_files(const char *name, const struct stat *status, int type) {
	if(type == FTW_F){
		string* file = new string();
		file->assign(name);
		gMessages.push_back(file);

	}

 return 0;
}

REGISTER_MODULE(Results);
//end_of_file
