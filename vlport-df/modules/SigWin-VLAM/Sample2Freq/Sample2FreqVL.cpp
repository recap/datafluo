//****************************************************************************
/** \class Sample2Freq Sample2Freq
 *  \ingroup ArrayStat Stat
 *  \brief Generates frequency distributions of a succession of samples.
 *
 *  \parameters
 *  <dl>
 *  	\param{input_datatype,datatype,d}
 *  	    Input sequence data type. Valid data types: 
 *  	    \c i, \c d, \c f.  
 *  	\param{output_datatype,datatype,d}
 *  	    Output probabilities data type. Valid data types: 
 *  	    \c i, \c d, \c f.  
 *  	\param{normalize,boolean,yes} 
 *	    Normalize the frequency counts? 
 *	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *
 *  \inputPorts
 *  <dl>
 *  	\port{samples,array,/array} 
 *  	    A succession of vectors, each array containing a sample.
 *  	\port{x_axis,sorted_set,/array/sorted_list/sorted_set} 
 *  	    The 'x'-axis for the frequency ditribution.
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{frequencies,array,/array} 
 *  	    A succession of vectors, each array containing the frequency 
 *  	    distribution corresponding one input sample. 
 *  </dl>
 *
 *  \description
 *  	Counts the number of times each number of the input set 
 *      appears in each sample. Normalizes the frequency counts if the
 *  	parameter \em normalize = \b \c yes. One frequency distribution is
 *  	generated for each input sample and streamed in a pipeline.  
 *
 *  \par Notes:
 *  	- The data type of the arrays in all input ports has to be 
 *  	compatible with (and will be converted) to the datatype specified 
 *  	by the parameter \em input_datatype. 
 *  	Accepted data types: \c i, \c d, \c f. 
 *  	- The output frequencies will be computed using the 
 *  	data type specified by the parameter \em output_datatype. 
 *  	Accepted data types: \c i, \c d, \c f. 
 *
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
 *  Created: December 02, 2005
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
#include <stdio.h>
#include <string.h>
#include <malloc.h>


#include "LogManager.H"
#include "Parameter.H"
#include "VarBinHist.H"

#include "TimeLag.H"

using namespace std;

class Sample2FreqVL : public IModule{
  private:
    stringstream  *inSortedSet; 
    stringstream  *inSamples; 
    stringstream  *outFrequencies; 
    
    //VL parameters
    Parameter<char> inputType;
    Parameter<char> outputType;
    Parameter<bool> normalize;
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<2,1> flag; 

  public:
    Sample2FreqVL(): 
	inputType("input_datatype", 'd'),
	outputType("output_datatype", 'd'),
	normalize("normalize", true),
//	logLevel("log_level",DEFAULTLOGLEVEL),
	logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"){	
	//Log::open(__func__,logLevel.value);
    inSortedSet = new stringstream();
    inSamples = new stringstream;
    outFrequencies = new stringstream;
	flag.set_in_name(1,"x_axis");
	flag.set_in_name(2,"samples");
	flag.set_out_name(1,"frequencies");
	INIT_PORTS();

    //rx_ports[1] = new MessageQueue("x_axis");
    //rx_ports[2] = new MessageQueue("samples");
    //tx_ports[1] = new MessageQueue("frequencies");
	MAP_RX_PORT(1,x_axis);
	MAP_RX_PORT(2,samples);
	MAP_TX_PORT(1,frequencies);
	
    }
	void stop();
	void start();
	void init(vector<string>* rParam);
    
    virtual ~Sample2FreqVL() throw() {
    	delete outFrequencies; 
    	delete inSamples;
		delete inSortedSet;
		//Log::close();
    }

    template <class InType> void choose_out_type(void);
    template <class InType, class OutType> void make_histogram(void);

};

void Sample2FreqVL::stop(){}

void Sample2FreqVL::init(vector<string>* rParam) {
    //get parameters
    /*Log::ptag(Release) << "Parameters:"<<flush;
    GET_PARAMETER(inputType);
    GET_PARAMETER(outputType);
    GET_PARAMETER(normalize);
    GET_PARAMETER(logLevel);
    Log::set_warn_level(logLevel.value);
    GET_PARAMETER(testPorts);    
    flag.set(testPorts.value);
    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;*/

}


