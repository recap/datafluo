//****************************************************************************
/** \class FDRThreshold FDRThreshold
 *  \ingroup ArrayStat Stat
 *  \brief  Computes false discovery rate (FDR) thresholds of a succession of 
 *  	observed frequency distributions.
 *
 *  \details
 *  \parameters
 *  <dl>
 *  	\param{datatype_thresh,datatype,d}
 *  	    Data type corresponding to the \em x_axis and 
 *  	    \em fdr_threshold input ports.
 *  	    Valid data types: \c i, \c d, \c f.  
 *  	\param{datatype_distr,datatype,d}
 *  	    Data type corresponding to the \em probabilities and 
 *  	    \em frequencies input ports. 
 *  	    Valid data types: \c d, \c f.  
 * 	\param{FDR_level,float,0.05} 
 *	    False discovery rate level. A number between 0.0 and 1.0.  
 *  	\param{threshold,string,high} 
 *	    Indicates which FDR threshold to compute: high or low. 
 * 	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *  \inputPorts
 *  <dl>
 *  	\port{probabilities,array,/array} 
 *  	    A succession of arrays, each array containing the \c y axis 
 *  	    values of a probability distribution.
 *  	\port{frequencies,array,/array} 
 *  	    A succession of arrays, each array containing the \c y axis
 *  	    values of a frequency distribution. 
 *  	\port{x_axis,sorted_set,/array/sorted_list/sorted_set} 
 *  	    The corresponding \c x axis coordinates.
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{fdr_threshold,number, /number} 
 *  	    The resulting FDR thresholds.
 *  </dl>
 *
 *
 * \description
 *  	Computes FDR thresholds of a succession of normalized observed
 *  frequency distributions. By comparing each frequency distribution with its
 *  corresponding probability distribution.  
 *
 *  \par Notes:
 *  	- The data type of the \c x axis coordinates in the \em x_axis 
 *  	input port has to be compatible with (and will be converted to) 
 *  	the datatype specified by the parameter \em datatype_thresh. 
 *  	This will be also the data type of the computed FDR thresholds. 
 *  	Accepted data types: \c i, \c d, \c f. 
 *  	- The data type of the data of the \c y axis values in the 
 *  	\em frequencies input port and in the \em probabilities input port 
 *  	has to be compatible with (and will be converted to) the datatype
 *  	specified by the parameter \em datatype_distr.
 *  	Accepted data types: \c d, \c f.  
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
 *  Created: January 02, 2006
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
#include <vector>

#include "LogManager.H"
#include "Parameter.H"
#include "VecIO.H"
#include "FDRAux.H"

#include "TimeLag.H"


using namespace std;


class FDRThresholdVL : public IModule {
  private:
    stringstream  *inStreamList; 
    stringstream  *inStreamProb; 
    stringstream  *inStreamFreq; 
    stringstream  *outStream; 
    
    //VL parameters
    Parameter<char> inputType;
    Parameter<char> outputType;
    Parameter<float> FDRPerc;
    Parameter<string> thresh;//>h,H= high (default), l,L=low
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<3,1> flag; 
    bool invert;
   
  public:
    FDRThresholdVL(): 
	inputType("datatype_thresh", 'd'),
	outputType("datatype_distr", 'd'),
	FDRPerc("FDR_level", 0.05), 
	thresh("threshold", "high"),
//	logLevel("log_level",DEFAULTLOGLEVEL),
	logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"),
	nBins(1){
	Log::open(__func__,logLevel.value);
	flag.set_in_name(1,"x_axis");
	flag.set_in_name(2,"probabilities");
	flag.set_in_name(3,"frequencies");
	flag.set_out_name(1,"fdr_threshold");
        inStreamList = new stringstream();
        inStreamProb = new stringstream(); 
        inStreamFreq = new stringstream();
        outStream = new stringstream();

		//vlport2
        rx_ports[1] = new MessageQueue("Iport1");//(char*)flag.get_in_name(1));
        rx_ports[2] = new MessageQueue("probabilities");
        rx_ports[3] = new MessageQueue("frequencies");
        tx_ports[1] = new MessageQueue("fdr_threshold");

     };

	void start();
	void stop();
	void init(vector<string> rParam);
    
    virtual ~FDRThresholdVL() throw() {
    	delete outStream; 
	delete inStreamFreq;
	delete inStreamProb;
	delete inStreamList;
	Log::close();
    };

    

  private:
    template <class InType> void choose_out_type(void);
    template <class InType, class OutType> void compute_FDR(void);
    template <class InType, class OutType> 
    void compute_p_values(ostream& SumTailFile, vector<InType>& valueList, 
    	vector<OutType>& freqData, vector<OutType>& probData);
    int nBins;
	
};

void FDRThresholdVL::stop(){}

void FDRThresholdVL::init(vector<string> rParam){
    //get parameters
    /*Log::ptag(Release) << "Parameters:"<<flush;
    GET_PARAMETER(inputType);
    GET_PARAMETER(outputType);
    GET_PARAMETER(FDRPerc);
    GET_PARAMETER(thresh);
    invert=((thresh.value[0]=='l')||(thresh.value[0]=='L')? true : false);
    GET_PARAMETER(logLevel);
    Log::set_warn_level(logLevel.value);
    GET_PARAMETER(testPorts);    
    flag.set(testPorts.value);
    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;*/
    invert=((thresh.value[0]=='l')||(thresh.value[0]=='L')? true : false);
    Log::set_warn_level(logLevel.value);
    flag.set(testPorts.value);
}
void FDRThresholdVL::start() {
    Log::ptag(Release) << "FDRThresholdVL::vlmain() is called" << flush;
    
    //compute FDR
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
		            << " badly defined!" << flush;
	    break;
    }
    
    Log::ptag(Release) << "FDRThresholdVL::vlmain() is finished"<<flush;
};    

