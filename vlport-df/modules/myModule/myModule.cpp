#include <iostream> 
#include <stdio.h>
#include "CommonDefines.h"
#include "IModule.h"

using namespace std;

class myModule : public IModule {

public:
		
	myModule(){
		cout << "myModule Constructor" << endl;
		//REGISTER PORTS
		rx_ports[1] = new MessageQueue("Iport1");
		rx_ports[2] = new MessageQueue("Iport2");
		tx_ports[1] = new MessageQueue("Oport1");
		
	}
	void init(vector<string> rParam)
    {
    }

	void start(){
		cout << "myModule Start" << endl;
		while(1)
		{
			MessageQueue::Message* im1 = rx_ports[1]->Read();
			MessageQueue::Message* im2 = rx_ports[2]->Read();
			char* stmp = (char*) im1->mpData;
			stmp[im1->mDataLength] = '\0';
			string s1((char*) im1->mpData);
			char* stmp2 = (char*) im2->mpData;
			stmp2[im2->mDataLength] = '\0';
			//s1[im1->mDataLength] = '\0';
			string s2((char*) im2->mpData);
			//s2[im2->mDataLength] = '\0';
		
			cout << "STRING 1: " << s1.size() << " : " << s1 << endl;	
			cout << "STRING 2: " << s2.size() << " : " << s2 << endl;	
			
			string s(s1 + s2);

			MessageQueue::Message* om1 = new MessageQueue::Message();
	
			om1->mDataLength = s.size();
			cout << "SIZESIZE: " << s.size() << endl;

			om1->mpData = (void* )s.c_str();

			tx_ports[1]->Write(om1);
			sleep(10);

		}
		cout << "myModule Start End" << endl;
	}
	void stop(){
		cout << "myModule Stop" << endl;
	}
};


REGISTER_MODULE(myModule);
