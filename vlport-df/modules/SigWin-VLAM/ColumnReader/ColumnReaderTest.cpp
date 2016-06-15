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
	string delimiter;
	int column;
	int start;
	int end;
	
};		

class ColumnReaderTest : public IModule {
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
		
	ColumnReaderTest()
	{
		cout << "ColumnReaderTest Constructor" << endl;
		//REGISTER PORTS
		rx_ports[1] = new MessageQueue("Iport1");
		tx_ports[1] = new MessageQueue("Oport1");
		
	}
	virtual ~ColumnReaderTest() throw(){
		}
	//Set parameters
	void init(vector<string> rParam)
	{	
		gParam.type = rParam.at(1);
			cout << "type: " << gParam.type << endl;
		gParam.column = atoi(rParam.at(2).c_str());
			cout << "column: " << gParam.column << endl;
		gParam.start = atoi(rParam.at(3).c_str());
			cout << "start: " << gParam.start << endl;
		gParam.end = atoi(rParam.at(4).c_str());
			cout << "end: " << gParam.end << endl;
		gParam.delimiter = rParam.at(5);
//		gParam.delimiter.at(gParam.delimiter.length() - 1) = ' ';
			cout << "delimiter: " << gParam.delimiter << endl;
		
	}
	void start(){
		cout << "ColumnReaderTest Start" << endl;
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
		cout << "ColumnReaderTest Start End" << endl;
	}
	void stop(){
		cout << "ColumnReaderTest Stop" << endl;
	}
};


REGISTER_MODULE(ColumnReaderTest);
