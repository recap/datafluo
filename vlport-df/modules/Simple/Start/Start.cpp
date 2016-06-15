#include <string>
#include <fstream>
#include <sstream>
#include "LogManager.H"
#include "TimeLag.H"
#include "CommonDefines.h"
#include "IModule.h"
#include <string.h>
#include <stdio.h>

using namespace std;

vector<string> gMessages;


class Start : public IModule {
  private:
	string host;	
	string test_string;

  public:
    Start(){ 

		//INIT_SYNCHRONIZE();
		INIT_PORTS();
		MAP_TX_PORT(1,out);

	    };
    
    virtual ~Start() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();   	

};


void Start::stop(){}

void Start::init(vector<string>* rParam){
	//1 = _host
	//2 = directory
	test_string = rParam->at(1);
	host = rParam->at(2);
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void Start::start()  {

    TimeLag timing;
    timing.start("Start");  
	LOG_PTAG(Info) << "Start start";
	sleep(10);
	test_string.insert(0,"Raw://");

		MessageQueue::Message* om1 = new MessageQueue::Message();
		om1->mMessageId.assign("NONE");
		om1->mDataLength = test_string.size();
		om1->mpData = (void*)malloc(om1->mDataLength);
		memcpy(om1->mpData,test_string.c_str(),om1->mDataLength);
		char* tmp_s = (char*) om1->mpData;
		tmp_s[om1->mDataLength] = '\0';
		LOG_PTAG(Detail) << "Wrting file name: " << (char*)om1->mpData << endl;
		WRITE_PORT(1,om1);

	WRITE_PORT(1,NULL);
	 
   timing.finish("Start");


};    
    

REGISTER_MODULE(Start);
//end_of_file
