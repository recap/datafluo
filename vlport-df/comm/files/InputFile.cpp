#include <iostream>
#include <stdio.h>
#include <string.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include "malloc.h"
#include "LogManager.H"

using namespace std;

class InputFile : public IComm {
private:
	string mFileStr;
	FILE*  mFile;
	char* mParam[30];
	void ParseParams(vector<string> rParam){
		//mFileStr = rParam.at(1);
		vector<string>::iterator itr;
		for(itr = rParam.begin(); itr < rParam.end(); ++itr)
		{
			string str = (string)(*itr);
			
			mFileStr.append("/");
			mFileStr.append(str);
		}
		//mFileStr = rParam.at(0);
	}
	


public:
	InputFile(){
		LOG_PTAG(Debug) << "comm/InputFile Constructor" << endl;
	}//()
	void init(vector<string> rParam){
		LOG_PTAG(Debug) << "comm/InputFile init" << endl;
		ParseParams(rParam);
		mQueue = new MessageQueue("InputFile");
		
	}//init
	void start(){
		LOG_PTAG(Debug) << "comm/InputFile start" << endl;
		MessageQueue::Message *m = new MessageQueue::Message();
		m->mMessageId = mFileStr;
		ifstream::pos_type size;
		ifstream file(mFileStr.c_str(), ios::in|ios::binary|ios::ate);
  		if (file.is_open())
  		{
    		size = file.tellg();
			m->mDataLength = size;
			LOG_PTAG(Debug) << "Input file size: " << size << endl;
    		file.seekg (0, ios::beg);
			m->mpData = malloc(size);
			file.read((char*)m->mpData,size);
    		file.close();
			LOG_PTAG(Debug) << "Input file queued on: " << mQueue->getName() << endl;
			mQueue->Write(m);
  		}
		else 
			LOG_PTAG(Error) << "Openening file:  " << mFileStr << endl;
	}//start

};

REGISTER_LIB(InputFile);
