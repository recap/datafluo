//****************************************************************************
/** \class Rank Rank
 *  \ingroup Arrays RankG Stat
 *  \brief Associates a rank to each number of the input array(s).
 *
 *  \details 
 *  \parameters
 *  <dl>
 *  	\param{datatype,datatype,d}
 *  	    Input array data type. Valid data types: 
 *  	    \c i, \c d, \c f.  
 *  	\param{write_to,string,all}
 *  	    Selects which output ports will be used.
 *  	    Accepted values: 'none', 'all', or a combination of 'rank',
 *  	    'sorted_list', and  'sorted_set' separated by blanc spaces.   
 *  	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *  
 *  \inputPorts
 *  <dl>
 *  	\port{sequence,array,/array} 
 *  	    A succession of arrays to be ranked.
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{rank,rank,/Rank} 
 *  	    A succession of Rank data structures. One for each input array.
 *  	\port{sorted_list,sorted_list,/array/sorted_list} 
 *  	    A succession of sorted arrays. One for each input array.
 *  	\port{sorted_set,sorted_set,/array/sorted_list/sorted_set} 
 *  	    A succesion sorted sets (i.e., a sorted array without duplicate 
 *  	    values. One for each input array.
 *  </dl>
 *
 *  \description
 *  	    Reads a succession of arrays. Generates a Rank structure for each
 *   	array by associating a rank to each number of the array. Writes the
 *  	resulting Rank structure, sorted_list, and sorted_set to the output
 *  	ports that are active. 
 *
 *  \par Notes:
 *  	- The data type of the input array has to be compatible with 
 *  	(and will be converted to) the datatype specified by the parameter 
 *  	\em datatype. 
 *  	Accepeted datatypes: \c i, \c d, \c f. 
 *  	- Accepts a succesion of input arrays.
 *  	- Only the output ports selected by the   
 *  	\em write_to parameter will be active.
 *
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 18, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Jan 18, 2008: Accepts a succesion of input arrays.
 *  	    	  Accepts array description.
 *  Sep 17, 2007: test_ports paparemter added.
 * Created: November 30, 2005	  *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************///*
#include "TestFlags.H"
#include "CommonDefines.h"
#include "IModule.h"


#include <fstream>
#include <string>
#include <stdio.h>
#include <string.h>
#include <malloc.h>


#include "LogManager.H"
#include "Parameter.H"
#include "DataIO.H"
#include "VecIO.H"
#include "Rank.H"

#include "TimeLag.H"


using namespace std;


class RankVL : public IModule{
  private:
    stringstream  *inStream; 
    stringstream  *outStreamRank; 
    stringstream  *outStreamSorted; 
    stringstream  *outStreamNoDupl; 
    
    //VL parameters
    Parameter<char> inputType;
    Parameter<string> writeto;
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<1,3> flag; 
    TestFlags<0,3> usePort; 

  public:
    RankVL()
        : 
	inputType("datatype", 'd'),
	writeto("write_to", "all"),
	//logLevel("log_level",DEFAULTLOGLEVEL),
	logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"),
	usePort("both"){
	Log::open(__func__,logLevel.value);
        inStream = new stringstream(); 
        outStreamRank = new stringstream();
        outStreamSorted = new stringstream();
        outStreamNoDupl = new stringstream();
	flag.set_in_name(1,"sequence");
	flag.set_out_name(1,"rank");
	flag.set_out_name(2,"sorted_list");
	flag.set_out_name(3,"sorted_set");
	usePort.set_out_name(1,"rank");
	usePort.set_out_name(2,"sorted_list");
	usePort.set_out_name(3,"sorted_set");

		//vlport2
		rx_ports[1] = new MessageQueue("sequence");
		tx_ports[1] = new MessageQueue("rank");
		tx_ports[2] = new MessageQueue("sorted_list");
		tx_ports[3] = new MessageQueue("sorted_set");
		
    }

	void start();
	void stop();
	void init(vector<string> rParam);
    
    virtual ~RankVL() throw() {
	delete outStreamNoDupl;
	delete outStreamSorted;
    	delete outStreamRank;
	delete inStream; 
	Log::close();
    }

    template <class T> void read_vectors(void);
    template <class T> void rank_vector(vector<T> sequence,const string& descr);

};

void RankVL::stop(){}

void RankVL::init(vector<string> rParam){
    //get parameters
/*    Log::ptag(Release) << "Parameters:"<<flush;
    GET_PARAMETER(inputType);
    GET_PARAMETER(writeto);
    usePort.set(writeto.value);
    GET_PARAMETER(logLevel);
    Log::set_warn_level(logLevel.value);
    GET_PARAMETER(testPorts);
    flag.set(testPorts.value);
    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;*/

}

