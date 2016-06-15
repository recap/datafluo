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

using namespace std;
int list_files(const char *name, const struct stat *status, int type); 

vector<string> gMessages;


class DirectoryReader : public IModule {
  private:
    string directory;
	string host;	


  public:
    DirectoryReader(){ 
		LOG_PTAG(Info) << "Setting LD_LIBRARY_PATH to  LD_LIBRARY_PATH=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4:$LD_LIBRARY_PATH";
		putenv("LD_LIBRARY_PATH=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4:$LD_LIBRARY_PATH");

		//INIT_SYNCHRONIZE();
		INIT_PORTS();
		MAP_TX_PORT(1,output_file);

	    };
    
    virtual ~DirectoryReader() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();   	

};


void DirectoryReader::stop(){}

void DirectoryReader::init(vector<string>* rParam){
	//1 = _host
	//2 = directory
	host = rParam->at(1);
	directory = rParam->at(2);
	//LOG_PTAG(Info) << "Directory: " << directory;
}


void DirectoryReader::start()  {

    TimeLag timing;
    timing.start("DirectoryReader");  
	LOG_PTAG(Info) << "DirectoryReader start";
	ftw((const char*)directory.c_str(),list_files,1);

   	vector<string>::iterator itr;
	for(itr = gMessages.begin(); itr < gMessages.end(); ++itr){
		 string s = (string)(*itr);
		string file("PGsiFtp://"+host+s.c_str());
		//LOG_PTAG(Info) << "File: " << file;

		MessageQueue::Message* om1 = new MessageQueue::Message();
		om1->mMessageId.assign("NONE");
		om1->mDataLength = file.size();
		om1->mpData = (void*)malloc(om1->mDataLength);
		memcpy(om1->mpData,file.c_str(),om1->mDataLength);
		char* tmp_s = (char*) om1->mpData;
		tmp_s[om1->mDataLength] = '\0';
		LOG_PTAG(Detail) << "Wrting file name: " << (char*)om1->mpData << endl;
		//tx_ports[1]->Write(om1);
		WRITE_PORT(1,om1);
		//sleep(1);
	}

	WRITE_PORT(1,NULL);
	//tx_ports[1]->Write(NULL);
    
   
	 
   timing.finish("DirectoryReader");


};    
    
int list_files(const char *name, const struct stat *status, int type) {
	if(type == FTW_F){
		//string* file = new string();
		//file->assign((const char*)name);
		string file((const char*)name);
		//this generates seg fault
		//file.append('\0');
		LOG_PTAG(Info) << "File: "<< file.c_str();
		gMessages.push_back(file);

	}

 return 0;
}

REGISTER_MODULE(DirectoryReader);
//end_of_file