template <class InType>
void FDRThresholdVL::choose_out_type(void){
    switch (outputType.value){
	case 'd':
	    compute_FDR<InType,double>();
	    break;
	case 'f':
	    compute_FDR<InType,float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << outputType.name 
		            << " badly defined!" << flush;
	    break;
    }
}

#undef SUMTAIL
template <class InType, class OutType> 
void FDRThresholdVL::compute_FDR(){
    TimeLag timeRead;

    LOG_PTAG(Info) << "Reading list of possible values ..." << flush;
    timeRead.start("FDRReadList");

	MessageQueue::Message* im1 = rx_ports[3]->Read();
	inStreamList->write( (char*)im1->mpData, im1->mDataLength);
	

    test_header(*inStreamList, flag.get_in_name(3),flag.get_in_flag(3));
    vector<InType> valueList;
    string descr="";
    read_vector(*inStreamList, valueList, descr);
    timeRead.finish("FDRReadList");
    timeRead.reset();
    
    TimeLag timeComp, timeWrite;
    timeWrite.start("FDRWrite");
    timeWrite.pause("FDRWrite");
    timeComp.start("FDRCompute");
    timeComp.pause("FDRCompute");
    
    timeRead.start("FDRRead");
    LOG_PTAG(Debug) << "Testing inStreamFreq ..." << flush;
//RRR
	MessageQueue::Message* im2 = rx_ports[1]->Read();
	inStreamFreq->write( (char*)im2->mpData, im2->mDataLength);

    test_header(*inStreamFreq, flag.get_in_name(1),flag.get_in_flag(1));
    
    LOG_PTAG(Debug) << "Testing inStreamProb ..." << flush;

	MessageQueue::Message* im3 = rx_ports[2]->Read();
	inStreamProb->write( (char*)im3->mpData, im3->mDataLength);

    test_header(*inStreamProb, flag.get_in_name(2),flag.get_in_flag(2));
  
    LOG_PTAG(Info) << "Computing FDR ..." << flush;
    write_test_header(*outStream,flag.get_out_flag(1));
        
#ifdef SUMTAIL
    ofstream SumTailFile("SumTail.dat");
    if(!SumTailFile){
        LOG_PTAG(Fatal)<<"Error opening output file SumTail.dat\n"<<flush; 
	exit(1);
    }
    Log::ptag(Release) <<"Writing tails to SumTail.dat"<<flush;
#endif
      
    unsigned size = valueList.size();
    vector<OutType> freqData;
    vector<OutType> probData;
    string descr_freq="";
    string descr_prob="";
    int count(0); 
	//inStreamFreq << eof;
    while( (read_vector(*inStreamFreq, freqData, descr_freq))&&
    	   (read_vector(*inStreamProb, probData, descr_prob)) ){
	if ( (freqData.size()!=size) ||  (probData.size() != size) ){
	    LOG_PTAG(Fatal) << "Wrong vector size! " << freqData.size() << ":" << probData.size() << ":" << size << endl;
	    	/*<< "freq.at(" << freqData.size() << "-1)=" 
	    	<< freqData.at(freqData.size()-1)   
		<< "prob.at(" << probData.size() << "-1)=" 
	    	<< probData.at(probData.size()-1) << flush; */
	    exit(1);
	}
	    LOG_PTAG(Fatal) << "Vector sizes! " << freqData.size() << ":" << probData.size() << ":" << size << endl;
	    cout << "Vector sizes! " << freqData.size() << ":" << probData.size() << ":" << size << endl;
	
    	timeRead.pause("FDRRead");
	++count;
		

    	LOG_PTAG(Info) << "FDRThreshold " << descr_prob << "/" << descr_freq
	    	    	<< flush;
    	Log::print(Debug) << "(" << count <<")"<<flush;
	Log::print(Info)<<" =" << flush;
    	descr_prob="";
	descr_freq="";    
	
 	
#ifdef SUMTAIL
    	compute_p_values(SumTailFile, valueList, freqData, probData);
#endif
    	timeComp.cont("FDRCompute");
	
	long index=threshold_index(freqData, probData, invert, FDRPerc.value);
		//index--; //RRR
    	timeComp.pause("FDRCompute");
    
    	Log::print(Debug) <<  " thresh=";
    	Log::print(Info) <<valueList.at(index) << endl << flush;
	if ((!invert) && (index==int(size-1)))
	    Log::print(Debug)<<" (maximum) ";
	if ((invert) && (index==0))
	    Log::print(Debug)<<" (minimum) ";
				
    	timeWrite.cont("FDRWrite");
	write_struct(*outStream, valueList.at(index));
	outStream->flush();

/*	MessageQueue::Message *om1 = new MessageQueue::Message();
	string tstr = outStream->str();
	char* pstr = (char*)tstr.c_str();
    om1->mDataLength = tstr.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,pstr,om1->mDataLength);
    tx_ports[1]->Write(om1);*/

	

    	timeWrite.pause("FDRWrite");
		
	freqData.clear();
	probData.clear();
    	timeRead.cont("FDRRead");
    }
	MessageQueue::Message *om1 = new MessageQueue::Message();
	string tstr = outStream->str();
	char* pstr = (char*)tstr.c_str();
    om1->mDataLength = tstr.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,pstr,om1->mDataLength);
    tx_ports[1]->Write(om1);

    LOG_PTAG(Debug) << "total count=" << count<<flush;
  
