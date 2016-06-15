#include <iostream>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <unistd.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include "LogManager.H"

using namespace std;

class OutputGsiFtp : public IComm {
private:
	string mFileStr;
	string mHostname;
	string mRemoteFile;
	string mDir;
	string mCurrentDir;
	FILE*  mFile;
	char** mParam;
	void ParseParams(vector<string> rParam){
		mHostname = rParam.at(1);
		mDir = rParam.at(2);
		
		//mFileStr = rParam.at(1);
		//mRemoteFile = rParam.at(2);
	}
	


public:
	OutputGsiFtp(){
		LOG_PTAG(Debug) << "comm/OutputGsiFtp Constructor" << endl;
		char buffer[100];
		getcwd((char*)&buffer,100);
		mCurrentDir.append((char*)&buffer);	
        putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");
	}//()
	void init(vector<string> rParam){
		ParseParams(rParam);
		mQueue = new MessageQueue("OutputGsiFtp");
		//LOG_PTAG(Debug) << "File set to: " << mFileStr << endl;
	}//init
	void start(){	
			MessageQueue::Message *m = mQueue->Read();
			string filestr = m->mMessageId;
			ofstream file(filestr.c_str(), ios::out|ios::binary|ios::ate);
			LOG_PTAG(Debug) << "Writing to file: " << filestr << endl;
			if (file.is_open())
			{
					file.write ((char*)m->mpData, m->mDataLength);
					file.flush();
					file.close();
			}		
			LOG_PTAG(Debug) << "Write Comlplete!" << endl;

			string gsiurl("gsiftp://"+mHostname+mDir+"/"+filestr);
			string gucCmd("globus-url-copy file://"+mCurrentDir + "/" +	filestr +" "+gsiurl);
			
			LOG_PTAG(Info) << "Command: " << gucCmd << flush;
			system(gucCmd.c_str());
			string newMsgId("GsiFtp://"+mHostname+mDir+"/"+filestr);
			//cout << "MsgId: " << newMsgId << endl;
			//exit(0);
			m->mMessageId = newMsgId;
	}//start

};

REGISTER_LIB(OutputGsiFtp);
