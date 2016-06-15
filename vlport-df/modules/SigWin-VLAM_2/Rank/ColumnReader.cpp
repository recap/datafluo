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

class ColumnReader : public IModule {
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
		
ColumnReader()
{
	cout << "ColumnReader Constructor" << endl;
	//REGISTER PORTS
	rx_ports[1] = new MessageQueue("Iport1");
	tx_ports[1] = new MessageQueue("Oport1");
	
}
virtual ~ColumnReader() throw(){
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
	gParam.delimiter = rParam.at(5); //not working
		cout << "delimiter: " << gParam.delimiter.c_str() << endl;
		
}
template <class T>
void column_send(string rStr)
{
	vector<string> rows;
    vector<T> *cols = new vector<T>;
    Parser::tokenize(rStr,rows,"\n");
    vector<string>::iterator itr;
    for(itr = rows.begin()+gParam.start-1;itr < rows.end(); ++itr)
    {
    	vector<string> tcol;
        Parser::tokenize((*itr),tcol,"\t");//hard coded
        //Parser::tokenize((*itr),tcol,gParam.delimiter.c_str());
        cols->push_back(atoi(tcol.at(gParam.column).c_str()));
        //cout << tcol.at(gParam.column).c_str() << endl;
    }//for
    T *i = &cols->at(0);
	//send
   	MessageQueue::Message* om1 = new MessageQueue::Message();
    om1->mDataLength = cols->size() * sizeof(T);
    //cout << "SIZE: " << om1->mDataLength << endl;
    om1->mpData = (void*)i;
    tx_ports[1]->Write(om1);
}
void start(){
	cout << "ColumnReader Start" << endl;
	while(1)
	{
		MessageQueue::Message* im1 = rx_ports[1]->Read();
		string s((char*) im1->mpData);
		if(gParam.type == "int")
			column_send<int>(s);
		if(gParam.type == "double")
			column_send<double>(s);
		if(gParam.type == "float")
			column_send<float>(s);
		
	}
	cout << "ColumnReader Start End" << endl;
}//start
void stop(){
	cout << "ColumnReader Stop" << endl;
}
};


REGISTER_MODULE(ColumnReader);
