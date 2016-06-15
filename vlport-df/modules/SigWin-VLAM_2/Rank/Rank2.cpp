#include <iostream> 
#include <stdio.h>
#include <stdlib.h>
#include "CommonDefines.h"
#include "IModule.h"

#include "TestFlags.H"
#include "LogManager.H"
#include "Parameter.H"
//#include "VecIO.H"
//#include "TimeLag.H"

using namespace std;

class Parameters{
public:
	string type;
	string write_to;
	string log_level;
};		

class Rank : public IModule {
private:
	Parameters gParam;
	//VLAM parameters
    /*Parameter<string> inFileName; //> Name of the input file.
    Parameter<string> descr;  //> Input file description.
    Parameter<int> column;  //> Column number
    Parameter<string> colDescr;  //> Column description.
    Parameter<char> inputType;  //> Input type: i=int, d=double, f=float.
    Parameter<WarnType> logLevel; //> Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
	TestFlags<1,1> flag;*/

public:
		
	Rank()
	{
		cout << "Rank Constructor" << endl;
		//REGISTER PORTS
		rx_ports[1] = new MessageQueue("Iport1");
		tx_ports[1] = new MessageQueue("OportRank");
		tx_ports[2] = new MessageQueue("OportSorted");
		tx_ports[3] = new MessageQueue("OportSortedSets");
		
	}
	virtual ~Rank() throw(){
		}
	//Set parameters
	void init(vector<string> rParam)
	{	
		gParam.type = rParam.at(1);
			cout << "type: " << gParam.type << endl;
		gParam.write_to = rParam.at(2)
			cout << "write_to: " << gParam.column << endl;
		
	}
	void start(){
		cout << "Rank Start" << endl;
		int *vvc;
		while(1)
		{
			MessageQueue::Message* im1 = rx_ports[1]->Read();
			cout << "READING" << endl;
			int index = im1->mDataLength / sizeof(int);

			vvc = (int*)im1->mpData;
			vector<int> vdata;
			vector<int>::iterator itr;
			for(int i = 0; i < index; i++)
				vdata.push_back(vvc[i]);

			for(itr = vdata.begin();itr < vdata.end();++itr)
		    	cout << (*itr) << endl;

			MessageQueue::Message* om1 = new MessageQueue::Message();
			om1->mDataLength = im1->mDataLength;
			om1->mpData = (void*)vvc;
			tx_ports[1]->Write(om1);
			

		}
		cout << "Rank Start End" << endl;
	}
	void stop(){
		cout << "Rank Stop" << endl;
	}
};


REGISTER_MODULE(Rank);
