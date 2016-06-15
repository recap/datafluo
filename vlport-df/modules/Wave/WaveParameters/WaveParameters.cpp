#include <string>
#include <fstream>
#include <sstream>
#include "LogManager.H"
#include "TimeLag.H"
#include "CommonDefines.h"
#include "IModule.h"
#include <string.h>
#include <stdio.h>
#include <ftw.h>

#define HERE(NUM) cout << "HERE " << #NUM << endl
#define HERE2(VAR) cout<< "HERE " << #VAR  << ": " << VAR << endl


using namespace std;



class WaveParameters : public IModule {
  private:
	string host;	
	int min;
	int max;
	int step;

  public:
    WaveParameters(){ 
		INIT_SYNCHRONIZE();

		for(int i = 0; i < MAXPORTS; i++){
			rx_ports[i] = NULL;
			tx_ports[i] = NULL;
			min = max = step = -1;
		}		
		tx_ports[1] = new MessageQueue("tasknumber");
	    };
    
    virtual ~WaveParameters() throw() {
    }
	void init(vector<string>* rParam);
	void start();
	void stop();
   	

};


void WaveParameters::stop(){}

void WaveParameters::init(vector<string>* rParam){
	//1 = min
	//2 = max
	//3 = step
	//4 = _host
	LOG_PTAG(Info) << "WaveParameters init!";
	min = atoi(rParam->at(1).c_str());
	max = atoi(rParam->at(2).c_str());
	step = atoi(rParam->at(3).c_str());
	
	

	if(min < 0)
		LOG_PTAG(Error) << "wrong min value: " << min;
	if(max < 0)
		LOG_PTAG(Error) << "wrong max value: " << max;
	if(step < 0)
		LOG_PTAG(Error) << "wrong step value: " << step;
	

	LOG_PTAG(Info) << "WaveParams mim: " << min << " max: " << max << " step: " << step << endl;
	//LOG_PTAG(Info) << "_host: " << host;
}


void WaveParameters::start()  {
	LOG_PTAG(Info) << "WaveParameters start()";
	
    TimeLag timing;
    timing.start("WaveParameters");

	for(int i = min; i < max; i += step) {

		stringstream msgstr;
		msgstr << "Raw://" << i;
		string message(msgstr.str());

		MessageQueue::Message* om1 = new MessageQueue::Message();
		om1->mMessageId.assign("NONE");
		om1->mDataLength = message.size();
		om1->mpData = (void*)malloc(om1->mDataLength);
		memcpy(om1->mpData,message.c_str(),om1->mDataLength);
		char* s = (char*)om1->mpData;
		s[om1->mDataLength] = '\0';
		LOG_PTAG(Detail) << "Wrting Params: " << s << endl;
		tx_ports[1]->Write(om1);

	}//for

	tx_ports[1]->Write(NULL);
   	timing.finish("WaveParameters");
};    
    

REGISTER_MODULE(WaveParameters);
//end_of_file
