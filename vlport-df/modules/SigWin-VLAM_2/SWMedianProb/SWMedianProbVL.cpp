//****************************************************************************
/** 
 *  \class SWMedianProb SWMedianProb
 *  \ingroup SlideWindows SW
 *  \brief Computes theoretical probabilities of moving medians for
 *  	   a series of window sizes.
 *
 *  \details 
 *  \parameters
 *  <dl>
 *  	\param{input_datatype,datatype,d}
 *  	    Input sequence data type. Valid data types: 
 *  	    \c i, \c d, \c f.  
 *  	\param{output_datatype,datatype,i}
 *  	    Computed moving median probabilities data type. Valid data types: 
 *  	    \c d, \c f.  
 *  	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level.
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *
 *  \inputPorts
 *  <dl>
 *  	\port{sw_parameters,swparameters,/SWParameters} 
 *  	    A SWParameters structure containing the parameters that define 
 *  	    the SlideWindow structure used to compute the moving median 
 *  	    probabilities.
 *  	\port{sorted_list,sorted_list,/array/sorted_list} 
 *  	    The input sequence in sorted order, see \ref note3 "Note 3".
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{sw_data,array,/array} 
 *  	    A sequence of vectors containing moving median 
 *  	    probability mass functions (pmf), each pmf corresponding to 
 *  	    a window size (starting from the smaller window size), see 
 *  	    \ref note3 "Note 3".
 *  </dl>
 *
 * \description 
 *  	Computes the probability mass function (pmf) \f$f(m)\f$, 
 *  	corresponding 
 *  	to the probability that \f$m\f$ is the median value in a
 *  	subsequence (or window) of size \f$S=2k+1\f$ of the input sequence.
 *  	If all elements in the sequence are different, this probability is 
 *  	equal to:
 *  	\f[
 *      \sigma_{2k+1,N}(E_j) = \frac{C^j_k C^{N-j}_k}{C^N_{2k+1}},
 *  	\f]
 *  	if \f$E_j\f$ is the the element ranked \f$j\f$ in the original
 *  	sequence, or to zero, otherwise.
 *  	Here \f$C^N_S\f$ is the number of combinations of \f$N\f$ elements 
 *  	taken \f$S\f$ at a time. 
 *  	If the input sequence has duplicate values
 *  	we assign consecutive ranks for the duplicate values 
 *  	and sum the probabilities of all ranks that correspond 
 *  	to the same value. 
 * \par
 *  Note that:
 *  - The value of \f$\sigma_{2k+1,N}(j)\f$ for \f$j<k\f$ and 
 *    \f$j\geq N-2k+1\f$ is zero. 
 *  - This probability function is symmetric: 
 *    \f$\sigma_{2k+1,N}(j) = \sigma_{2k+1,N}(n-j-1)\f$.
 *  \par	
 *  	Computes the moving median pmf corresponding to a input sequence 
 *      of size \em N, which values are given in the input sorted list, 
 *  	for window sizes \n
 *  	\em S = \em min_window_size \b to min(\em max_window_size, \em N) \b 
 *  	step \em step_size.
 *  \par
 *  	Stores the results in a SlideWindow data structure
 *  	(of size \f$O(N^2)\f$) before streaming them to the output port.
 *
 *  \par Notes:
 *  	-# The data type of the input sequence has to be compatible
 *  	with (and will be converted to) the data type specified by the 
 *  	parameter \em datatype. 
 *  	Accepted data types: \c i, \c d, \c f. 
 *  	-# The output probabilities will be computed using the 
 *  	data type specified by the parameter \em output_datatype. 
 *  	Accepted data types: \c d, \c f. 
 *  	-# \anchor note3 Each output array has size equal to the number
 *          of non-duplicate elements in the input sequence.
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
 *  Sep 18, 2007: deleted sw parameters.
 *  Feb 02, 2007: derived from module SWMedian.
 *  Created: December 07, 2005		
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
#include <sstream>
#include <string.h>
#include <string>
#include <vector>
#include <stdio.h>
#include <malloc.h>
#include <typeinfo>


#include "LogManager.H"
#include "Parameter.H"
#include "VecIO.H"
#include "Rank.H"
#include "SWMedianLogProb.H"
//#include "SWMedianLogProbNoStore.H"

using namespace std;

class SWMedianProbVL : public IModule{
  private:
    stringstream  *inSWParameters; 
    stringstream  *inSortedList; 
    stringstream  *outSWdata;
    
    //VL parameters
    Parameter<char> inputType;
    Parameter<char> outputType;
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<2,1> flag; 
    

  public:
    SWMedianProbVL()
        : 
	inputType("input_datatype", 'd'),
	outputType("output_datatype", 'd'),
	logLevel("log_level",Extreme),
	//logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"){
		Log::open(__func__,logLevel.value);
        inSWParameters = new stringstream();
        inSortedList = new stringstream();
        outSWdata = new stringstream();
		flag.set_in_name(1,"sw_parameters");
		flag.set_in_name(2,"sorted_list");
		flag.set_out_name(1,"probabilities");

		//vlport2
        rx_ports[1] = new MessageQueue("sw_parameters");
        rx_ports[2] = new MessageQueue("sorted_list");
		tx_ports[1] = new MessageQueue("probabilities");

    };

	void init(vector<string> rParam);
	void start();
	void stop();
    
    virtual ~SWMedianProbVL() throw() { 
    	delete outSWdata;
	delete inSortedList;
	delete inSWParameters;
    	Log::close();
    };

    template <class InType> void choose_out_type(void);
    template <class InType, class OutType> void compute(void);

};

void SWMedianProbVL::stop(){}

void SWMedianProbVL::init(vector<string> rParam)
{
    //get parameters
   	//Log::ptag(Release) << "Parameters"<<flush;
    //GET_PARAMETER(inputType);
    //GET_PARAMETER(outputType);
    //GET_PARAMETER(logLevel);
    //Log::set_warn_level(logLevel.value);
    //GET_PARAMETER(testPorts);    
	
}

void SWMedianProbVL::start() {

    Log::ptag(Info) << "SWMedianProbVL::vlmain() is called. " << flush;
   
    //compute medians
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

    Log::ptag(Info) << "SWMedianProbVL::vlmain() is finished. " << flush;
};    

template <class InType>
void SWMedianProbVL::choose_out_type(void){
    switch (outputType.value){
	case 'd':
	    compute<InType,double>();
	    break;
	case 'f':
	    compute<InType,float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << outputType.name 
		            << " badly defined! " << flush;
	    break;
    }
}

    
template <class InType, class OutType>    
void SWMedianProbVL::compute(void){

    LOG_PTAG(Info) << "Reading sorted vector from inSortedList ... " << flush;

	//changing RRR
	//MessageQueue::Message* im1 = rx_ports[2]->Read();
	MessageQueue::Message* im1 = rx_ports[1]->Read();
	inSortedList->write( (char*)im1->mpData, im1->mDataLength);
	cout << im1->mDataLength << endl;
	

    test_header(*inSortedList, "sorted_list", flag.get_in_flag(2));
    
    vector<InType> sortedList;
    string descr="";
    read_vector(*inSortedList, sortedList, descr);
    
    const long Size(sortedList.size());
	cout << "SORTED LIST SIZE: " << Size << endl;

    LOG_PTAG(Debug)<< "Allocating median data structure ... " << flush;

//changing RRR
	//MessageQueue::Message* im2 = rx_ports[1]->Read();
	MessageQueue::Message* im2 = rx_ports[2]->Read();
	inSWParameters->write( (char*)im2->mpData, im2->mDataLength);

    SlideWindowParameters p("",Size);
    test_header(*inSWParameters, "sw_parameters", flag.get_in_flag(1));
	cout << "here3" << endl;
    p.read(*inSWParameters);

	SWMedianProbOdd<OutType> exact(p);
    LOG_PTAG(Info)<< "Computing exact median probability values ... " << flush;
    
    LOG_PTAG(Debug)<< "Counting no duplicate values ... " << flush;
    long nNoDupl(1);
    InType oldValue(sortedList.at(0));
    for (unsigned j=1; j<sortedList.size(); ++j){
	InType value(sortedList.at(j));
	if (value != oldValue){//accumulate
	    ++nNoDupl;
	    oldValue = value;
    	}
    }
    exact.median_probability();
    
    //LOG_PTAG(Info)<< "Printing densities to prob.dat... " << flush;
    //ofstream outfile("/home2/inda/prob.dat");
    //exact.print_prob(nNoDupl, sortedList, outfile);
	//exact.test_prob(sortedList);

    

    LOG_PTAG(Info)<< "Writing probabilities... " << flush;
    write_test_header(*outSWdata, flag.get_out_flag(1));
    exact.write_prob(nNoDupl, sortedList, *outSWdata);

	
	MessageQueue::Message *om1 = new MessageQueue::Message();
    string tstr = outSWdata->str();
    char* pstr = (char*)tstr.c_str();

    om1->mDataLength = tstr.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,pstr,om1->mDataLength);
    tx_ports[1]->Write(om1);
       
}

//****************************************************************************

REGISTER_MODULE(SWMedianProbVL);

//end_of_file
