#include <iostream>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include "LogManager.H"

using namespace std;

class OutputFile : public IComm {
private:
	string  mFileStr;
	FILE*  mFile;
	char** mParam;
	void ParseParams(vector<string> rParam){
		//mFileStr = rParam.at(1);
		mFileStr = rParam.at(0);
	}
	


public:
	OutputFile(){
		LOG_PTAG(Debug) << "comm/OutputFile Constructor" << endl;
	}//()
	void init(vector<string> rParam){
		ParseParams(rParam);
		mQueue = new MessageQueue("OutputFile");
		//LOG_PTAG(Debug) << "File set to: " << mFileStr << endl;
	}//init
	void start(){	
			MessageQueue::Message *m = mQueue->Read();
			ofstream file(m->mMessageId.c_str(), ios::out|ios::binary|ios::ate);
			LOG_PTAG(Debug) << "Writing to file: " << m->mMessageId << endl;
        	if (file.is_open())
        	{
            	file.write ((char*)m->mpData, m->mDataLength);
				file.flush();
				file.close();
        	}
			LOG_PTAG(Debug) << "Write Comlplete!" << endl;
			string newMsgId("File://"+m->mMessageId);
			m->mMessageId = newMsgId;



	}//start

};

REGISTER_LIB(OutputFile);
