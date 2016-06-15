//****************************************************************************
/** 
 *  \class ColumnReader ColumnReader
 *  \ingroup Arrays IO
 *  \brief Reads a column from a file and streams it to the output port.
 *
 *  \details
 *  \parameters
 *  <dl>
 *	\param{file_name,string,from_port} 
 *  	    Name of the (local) input file. 
 *  	    If \em file_name = \c from_port, then the module reads the 
 *  	    \c File from the \em input_file port .\n
 *  	\param{column,integer,1}
 *  	    Number of the column in the input file to be read.
 *  	\param{datatype,datatype,d}
 *  	    Data type of the values in the column. Valid data types: 
 *  	    \c i, \c d, \c f, \c s (=string without spaces).  
 *  	\param{file_descr,string}
 *  	    String to match the tag 'descr' of the input file header. 
 *  	    May be omitted.
 *  	\param{col_descr,string}
 *  	    String to match description of the column that will be read. 
 *  	    May be omitted.
 *  	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *
 *  \inputPorts
 *  <dl>
 *  	\port{input_file,file,/file} 
 *	    The input file. 
 *   	    (Only used if parameter \em file_name = \c from_port.)
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{sequence,array,/array} 
 *  	    The input sequence transformed into an \refc{array,/array} of
 *  	    data type \c datatype. 
 *  </dl>
 *
 *  \description
 *  	Reads the specified column from the input file and streams it 
 *  	(as an \refc{array,/array}) to the output port.	
 *  \par
 *  	Assumes that the input file is a space-delimited file with (at least)
 *  one column containing a sequence of values that will be streamed to the 
 *  output port. 
 *  A two-line header must precede the data (\ref input_file_example
 * "input file example").
 *
 *  \b Notes:
 *  	- The values of the input column will be converted to the data type 
 *  	specified by the parameter \em datatype. Accepted data types: 
 *  	    \c i, \c d, \c f, \c s (=string without spaces).   
 *  	- Accepts only one input file.
 *
 *
 *  \anchor input_file_example \par Input file example: 
 *  \include input.dat 
 *  The tags \c size=, \c ncols=, and \c descr= may be omitted.
 *
 *
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 25, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
/*---------------------------------------------------------------------------
 *  History:
 *  Jan 25, 2008: Commented out CDB part.
 *  Sep 17, 2007: Test_ports parameter added.
 *  Sep 11, 2007: Changed class name to ColumnReaderVL
 *  May 25, 2007: New input port for the file. File can be local or read from
 *  	    	 input port using CBD libraries 
 *  Created: April 14, 2006	  
 *---------------------------------------------------------------------------
 * Author: Marcia A. Inda
 *  	   Integrative Bioinformatics Unit, UvA
 *         http://staff.science.uva.nl/~indahttp://staff.science.uva.nl/~inda
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/


//#include <vlapp.h>
#include "TestFlags.H"
#include <string>
#include <fstream>
#include <sstream>
#include "LogManager.H"
#include "Parameter.H"
#include "VecIO.H"
#include "TimeLag.H"
#include "CommonDefines.h"
#include "IModule.h"
#include <string.h>
#include <stdio.h>

//#ifdef CBD
//This does not work yet with the new RTSM
//#include <vlecbd.h>
//#include <vlecbdfile.h>
//#endif

using namespace std;

//****************************************************************************
/* \class ColumnReaderVL 
 *  \brief Reads a sequence of numbers from a file and streams it.
 *
 *  \see ColumnReader.C 
 */ 
class ColumnReaderVL : public IModule {
  private:
    stringstream *inStream; //> Input port: input file.
   	stringstream *outStream; //> Output port: sequence of values.

    //VLAM parameters
    Parameter<string> inFileName; //> Name of the input file.
    Parameter<string> descr;  //> Input file description.
    Parameter<int> column;  //> Column number
    Parameter<string> colDescr;  //> Column description.
    Parameter<char> inputType;  //> Input type: i=int, d=double, f=float.
    Parameter<WarnType> logLevel; //> Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<1,1> flag;

  public:
    ColumnReaderVL(): 
	inFileName("file_name","from_port"),
    	descr("file_descr",""),
    	column("column",4),
    	colDescr("col_descr",""),
	inputType("datatype",'d'),
	logLevel("log_level",Extreme),
	//logLevel("EXTREME",DEFAULTLOGLEVEL),
	testPorts("test_ports","all"),
	flag("both"){
		flag.set_in_name(1,"input_file");
		flag.set_out_name(1,"sequence");
		INIT_PORTS();		
		inStream = new stringstream();
		outStream = new stringstream();
		MAP_RX_PORT(1,input_file);
		MAP_TX_PORT(1,sequence);
//		rx_ports[1] = new MessageQueue("input_file");
//		tx_ports[1] = new MessageQueue("sequence");
		//Log::open(__func__,logLevel.value);
	    };
    
