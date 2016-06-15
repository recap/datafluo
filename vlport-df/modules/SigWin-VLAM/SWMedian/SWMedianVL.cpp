//****************************************************************************
/** \class SWMedian SWMedian
 *  \ingroup SlideWindows SW Stat
 *  \brief Computes moving median profiles of the input array for a 
 *  	    series of window sizes.
 *
 *  \details 
 *  \parameters
 *  <dl>
 *  	\param{datatype,datatype,d}
 *  	    Input sequence data type. Valid data types: 
 *  	    \c i, \c d, \c f.  
 *  	\param{min_window_size,integer,1}
 *  	    Smallest window size. Must be an odd number.
 *  	\param{max_window_size,integer,100000}
 *  	    Approximate largest window size. It will be adjusted.
 *  	\param{step_size,integer,2}
 *  	    Step for incrementing window sizes. Must be even.
 *  	\param{write_parameters,boolean,yes}
 *  	    If \c \b yes, turns on steps send the SWParameters structure 
 *  	    to the \em sw_parameters port.  
 *  	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level.
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *
 *  \inputPorts
 *  <dl>
 *  	\port{rank,rank,/Rank} 
 *  	    A Rank data structure containing the input sequence.
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{sw_parameters,swparameters,/SWParameters} 
 *  	    A SWParameters structure containing the parameters that define 
 *  	    the SlideWindow structure used to compute the moving median
 *  	    profiles.
 *  	\port{sw_data,array,/array} 
 *  	    A series of vectors containing moving median profiles,
 *  	    each profile corresponding to a window size 
 *  	    (starting from the smaller window size).
 *  </dl>
 *
 *  \description
 *   	Computes the moving median profiles corresponding to a input sequence 
 *      of size \em N,
 *  	which is stored in the input Rank structure, 
 *  	for window sizes \n
 *  	\em S=\em min_window_size \b to min(\em max_window_size, \em N) \b 
 *  	step \em step_size.
 * \par  	
 *  	Stores the profiles simultaneously in a SlideWindow data structure
 *  	(of size \f$O(N^2)\f$) before streaming it to the output port.  
 *
 *  \par Notes:
 *  	- The data type of the input Rank structure has to be compatible
 *  	with (and will be converted to) the data type specified by the 
 *  	parameter \em datatype. 
 *  	Accepted data types: \c i, \c d, \c f. 
 *  	- The output port \em sw_parameters is active only if the corresponding 
 *  	\em write_parameter parameter is set to \c \b yes.
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since September 18, 2007.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Sep 18, 2007: test_ports parameter added.
 *  Created: October 19, 2005
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#include "TestFlags.H"
#include "CommonDefines.h"
#include "IModule.h"

#include <fstream>
#include <string>
#include <string.h>
#include <malloc.h>
#include <sstream>

#include "LogManager.H"
#include "Parameter.H"
#include "VecIO.H"
#include "Rank.H"
#include "SlideWindowRank.H"
#include "TestFlags.H"

#include "TimeLag.H"

using namespace std;

class SWMedianVL : public IModule{
  private:
    stringstream  *inStream; 
    stringstream  *outStreamParameters; 
    stringstream  *outStreamData;
    
    //VL parameters
    Parameter<long> minWS;
    Parameter<long> maxWS;
    Parameter<long> stepWS;
    Parameter<char> inputType;
    Parameter<bool> writeParameters;
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<1,2> flag; 

  public:
    SWMedianVL() : 
	minWS("min_window_size", 1),
	maxWS("max_window_size", 100000),
	stepWS("step_size", 100),
	inputType("datatype", 'd'),
	writeParameters("write_parameters", true),
	//logLevel("log_level",DEFAULTLOGLEVEL),
	logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"){
	//Log::open(__func__, logLevel.value);
        inStream = new stringstream();
        outStreamParameters = new stringstream();
        outStreamData = new stringstream();
	flag.set_in_name(1,"rank");
	flag.set_out_name(1,"sw_parameters");
	flag.set_out_name(2,"sw_data");
	INIT_PORTS();

	//rx_ports[1] = new MessageQueue("rank");
    //tx_ports[1] = new MessageQueue("sw_parameters");
    //tx_ports[2] = new MessageQueue("sw_data");

	MAP_RX_PORT(1,rank);
	MAP_TX_PORT(1,sw_parameters);
	MAP_TX_PORT(2,sw_data);
		
    };
	void init(vector<string>* rParam);
	void start();
	void stop(){}
    
    virtual ~SWMedianVL() throw() { 
    	delete outStreamData;
    	delete outStreamParameters;
		delete inStream;
    //	Log::close();
    };

    template <class T> void compute_medians(void);

  
};
void SWMedianVL::init(vector<string>* rParam) {
	cout << "Init called" << endl;
}

void SWMedianVL::start()  {
    Log::ptag(Info) << "SWMedianVL::vlmain() is called. " << flush;
    cout << "SWMedianVL::vlmain() is called. " << flush;

    //get parameters
  /*  Log::ptag(Release) << "Parameters:"<<flush;
    GET_PARAMETER(minWS);
    GET_PARAMETER(maxWS);
    GET_PARAMETER(stepWS);
    GET_PARAMETER(inputType);
    GET_PARAMETER(writeParameters);
    GET_PARAMETER(logLevel);
    Log::set_warn_level(logLevel.value);
    GET_PARAMETER(testPorts);
    flag.set(testPorts.value);
    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;*/

	//MessageQueue::Message* im1 = rx_ports[1]->Read();
	MessageQueue::Message* im1 = READ_PORT(1);
	inStream->write( (char*)im1->mpData, im1->mDataLength);
	SET_RX_PORT_STATE(1,1);
	SIGNAL_RX_PORT(1);
		
  
    //compute medians
    switch (inputType.value){
    	case 'i':
	    compute_medians<int>();
	    break;
	case 'd':
	    compute_medians<double>();
	    break;
	case 'f':
	    compute_medians<float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << inputType.name 
		            << " badly defined! " << flush;
	    break;
    }

	{
	MessageQueue::Message *om1 = new MessageQueue::Message();
	string tstr = outStreamParameters->str();
    char* pstr = (char*)tstr.c_str();
    om1->mDataLength = tstr.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,pstr,om1->mDataLength);
    //tx_ports[1]->Write(om1);
	WRITE_PORT(1,om1);
	}

	{
	MessageQueue::Message *om1 = new MessageQueue::Message();
	string tstr = outStreamData->str();
    char* pstr = (char*)tstr.c_str();
    om1->mDataLength = tstr.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,pstr,om1->mDataLength);
    //tx_ports[2]->Write(om1);
	WRITE_PORT(2,om1);
	}
	
	

    Log::ptag(Info) << "SlideWindow::vlmain() is finished. " << flush;
	WRITE_PORT(1,NULL);
	WRITE_PORT(2,NULL);
    sleep(1);
};    
    
