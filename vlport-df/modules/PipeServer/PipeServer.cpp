#include <iostream> 
#include <stdio.h>
#include "CommonDefines.h"
#include "IModule.h"

using namespace std;

class PipeServer : public IModule {

public:
		
	PipeServer(){
		cout << "PipeServer Constructor" << endl;
		//REGISTER PORTS
		rx_ports[1] = new MessageQueue("Iport1");
		rx_ports[2] = new MessageQueue("Iport2");
		tx_ports[1] = new MessageQueue("Oport1");
		
	}
	void init(vector<string> rParam){}

	void start(){
		cout << "PipeServer Start" << endl;
		while(1)
		{
			MessageQueue::Message* im1 = rx_ports[1]->Read();
			MessageQueue::Message* im2 = rx_ports[2]->Read();
			string s1((char*) im1->mpData);
			string s2((char*) im2->mpData);
			
			string s(s1 + s2);

			MessageQueue::Message* om1 = new MessageQueue::Message();
	
			om1->mDataLength = s.size();
			om1->mpData = (void* )s.c_str();

			tx_ports[1]->Write(om1);

		}
		cout << "PipeServer Start End" << endl;
	}
	void stop(){
		cout << "PipeServer Stop" << endl;
	}
};


REGISTER_MODULE(PipeServer);