    virtual ~ColumnReaderVL() throw() {
 //   	Log::close(); 
    	delete outStream; 
    	delete inStream;
    }
	void init(vector<string>* rParam);
	void start();
	void stop();


  private:
    template <class T> 
    void stream_vector(istream& inFile);

};


void ColumnReaderVL::stop(){}

void ColumnReaderVL::init(vector<string>* rParam) {
//get parameters
  //  Log::ptag(Release) << "Parameters:"<<flush;
/*    GET_PARAMETER(inputType);
    GET_PARAMETER(inFileName);
    GET_PARAMETER(descr);
    GET_PARAMETER(colDescr);
    GET_PARAMETER(column);
    GET_PARAMETER(logLevel);*/
   // Log::set_warn_level(logLevel.value);
    //GET_PARAMETER(testPorts);
    flag.set(testPorts.value);
//    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;
}

void ColumnReaderVL::start()  {

	while(1){
		//MessageQueue::Message* im1 = rx_ports[1]->Read();
		MessageQueue::Message* im1 = READ_PORT(1);
		SIGNAL_RX_PORT(1);
		if(im1 == NULL)
		{
			LOG_PTAG(Info) << "NULL Message";
			SET_RX_PORT_STATE(1,1);
			break;
		}else{
    	Log::ptag(Info) << "Reading file from port"<<flush;     
        string s((char*) im1->mpData);
		*inStream << s;
		//cout << inStream->str() << endl;
		char c=inStream->peek();
		cout << (int)c << endl;
		
		

    TimeLag timing;
    timing.start("ColRead");    
    
	switch (inputType.value){
	    case 'i':
	    	LOG_PTAG(Debug) << "Writing integer vector! " <<flush;
		stream_vector<int>(*inStream);
		break;
	    case 'd':
	    	stream_vector<double>(*inStream);
		break;
	    case 'f':
	    	stream_vector<float>(*inStream);
		break;
	    case 's':
	    	stream_vector<string>(*inStream);
		break;
	    default:
	    	LOG_PTAG(Fatal) << "Parameter " << inputType.name 
		                << " badly defined! " << flush;
		break;
    	}
    timing.finish("ColRead");

		MessageQueue::Message* om1 = new MessageQueue::Message();

		//char* to_c_string(std::string const& str) {
		string strstr;
		//strstr.assign(outStream->str());	
		strstr = outStream->str();
		char * pstr = (char*)strstr.c_str();
		//cout << pstr << endl;
		//cout << strstr << endl;
		om1->mDataLength = strstr.size();
		cout << "sizeof(double)= " << sizeof(double) << ", size= " << om1->mDataLength << endl;
	//	void** p = (void**)(&(outStream->str()));
	//	void* l = (void*)(&(outStream->str()));
	//	char* pstr = (char*)outStream->str().c_str();
	//	cout << outStream->str() << endl;
		om1->mpData = (void*)malloc(om1->mDataLength);
		memcpy(om1->mpData,pstr,om1->mDataLength);
		
//		vector<int> vi;
//		vector<int>::iterator itr;
		//string str = "hg18-htm:expression";
//		string str = "";*/
		//*outStream >> str;
		//cout << str << endl;
		//read_vector(*outStream, vi , descr.value + ":" + colDescr.value);
    	//read_vector(*outStream, vi, str);
		//cout << vi.size() << endl;
		//cout << outStream->str().size() << endl;
		//for(itr = vi.begin(); itr < vi.end(); ++itr)
  	      //   cout << (*itr) << endl;
		

		//cout << outStream->str() << endl;
		//tx_ports[1]->Write(om1);
		WRITE_PORT(1,om1);
		
		SIGNAL_RX_PORT(1);
		}//else
	}//while

	WRITE_PORT(1,NULL);
    
};    
    
template <class T>
void ColumnReaderVL::stream_vector(istream& inFile){
    vector<T> vec;
	vector<int>::iterator itr;
    LOG_PTAG(Info) << "Reading column " << column.value <<flush;
    if (scan_column(inFile, vec, descr.value, column.value, colDescr.value)){
		//for(itr = vec.begin();itr < vec.end(); ++itr)
		//	cout << (*itr) << endl;
    	write_test_header(*outStream,  flag.get_out_flag(1));	
    	write_vector(*outStream, vec, descr.value + ":" + colDescr.value);
		//cout << vec.at(0) << endl;
    }
    else{
    	LOG_PTAG(Fatal)<<"Error reading column.\n"<<flush;
	exit(1);
    }
} 

REGISTER_MODULE(ColumnReaderVL);
//end_of_file
