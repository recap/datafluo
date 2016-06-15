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

#define GC GlobalConfiguration

using namespace std;

class OutputPGsiFtp : public IComm {
private:
	string mFileStr;
	string mHostname;
	string mRemoteFile;
	string mDir;
	string mCurrentDir;
	string mHeadNode;
	FILE*  mFile;
	char** mParam;
	void ParseParams(vector<string> rParam){
		mHostname = rParam.at(1);
		mDir = rParam.at(2);
		
		//mFileStr = rParam.at(1);
		//mRemoteFile = rParam.at(2);
	}
	


public:
	OutputPGsiFtp(){
		LOG_PTAG(Debug) << "comm/OutputPGsiFtp Constructor" << endl;
		char buffer[100];
		getcwd((char*)&buffer,100);
		mCurrentDir.append((char*)&buffer);	
	    //putenv("X509_USER_PROXY=/home/rcushing/.globus/x509up_u1621");	
		char* headnode;
		headnode =  (char*)gSharedVariables["HEAD_NODE"];
		mHeadNode.append((char*)headnode);
		LOG_PTAG(Info) << "HEAD_NODE: " << mHeadNode << endl;;
	}//()
	void init(vector<string> rParam){
		ParseParams(rParam);
		mQueue = new MessageQueue("OutputPGsiFtp");
		//LOG_PTAG(Debug) << "File set to: " << mFileStr << endl;
	}//init
	void start(){	
			MessageQueue::Message *m = mQueue->Read();
			char* s = (char*)m->mpData;
			s[m->mDataLength] = '\0';
			//string filestr = m->mMessageId;
			string filestr(s);
			TokenString::TokenString* ts = new TokenString::TokenString(filestr,"/");
			string file_name = ts->tokens.back();	
			if (file_exists(filestr.c_str()) == true ){
				if(mHostname.compare(mHeadNode) != 0){
					string gsiurl("gsiftp://"+mHostname+mDir+"/"+file_name);
					string gucCmd("globus-url-copy file://"+filestr +" "+gsiurl+" 2>&1");
					
					LOG_PTAG(Info) << "Command: " << gucCmd << flush;
					//system(gucCmd.c_str());
					srand(time(NULL));
					int rnd_num = 0;
					while(sys_cmd(gucCmd.c_str()) == false){
						rnd_num = (rand() % 10) + 1;
						sleep(rnd_num);
					}

				}else{
					string mvCmd("mv "+filestr+" "+mDir+"/"+file_name);
					//mvCmd.append("mv ");
					//mvCmd.append(filestr);
					//mvCmd.append(" ");
					//mvCmd.append(mDir+"/"+file_name);
					LOG_PTAG(Info) << "Command: " << mvCmd << flush;
					system(mvCmd.c_str());
				}

					string newMsgId("PGsiFtp://"+mHostname+mDir+"/"+file_name);
					m->mMessageId = newMsgId;
						
			}//if file_exists
			else
				LOG_PTAG(Error) <<"File not found: " << filestr;

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

REGISTER_LIB(OutputPGsiFtp);
