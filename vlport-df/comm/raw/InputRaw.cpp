#include <iostream>
#include <stdio.h>
#include <string.h>
#include <fstream>
#include <unistd.h>
#include <cstdlib>
#include "CommonDefines.h"
#include "IComm.h"
#include "malloc.h"
#include "LogManager.H"

#define HERE(NUM) cout << "HERE " << #NUM << endl
#define HERE2(VAR) cout<< "HERE " << #VAR  << ": " << VAR << endl


using namespace std;

class InputRaw : public IComm {
private:
	vector<string> params;
	void ParseParams(vector<string> rParam){
		params = rParam;
		//LOG_PTAG(Info) << "Param for InputRaw: " << raw_param;

	}
	


public:
	InputRaw(){
		LOG_PTAG(Debug) << "comm/InputRaw Constructor" << endl;
		mQueue = new MessageQueue("InputRaw");
	}//()
	void init(vector<string> rParam){
		LOG_PTAG(Debug) << "comm/InputRaw init" << endl;
		ParseParams(rParam);
		
	}//init
	void start(){
		LOG_PTAG(Debug) << "comm/InputRaw start" << endl;

		string raw_param;
		raw_param = params.at(0);
		/*for(vector<string>::iterator itr = params.begin(); itr != params.end(); ++itr){
			string s = (string)(*itr);
			raw_param.append("/"+s);
		}*/


		MessageQueue::Message *m = new MessageQueue::Message();
		m->mMessageId = "NONE";
		m->mDataLength = raw_param.size();
		m->mpData = malloc(m->mDataLength);
		memcpy(m->mpData,raw_param.c_str(),m->mDataLength);
		LOG_PTAG(Debug) << "Writing Raw message to mQueue";
		mQueue->Write(m);
		LOG_PTAG(Info) << "Written Raw message!"; 
			
	}//start

	bool file_exists(const char* filename){
		if(FILE* file = fopen(filename,"r")){
			fclose(file);
			return true;
		}
		return false;		
	}

};


REGISTER_LIB(InputRaw);