void Sample2FreqVL::start() {
    Log::ptag(Info) << "Sample2FreqVL::vlmain() is called. " << flush;

    
    //compute medians
    LOG_PTAG(Debug) << "inputType= " << inputType.value << flush; 
    switch (inputType.value){
    	case 'i':
	    choose_out_type<int>();
	    break;
	case 'd':
	    choose_out_type<double>();
	    break;
	case 'f':
	    choose_out_type<float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << inputType.name 
		            << " badly defined! " << flush;
	    break;
    }
    
    Log::ptag(Info) << "Sample2FreqVL::vlmain() is finished. " << flush;
};  

template <class InType>
void Sample2FreqVL::choose_out_type(void){
    LOG_PTAG(Debug) << "outputType= " << outputType.value << flush; 
    switch (outputType.value){
	case 'd':
	    make_histogram<InType,double>();
	    break;
	case 'f':
	    make_histogram<InType,float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << outputType.name 
		            << " badly defined! " << flush;
	    break;
    }
}
  
template <class InType, class OutType>    
void Sample2FreqVL::make_histogram(void){

    LOG_PTAG(Debug) << "Reading list of possible values from inSortedSet ... "
     	    	    << flush;
	//MessageQueue::Message* im1 = rx_ports[1]->Read();
	MessageQueue::Message* im1 = READ_PORT(1);
    inSortedSet->write( (char*)im1->mpData, im1->mDataLength);
	SET_RX_PORT_STATE(1,1);
	SIGNAL_RX_PORT(1);

    test_header(*inSortedSet, flag.get_in_name(1),flag.get_in_flag(1));
    vector<InType> valueList;
    string descr="";
    read_vector(*inSortedSet, valueList, descr);
    LOG_PTAG(Debug) <<"valueList size=" <<valueList.size() << flush;
        
    LOG_PTAG(Debug) <<"Allocating histogram... " << flush;
    VarBinHist<InType,OutType> hist(valueList);

	//MessageQueue::Message* im2 = rx_ports[2]->Read();
	MessageQueue::Message* im2 = READ_PORT(2);
    inSamples->write( (char*)im2->mpData, im2->mDataLength);
	SET_RX_PORT_STATE(2,1);
	SIGNAL_RX_PORT(2);
    
    LOG_PTAG(Debug) << "Reading test header data from inSamples " << flush;
    test_header(*inSamples,  flag.get_in_name(2),flag.get_in_flag(2));
  
    LOG_PTAG(Debug) << "Writing test header data to outFrequencies " << flush;
    write_test_header(*outFrequencies, flag.get_out_flag(1));
    
    vector<InType> data;
    string descr_data="";
    
   
    long count(0);
    TimeLag TimeRead, TimeWrite, TimeComp;
    TimeWrite.start("S2FWrite");
    TimeWrite.pause("S2FWrite");
    TimeComp.start("S2FComp");
    TimeComp.pause("S2FComp");
    TimeRead.start("S2FRead");
    while(read_vector(*inSamples, data, descr_data)){
    	Log::print(Info) << "." <<flush;
    	TimeRead.pause("S2FRead");
    	++count;
    	LOG_PTAG(Debug) << "Constructing histogram " << count 
	    << ": " << descr_data <<flush ;
	descr_data="";
    	TimeComp.cont("S2FComp");
	hist.set_from_sample(data);
	
 		
	if (normalize.value)
	    hist.normalize();
    	TimeComp.pause("S2FComp");
	data.clear();
    	
	LOG_PTAG(Debug) << "Writing frequencies ... " << flush;
    	TimeWrite.cont("S2FWrite");
	
    	hist.write_freq(*outFrequencies);


    	
	TimeWrite.pause("S2FWrite");
    	TimeRead.cont("S2FRead");
    }
	MessageQueue::Message *om1 = new MessageQueue::Message();
	string tstr = outFrequencies->str();
	char* pstr = (char*)tstr.c_str();
    om1->mDataLength = tstr.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,pstr,om1->mDataLength);
        //tx_ports[1]->Write(om1);
	WRITE_PORT(1,om1);
		
	WRITE_PORT(1,NULL);
    TimeRead.finish("S2FRead");
    TimeWrite.finish("S2FWrite");
    TimeComp.finish("S2FComp");
     
    LOG_PTAG(Debug) << "End " << flush;

} 
//*********************************************************************
REGISTER_MODULE(Sample2FreqVL);
//end_of_file
