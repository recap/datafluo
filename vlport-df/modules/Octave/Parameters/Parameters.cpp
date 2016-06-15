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



class Parameters : public IModule {
  private:
    string param_file;
	string host;	


  public:
    Parameters(){ 
//		INIT_SYNCHRONIZE();
		INIT_PORTS();
		MAP_TX_PORT(1,parameters);
		//tx_ports[1] = new MessageQueue("parameters");
	    };
    
    virtual ~Parameters() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();
   	

};


void Parameters::stop(){}

void Parameters::init(vector<string>* rParam){
	//1 = parameter_file
	//2 = _host
	//sleep(5);
	LOG_PTAG(Info) << "Prameters init!";
	param_file = rParam->at(1);

	host = rParam->at(2);
	

	LOG_PTAG(Info) << "param_file: " << param_file;
	LOG_PTAG(Info) << "_host: " << host;
	//LOG_PTAG(Info) << "Blabla" ;
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void Parameters::start()  {
	LOG_PTAG(Info) << "Parameters start()";
	
    TimeLag timing;
    timing.start("Parameters"); 

	string line;
	ifstream infile(param_file.c_str(), ios::in);
	if(!infile)
		LOG_PTAG(Error) << "Error reading file :" << param_file;
	while(!infile.eof()){
		line.clear();
		infile >> line;
		cout << line << endl;
		if(line.size() > 0){

		string message("Raw://"+line);
		LOG_PTAG(Info) << "Read parameter list: " << line;

		MessageQueue::Message* om1 = new MessageQueue::Message();
		om1->mMessageId.assign("NONE");
		om1->mDataLength = message.size();
		om1->mpData = (void*)malloc(om1->mDataLength);
		memcpy(om1->mpData,message.c_str(),om1->mDataLength);
		char* s = (char*)om1->mpData;
		s[om1->mDataLength] = '\0';
		LOG_PTAG(Detail) << "Wrting Params: " << (char*)om1->mpData << endl;
		WRITE_PORT(1,om1);
		//tx_ports[1]->Write(om1);
		}
	}
	WRITE_PORT(1,NULL);
	//tx_ports[1]->Write(NULL);
	 
   	timing.finish("Parameters");


};    
    

REGISTER_MODULE(Parameters);
//end_of_file
