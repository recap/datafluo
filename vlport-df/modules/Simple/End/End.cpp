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

vector<string*> gMessages;


class End : public IModule {
  private:
    string results_file;
	string host;	


  public:
    End(){ 

		INIT_PORTS();
		MAP_RX_PORT(1,in);
	    };
    
    virtual ~End() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();
   	

};


void End::stop(){}

void End::init(vector<string>* rParam){
	//1 = reulsts_file
	//2 = _farm
	//3 = _host
	results_file = rParam->at(1);
	//host = rParam.at(2);
	//directory = rParam.at(3);
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void End::start()  {

    TimeLag timing;
    timing.start("End");  
	//ftw((const char*)directory.c_str(),list_files,1);
	ofstream results(results_file.c_str());

	//results << "Image One\t" << "Image Two\t" << "Euclidean Diff\t" << "Bin Size" << endl << flush;	
	results << "Simple Results" << endl;
	results << "-------------------------------------------" << endl;
	
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

	timing.finish("End");

};    
    

REGISTER_MODULE(End);
//end_of_file
