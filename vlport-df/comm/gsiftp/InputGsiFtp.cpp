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

using namespace std;

class InputGsiFtp : public IComm {
private:
	string mFileStr;
	string mHostname;
	string mCurrentDir;
	string mDir;
	FILE*  mFile;
	//string mParam;
	//char* mParam[30];
	void ParseParams(vector<string> rParam){
		//mFileStr = rParam.at(1);
		mHostname = rParam.at(0);
		mDir = rParam.at(1);
		mFileStr = rParam.at(2);
	}
	


public:
	InputGsiFtp(){
		LOG_PTAG(Debug) << "comm/InputFile Constructor" << endl;
		char buffer[100];
		getcwd((char*)&buffer,100);
		mCurrentDir.append((char*)&buffer);
		LOG_PTAG(Info) << "Setting X509_USER_PROXY" << flush;
		putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");
		//system("echo $X509_USER_PROXY");
	}//()
	void init(vector<string> rParam){
		LOG_PTAG(Debug) << "comm/InputFile init" << endl;
		ParseParams(rParam);
		mQueue = new MessageQueue("InputGsiFtp");
		
	}//init
	void start(){
		LOG_PTAG(Debug) << "comm/InputGsiFtp start" << endl;
		if( file_exists(mFileStr.c_str()) == false){
			string gsiurl("gsiftp://"+mHostname+mDir+"/"+mFileStr);
			string gucCmd("globus-url-copy " + gsiurl +" file://"+mCurrentDir+"/"+mFileStr+" 2>&1");
			LOG_PTAG(Info) << "Command: " << gucCmd << flush;
			//exit(1);
			
			//system(gucCmd.c_str());
			srand(time(NULL));
			int rnd_num = 0;
			while(sys_cmd(gucCmd.c_str()) == false){
				rnd_num = (rand() % 10) + 1;
				sleep(rnd_num);
			}
			//if(file_exists(mFileStr.c_str()) == false){
			//	LOG_PTAG(Fatal) << "Error getting gsiftp file: " << gsiurl << flush;
			//exit(-1);

			LOG_PTAG(Info) << "Got File" <<flush; 
		}

		MessageQueue::Message *m = new MessageQueue::Message();
		m->mMessageId = mFileStr;
		ifstream::pos_type size;
		ifstream file(mFileStr.c_str(), ios::in|ios::binary|ios::ate);
  		if (file.is_open())
  		{
    		size = file.tellg();
			m->mDataLength = size;
			LOG_PTAG(Debug) << "Input file size: " << size << endl;
			LOG_PTAG(Info) << "Input file size: " << size << endl;
    		file.seekg (0, ios::beg);
			m->mpData = malloc(size);
			file.read((char*)m->mpData,size);
    		file.close();
			LOG_PTAG(Debug) << "Input file queued on: " << mQueue->getName() << endl;
			LOG_PTAG(Info) << "Input file queued on: " << mQueue->getName() << endl;
			mQueue->Write(m);
			
  		}
		else 
			LOG_PTAG(Error) << "Openening file:  " << mFileStr << endl;
	}//start

	bool file_exists(const char* filename){
		if(FILE* file = fopen(filename,"r")){
			fclose(file);
			return true;
		}
		return false;		
	}

	bool sys_cmd(const char* cmd){
        FILE *fp;
        char line[256];
        size_t found = 0;
        fp = popen(cmd,"r");
        if(fp == NULL)
            return false;
        while(fgets(line, sizeof(line)-1, fp) != NULL){
            string str(line);
            found=str.find("error");
            if(found != string::npos){
                pclose(fp);
                return false;
            }//if
        }//while
        pclose(fp);

        return true;
    }//sys_cmd


};


REGISTER_LIB(InputGsiFtp);