template <class T>    
void SWMedianVL::compute_medians(void){
    TimeLag timing;
    LOG_PTAG(Debug) << "Reading ranked vector from inStream ... " << flush;
    timing.start("SWMedRead");
    test_header(*inStream, flag.get_in_name(1), flag.get_in_flag(1));
    
    Rank<T> rankSeq;
    rankSeq.read(*inStream);
    timing.finish("SWMedRead");
    timing.reset();
    
    const long Size(rankSeq.size());
    if (Size < maxWS.value)
    	maxWS.value = Size;
    
    LOG_PTAG(Debug)<< "Allocating sliding window data structure ... " << flush;
    
    LOG_PTAG(Debug) << "Size="<<Size<<" min=" << minWS.value
    	<< " max=" << maxWS.value << " step=" << stepWS.value<<endl<<flush;
    SlideWindowRank<T> swMedianSeq(Size, minWS.value, maxWS.value,stepWS.value);
    LOG_PTAG(Debug) << swMedianSeq.definition();
    
    if (writeParameters.value){
    	LOG_PTAG(Debug) << "Streaming parameters ... " << flush;
    	write_test_header(*outStreamParameters, flag.get_out_flag(1));
     	swMedianSeq.write_parameters(*outStreamParameters);
    }

    LOG_PTAG(Info) << "Computing sliding window medians ... " << flush;
    timing.start("SWMedComp");
    swMedianSeq.compute_medians(rankSeq);
    timing.finish("SWMedComp");
    timing.reset();
    
    LOG_PTAG(Info) << "Streaming sliding window medians ... " << flush;
    timing.start("SWMedWrite");
    write_test_header(*outStreamData, flag.get_out_flag(2));
    swMedianSeq.write_data(*outStreamData);
    timing.finish("SWMedWrite");
    timing.reset();    
}

//****************************************************************************

REGISTER_MODULE(SWMedianVL);

//end_of_file
