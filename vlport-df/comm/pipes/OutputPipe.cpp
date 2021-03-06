#include <iostream>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include <fcntl.h>

using namespace std;

class OutputPipe : public IComm {
private:
	string  mPipeStr;
	FILE*  mPipe;
	char** mParam;
	void ParseParams(vector<string> rParam){
		mPipeStr = rParam.at(1);
	}
	


public:
	OutputPipe(){
		cout << "Started OutputPipe" << endl;
	}
	void init(vector<string> rParam){
		ParseParams(rParam);
		mQueue = new MessageQueue("PipeQ");
		cout << "Pipe set to " << mPipeStr << endl;
	}
	void start(){
			cout << "OutPipe: start!" << endl;	
			FILE* fd;
			cout << "OPENING PIPE" << endl;
			fd = fopen(mPipeStr.c_str(),"wb");
			//fd = fopen(mPipeStr.c_str(),"w");
		while(1)
		{
			MessageQueue::Message *im1 = mQueue->Read();
			fwrite(&im1->mDataLength,sizeof(size_t),1,fd);
			//fwrite(&l,sizeof(int),1,fd);
			fwrite((void*)im1->mpData,im1->mDataLength,1,fd);
			//fwrite((char*)im1->mpData,im1->mDataLength,1,fd);
			fflush(fd);
			cout << "WRITING PIPE" << endl;
			
		}//while
/*//		while(1)
//		{
//			MessageQueue::Message *m = mQueue->Read();
			//ifstream::pos_type size;
        	ofstream file(mPipeStr.c_str(), ios::out|ios::binary|ios::ate);
        	//ofstream file(mPipeStr.c_str(), ios::in|ios::ate);
			int ss = 12;
//			cout << "Openening Writer!" << endl;
			char* message = "Hello, world!";
			int fd = open(mPipeStr.c_str(),O_WRONLY);
			cout << "FD: "<< fd << endl;
			if (write(fd, message, 14) < 0)
    			cout << "error writing" << endl;

			sleep(10);
			
//			cout << "Moved ahead!" << endl;
		
//        	if (file.is_open())
  //     	{
			cout << "Writing to pipe" << endl;
            	//file.write (reinterpret_cast<const char*>(&ss),2);
				char* b = "test string";		
            	file.write (b,12);

				sleep(20);

            	//size = file.tellg();
            	//cout << "size: "<< size << endl;
            	//char* t =  new char [size + 1];
            	//file.seekg (0, ios::beg);
            	//file.write ((char*)m->mpData, m->mDataLength);
            	//file.close();
            	//t[size-1] = '\0';
            	//m->mpData = (void *)t;
            	//mQueue->Write(m);
    //    	} // if*/

//		} //while

	}

};

REGISTER_LIB(OutputPipe);