#ifdef SUMTAIL
    SumTailFile.close();
#endif
    timeRead.finish("FDRRead");
    timeWrite.finish("FDRWrite");
    timeComp.finish("FDRCompute");
}


template <class InType, class OutType> 
void FDRThresholdVL::compute_p_values(ostream& SumTailFile, 
    vector<InType>& valueList, 
    vector<OutType>& freqData, vector<OutType>& probData){
    
    int size = valueList.size();
    
    SumTailFile << "#size= " << size << " nBins= " << nBins << endl<<flush;
    SumTailFile <<"#Value\t count\t Ratio\t sumProb\t sumFreq\t prob\t freq\n"
     	<< flush;
	
    OutType ratio(0);
    OutType sumFreq(0);
    OutType sumProb(0);
    int index(size-1);
    double minFreq;
    minFreq=1.0/size;
    
    while (index>=0){    
    	OutType binFreq(0);
	OutType binProb(0);
	int count(0);
	//while ((binFreq<minFreq)&&(index>=0)){
	while ((count<nBins)&&(index>=0)){
	    binFreq += freqData.at(index);
	    binProb += probData.at(index);
	    ++count;
	    --index;
	}
	sumFreq+=binFreq;
	sumProb+=binProb;
	ratio = (sumFreq>0 ? sumProb/sumFreq : 0);
    	SumTailFile << setw(5) << valueList.at(index+1) << " " 
	    	    << setw(2) << count <<" "
	    	    << setw(12) << ratio   <<" "
	    	    << setw(12) << sumProb << " " 
		    << setw(12) << sumFreq <<" ";
    	SumTailFile << setw(12) << binProb << " " 
	    	    << setw(12) << binFreq <<"\n";
    }
    SumTailFile<<endl<<flush;
}
//**********************************************************

REGISTER_MODULE(FDRThresholdVL);

//end_of_file
