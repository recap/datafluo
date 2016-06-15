#include <iostream>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <unistd.h>
#include <cstdlib>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include "LogManager.H"
#include "TokenString.h"

using namespace std;

class OutputRaw : public IComm {
private:
	//string raw_param;
	void ParseParams(vector<string> rParam){
	//	raw_param = rParam.at(1);
	}
	


public:
	OutputRaw(){
		LOG_PTAG(Debug) << "comm/OutputRaw Constructor" << endl;
	}//()
	void init(vector<string> rParam){
		ParseParams(rParam);
	//	mQueue = new MessageQueue("OutputRaw");
	}//init
	void start(){	
	/*		MessageQueue::Message *m = mQueue->Read();
			char* s = (char*)m->mpData;
			s[m->mDataLength] = '\0';
			//string filestr = m->mMessageId;
			string filestr(s);
			TokenString::TokenString* ts = new TokenString::TokenString(filestr,"/");
			string file_name = ts->tokens.back();	
			if (file_exists(filestr.c_str()) == true ){
					string gsiurl("gsiftp://"+mHostname+mDir+"/"+file_name);
					string gucCmd("globus-url-copy file://"+filestr +" "+gsiurl);
					
					LOG_PTAG(Info) << "Command: " << gucCmd << flush;
					system(gucCmd.c_str());

					string newMsgId("Raw://"+mHostname+mDir+"/"+file_name);
					m->mMessageId = newMsgId;
						
			}//if file_exists
			else
				LOG_PTAG(Error) <<"File not found: " << filestr;
*/
	}//start

	bool file_exists(const char* filename){
        if(FILE* file = fopen(filename,"r")){
            fclose(file);
            return true;
        }
        return false;
    }


};

REGISTER_LIB(OutputRaw);