void RankVL::start(){

    Log::ptag(Release) << "RankVL::vlmain() is called. " << flush;

    //compute medians
    switch (inputType.value){
    	case 'i':
	    read_vectors<int>();
	    break;
	case 'd':
	    read_vectors<double>();
	    break;
	case 'f':
	    read_vectors<float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << inputType.name 
		            << " badly defined!. " << flush;
	    break;
    }    
    Log::ptag(Release) << "RankVL::vlmain() is finished. " << flush;
    sleep(1);
};    

template <class T>    
void RankVL::read_vectors(void){
    
    LOG_PTAG(Info) << "Reading input arrays from inStream. " << flush;
    vector<T> sequence;

	MessageQueue::Message* im1 = rx_ports[1]->Read();
    inStream->write( (char*)im1->mpData, im1->mDataLength);
	//cout << im1->mDataLength << (char*)im1->mpData << endl;

    test_header(*inStream, flag.get_in_name(1),flag.get_in_flag(1));
    if (usePort.get_out_flag(1))
    	write_test_header(*outStreamRank, flag.get_out_flag(1));
    if (usePort.get_out_flag(2))
    	write_test_header(*outStreamSorted, flag.get_out_flag(2));
    if (usePort.get_out_flag(3))
    	write_test_header(*outStreamNoDupl, flag.get_out_flag(3));
    string descr="";
    long count(0);
    while (read_vector(*inStream, sequence, descr)){
	++count;    
    	LOG_PTAG(Info)<<"Ranking array: "<<count<< " '" << descr <<"'"<< flush;
    	rank_vector(sequence, descr);
    	descr="";
    }    
    LOG_PTAG(Info)<< "Total number of input arrays " << count<<flush;  
}
    
template <class T>    
void RankVL::rank_vector(vector<T> sequence, const string& descr){
    TimeLag timing;
    
    timing.start("Ranking");
    Rank<T> rankSequence(sequence, descr, true);
    timing.finish("Ranking");
    timing.reset();
    
   
    LOG_PTAG(Debug) << "WriteRank= "<< usePort.get_out_flag(1) << flush;
    if (usePort.get_out_flag(1)){
        LOG_PTAG(Info) << "Writing ranked sequence to outStreamRank. "<< flush;
    	timing.start("RankWrite");
    	rankSequence.write(*outStreamRank);    

		MessageQueue::Message *om1 = new MessageQueue::Message();
		string tstr = outStreamRank->str();
		char* pstr = (char*)tstr.c_str();
        om1->mDataLength = tstr.size();
        om1->mpData = (void*)malloc(om1->mDataLength);
        memcpy(om1->mpData,pstr,om1->mDataLength);
        tx_ports[1]->Write(om1);

    	timing.finish("RankWrite");
    	timing.reset();
	
    }
    LOG_PTAG(Debug) << "WriteSorted= "<< usePort.get_out_flag(2) << flush;
    
    if (usePort.get_out_flag(2)){
        LOG_PTAG(Info) << "Writing sorted to outStreamSorted. "<< flush;
    	timing.start("RankWriteSorted");
	rankSequence.write_sorted(*outStreamSorted);

		MessageQueue::Message *om1 = new MessageQueue::Message();
		string tstr = outStreamSorted->str();
		char* pstr = (char*)tstr.c_str();
        om1->mDataLength = tstr.size();
        om1->mpData = (void*)malloc(om1->mDataLength);
        memcpy(om1->mpData,pstr,om1->mDataLength);
        //tx_ports[2]->Write(om1); //RRR
        tx_ports[3]->Write(om1); //RRR
		
														

    	timing.finish("RankWriteSorted");
    	timing.reset();
    }
    LOG_PTAG(Debug) << "WriteSet= "<< usePort.get_out_flag(3) << flush;
    if (usePort.get_out_flag(3)){
        LOG_PTAG(Info) << "Writing sorted no duplicates to outStreamNoDupl. "
	                << flush;
    	timing.start("RankWriteNoDupl");
	rankSequence.write_sorted_no_duplicates(*outStreamNoDupl);
	//rankSequence.print(cout);

		MessageQueue::Message *om1 = new MessageQueue::Message();
        string tstr = outStreamNoDupl->str();
        char* pstr = (char*)tstr.c_str();
        om1->mDataLength = tstr.size();
        om1->mpData = (void*)malloc(om1->mDataLength);
        memcpy(om1->mpData,pstr,om1->mDataLength);
        //tx_ports[3]->Write(om1); //RRRR
        tx_ports[2]->Write(om1);


    	timing.finish("RankWriteNoDupl");
    	timing.reset();
    }
}
//*************************************************************************

REGISTER_MODULE(RankVL);

//end_of_file
