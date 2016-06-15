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

class InputPGsiFtp : public IComm {
private:
	string mFileStr;
	string mHostname;
	string mCurrentDir;
	char   mCurrentDirBuff[100];
	string mDir;
	FILE*  mFile;
	//string mParam;
	//char* mParam[30];
	void ParseParams(vector<string> rParam){
		//mFileStr = rParam.at(1);
		mHostname = rParam.at(0);
		mFileStr = rParam.at(2);
		mDir = rParam.at(1);
	}
	


public:
	InputPGsiFtp(){
		LOG_PTAG(Debug) << "comm/InputFile Constructor" << endl;
		//char buffer[100];
		getcwd((char*)&mCurrentDirBuff,100);
		mCurrentDir.append((char*)&mCurrentDirBuff);
		//LOG_PTAG(Info) << "Setting X509_USER_PROXY" << flush;
		//putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");
		mQueue = new MessageQueue("InputPGsiFtp");
		//system("echo $X509_USER_PROXY");
	}//()
	void init(vector<string> rParam){
		LOG_PTAG(Debug) << "comm/InputFile init" << endl;
		ParseParams(rParam);
		
	}//init
	void start(){
		LOG_PTAG(Debug) << "comm/InputPGsiFtp start" << endl;
		string longFileName(mCurrentDir+"/"+mFileStr);	
		string localFileName(mDir+"/"+mFileStr);
		if(file_exists(localFileName.c_str()) == true){
			string cpCmd("cp "+localFileName+" "+longFileName);
			LOG_PTAG(Info) << "Command: " << cpCmd << flush;
			system(cpCmd.c_str());
		}else{

		if( file_exists(longFileName.c_str()) == false){
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
			//sleep(1);
			//if(file_exists(longFileName.c_str()) == false){
			//	LOG_PTAG(Fatal) << "Error getting gsiftp file: " << gsiurl << flush;
			//exit(-1);
			//}

			if(file_size(longFileName.c_str()) <= 0)
			{
				/*int retry = 5;

				while(retry > 0){
					sleep(1);
					system(gucCmd.c_str());
					 if(file_size(longFileName.c_str()) > 0)
						retry = 0;
					 else
						retry--;
				}//while*/
				LOG_PTAG(Fatal) << "Error retrieving file: " << gsiurl << flush;
				exit(-1);
			}
		}//top if
				
		LOG_PTAG(Info) << "Got File" <<flush; 
		}


		MessageQueue::Message *m = new MessageQueue::Message();
		m->mMessageId = mFileStr;
		m->mDataLength = longFileName.size();
		m->mpData = malloc(m->mDataLength);
		memcpy(m->mpData,longFileName.c_str(),m->mDataLength);
		LOG_PTAG(Debug) << "Writing PGsiFtp message to mQueue";
		mQueue->Write(m);
		LOG_PTAG(Info) << "Written PGsiFtp message!"; 
			
	}//start

	long file_size(const char* filename){
		long lsize = 0;
		ifstream::pos_type size;
		ifstream file(filename, ios::in|ios::binary|ios::ate);
		if(file.is_open()){
			size = file.tellg();
			lsize = (long) size;
		}//if
		file.close();

		return lsize;

	}

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
			LOG_PTAG(Info) << "INPUT GRIDFTP: " << str;
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


REGISTER_LIB(InputPGsiFtp);
