#include <iostream>
#include <stdio.h>
#include <string.h>
#include <fstream>
#include "CommonDefines.h"
#include "IComm.h"
#include <sys/stat.h>
#include <fcntl.h>
#include <malloc.h>

using namespace std;

/*#include <iostream>
#include <fstream>
using namespace std;

ifstream::pos_type size;
char * memblock;

int main () {
  ifstream file ("example.bin", ios::in|ios::binary|ios::ate);
  if (file.is_open())
  {
    size = file.tellg();
    memblock = new char [size];
    file.seekg (0, ios::beg);
    file.read (memblock, size);
    file.close();

    cout << "the complete file content is in memory";

    delete[] memblock;
  }
  else cout << "Unable to open file";
  return 0;
}*/

class InputPipe : public IComm {
private:
	string mPipeStr;
	FILE*  mPipe;
	char* mParam[30];
	void ParseParams(vector<string> rParam){
		mPipeStr = rParam.at(1);
	}
	


public:
	InputPipe(){
		cout << "Started InputPipe" << endl;
	}
	void init(vector<string> rParam){
		cout << "InputPipe: init" << endl;
		ParseParams(rParam);
		mQueue = new MessageQueue("PipeQ");
		
	}
	void start(){
		cout << "InputPipe: start" << endl;
		
		struct stat stFileInfo;
		int istat;
		FILE *fd = fopen(mPipeStr.c_str(),"rb");
		//FILE *fd = fopen(mPipeStr.c_str(),"r");
		while(1){
		MessageQueue::Message* im1 = new MessageQueue::Message();
		
		//cout << "Opening reader!" << endl;
		//cout << "Opend!" << endl;
		fread(&im1->mDataLength,sizeof(size_t),1,fd);
	//	cout << "Data Length: " << im1->mDataLength << endl;
		im1->mpData = malloc(im1->mDataLength);
		fread((void*)im1->mpData,im1->mDataLength,1,fd);
	//	cout << "READ MESSAGE " << (char *) im1->mpData  << endl;

		mQueue->Write(im1);
		}
		
		//cout << "L: " << l << endl;
		//cout << "Moving reader ahead!" << endl;
/*
		do{
		istat = stat(mPipeStr.c_str(),&stFileInfo);
		sleep(5);
		cout << "Waiting!" << endl;
		}while(istat != 0);

		cout << "Moved on" << endl;*/
		
	//	MessageQueue::Message *m = new MessageQueue::Message();
//		ifstream file(mPipeStr.c_str(), ios::in|ios::binary|ios::ate);
//		sleep(5);
	//	cout << "before if" << endl;
	//	if (file.is_open())
//		{
//			int i = 0;
//			char t[20];
//			cout << "reading" << endl;
//			file.read(t,12);
//			cout << t << endl;
			
//		}else
//			cout << "not open" << endl;
				
		
		
		
		/*MessageQueue::Message *m = new MessageQueue::Message();
		ifstream::pos_type size;
		ifstream file(mPipeStr.c_str(), ios::in|ios::binary|ios::ate);
		//ifstream file(mPipeStr.c_str(), ios::in|ios::ate);
  		if (file.is_open())
  		{
    		size = file.tellg();
			m->mDataLength = size;
			cout << "size: "<< size << endl;
			char* t =  new char [size + 1];
    		file.seekg (0, ios::beg);
    		file.read (t, size);
    		file.close();
			t[size-1] = '\0';
			m->mpData = (void *)t;
			mQueue->Write(m);
  		}
		else cout << "Error" << endl;*/

		
		/*mPipe = fopen(mPipeStr,"rt");
		
		fseek(mPipe,0,SEEK_END); //go to end
		len=ftell(mPipe); //get position at end (length)
		fseek(mPipe,0,SEEK_SET); //go to beg.
		m->mpData=(char *)malloc(len); //malloc buffer
		fread(m->mpData,len,1,mPipe); //read into buffer
		fclose(mPipe);
		mQueue->Write(m);
		cout << "Read Pipe" << endl;*/

	}

};

REGISTER_LIB(InputPipe);
