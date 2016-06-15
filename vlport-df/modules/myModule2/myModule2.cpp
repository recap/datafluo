#include <iostream> 
#include <stdio.h>
#include "CommonDefines.h"
#include "IModule.h"
#include <stdlib.h>

using namespace std;


class myModule2 : public IModule {

public:
		
	myModule2(){
		cout << "myModule2 Constructor" << endl;
		rx_ports[1] = new MessageQueue("myModule2::Iport1");
		tx_ports[1] = new MessageQueue("myModule2::Oport1");
		tx_ports[2] = new MessageQueue("myModule2::Oport2");
	}
	void init(vector<string> rParam)
	{
	}
	
	void start(){
		cout << "myModule2 Start" << endl;
		while(1)
		{
			MessageQueue::Message* im1 = rx_ports[1]->Read();
			char* t = (char*)im1->mpData;
			t[im1->mDataLength] = '\0';
			cout << "Message Data: " << (char*)im1->mpData << endl;
			string data((char *)t);
			//copy by value
			string data2(data);
			//init sizze = data2.size();
			//cout << "MYMODULE SIZE: " << data.size() << ":" << data2.size << endl;

			//simple encrypt data
			for(int i=0;i<data.size()-1;i++)
				data.at(i) = ((data.at(i) ^ 123) + 32);
			
			
			MessageQueue::Message* om1 = new MessageQueue::Message();
			MessageQueue::Message* om2 = new MessageQueue::Message();

			om1->mDataLength = data.size();
			om1->mpData = (void *)data.c_str();

			om2->mDataLength = data2.size();
			om2->mpData = (void *)data2.c_str();

			tx_ports[1]->Write(om1);
			tx_ports[2]->Write(om2);
			sleep(10);
		//	break;

		}//while
		cout << "myModule2 Start End" << endl;
	} //start

	void stop(){
		cout << "myModule2 Stop" << endl;
	}
};

REGISTER_MODULE(myModule2);
