//****************************************************************************
/** \class SigWinSelect SigWinSelect
 *  \ingroup SigWin SW
 *  \brief Selects significant windows, and write them to a file in a 
 *  	    format suitable for plotting SigWin-maps.   
 *
 *  \details
 *  \parameters
 *  <dl>
 *  	\param{output_file,string,SigWin} 
 *	    Output local file name. 
 *  	\param{format,string,2d} 
 *	    Indicates the format of the output file. 
 *  	    Allowed values \c 2d or \c 3dmap. 	
 *  	\param{datatype,datatype,d}
 *  	    Data type of the input data.
 *  	    Valid data types: \c i, \c d, \c f.  
 *  	\param{threshold,string,high} 
 *	    Indicates which FDR threshold was computed: high or low. 
 *  	\param{write2port,boolean,no} 
 *	    If \b \c yes, writes output file to output port. 
  	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level.
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  </dl>    
 *  \inputPorts
 *  <dl>
 *  	\port{sw_parameters,swparameters,/SWParameters} 
 *  	    A succession of SWParameters structures defining the input
 *  	    SlideWindow structures.
 *  	\port{sw_data,array,/array} 
 *  	    A succession of arrays containing the data corresponding to 
 *  	    each SlideWindow structure.
 *  	\port{fdr_threshold, number, /number} 
 *  	    The FDR thresholds for each window size.
 *  </dl>
 *
 *  \outputPorts
 *  <dl>
 *  	\port{data_file_name, string,/string} 
 *  	    The name of the local file to which the selected significant 
 *  	    windows were written.
 *  	\port{data_file, file,/file} 
 *  	    The file containing the selected significant windows.
 *  </dl>
 *
 *
 * \description
 *  	Receives a succession of SlideWindow structures and a succession of   
 *  	FDR thresholds, selects the corresponding significant windows, and
 *  	print them to a file in a format suitable for plotting SigWin-maps.   
 *
 *  \par Notes:
 *  	- All the SlideWindow structures must have the same 
 *  	::wMin (minimum window size), and ::wStep (step size), and they must
 *  	correspond to windows sizes of the input thresholds.  
 *  	- The data type of the input data has to be compatible with
 *	(and will be converted to) the datatype specified by the parameter 
 *  	\em datatype. 
 *
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since September 28, 2007.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Sep 18, 2007: test_ports parameter added.
 *  Created: January 12, 2007
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
#include <stdio.h>
#include <string.h>
#include <malloc.h>


#include "LogManager.H"
#include "Parameter.H"
#include "VecIO.H"
#include "SlideWindowParameters.H"
#include "serializer.h"
#include "GnuConfig.H"
#include "SigWinAux.H"

#include "TimeLag.H"

static const int MAXPOINTS(1000);
static const int MAXPPSLICE(100);

typedef vector<SlideWindowParameters>::iterator swpiter;

using namespace std;

class SigWinSelectVL : public IModule{
  private:
    stringstream  *inSWParameters; 
    stringstream  *inSWData; 
    stringstream  *inFDRThresh; 
    stringstream  *outFileName;
    stringstream  *outStreamFile;
    
    //VL parameters
    Parameter<char> inputType;
    Parameter<string> fileName;
    Parameter<bool> write2port;
    Parameter<string> thresh;
    Parameter<string> outFormat;
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<3,2> flag; 
    bool invert;
    
  public:
    SigWinSelectVL()
        : 
	inputType("datatype", 'd'),
	fileName("output_file","SigWin.out"),
	write2port("write2port","true"),
	thresh("threshold","high"),
	outFormat("format","2d"),
	//logLevel("log_level",DEFAULTLOGLEVEL),
	logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"),
	invert(false){
	//Log::open(__func__,logLevel.value);
        inSWParameters = new stringstream();
        inSWData = new stringstream(); 
        inFDRThresh = new stringstream();
        outFileName = new stringstream();
        outStreamFile = new stringstream();
	flag.set_in_name(1,"sw_parameters");
	flag.set_in_name(2,"sw_data");
	flag.set_in_name(3,"fdr_threshold");
	flag.set_out_name(1,"data_file_name");
	flag.set_out_name(2,"data_file");
	INIT_PORTS();

	//rx_ports[1] = new MessageQueue("sw_parameters");
	//rx_ports[2] = new MessageQueue("sw_data");
	//rx_ports[3] = new MessageQueue("fdr_threshold");
	//tx_ports[1] = new MessageQueue("data_file_name");
	//tx_ports[2] = new MessageQueue("data_file");
	MAP_RX_PORT(1,sw_parameters);
	MAP_RX_PORT(2,sw_data);
	MAP_RX_PORT(3,fdr_threshold);
	MAP_TX_PORT(1,data_file_name);
	MAP_TX_PORT(2,data_file);

	
    };
	void start();
	void stop();
	void init(vector<string>* rParam);
    
    virtual ~SigWinSelectVL() throw() { 
    	delete outStreamFile;
    	delete outFileName;
		delete inFDRThresh;
		delete inSWData;
		delete inSWParameters;
    	//Log::close();
    };


  private:
    template <class InType> void compute(void);
    
    template <class InType> 
    void print_header(ostream& outStream, SlideWindowParameters& p,
     	const InType& FDRThresh);
    
};


void SigWinSelectVL::stop(){}
void SigWinSelectVL::init(vector<string>* rParam){
    //get parameters
    /*LOG_PTAG(Release)<< "Parameters:"<<flush;
    GET_PARAMETER(inputType);
    GET_PARAMETER(fileName);  
    GET_PARAMETER(write2port);  
    GET_PARAMETER(thresh);
    invert=((thresh.value[0]=='l')||(thresh.value[0]=='L')? true : false);
    GET_PARAMETER(outFormat);   
    GET_PARAMETER(logLevel);
    Log::set_warn_level(logLevel.value);
    GET_PARAMETER(testPorts);
    flag.set(testPorts.value);
    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;*/

}

void SigWinSelectVL::start(){

    Log::ptag(Info) << "SigWinSelectVL::vlmain() is called. " << flush;

   
    //compute medians
    switch (inputType.value){
	case 'i':
	    compute<int>();
	    break;
	case 'd':
	    compute<double>();
	    break;
	case 'f':
	    compute<float>();
	    break;
	default:
	    LOG_PTAG(Fatal) << "Parameter " << inputType.name 
		            << " badly defined! " << flush;
	    break;
    }

    Log::ptag(Info) << "SigWinSelectVL::vlmain() is finished. " << flush;
};    

   
//****************************************************************************
template <class InType>    
void SigWinSelectVL::compute(void){
    TimeLag timeRead;
    TimeLag timeComp;

	//MessageQueue::Message* im1 = rx_ports[3]->Read();RRR
	//MessageQueue::Message* im1 = rx_ports[1]->Read();
	MessageQueue::Message* im1 = READ_PORT(1);
	inSWParameters->write( (char*)im1->mpData, im1->mDataLength);
	SET_RX_PORT_STATE(1,1);
	SIGNAL_RX_PORT(1);
	//MessageQueue::Message* im2 = rx_ports[2]->Read();
	MessageQueue::Message* im2 = READ_PORT(2);
	inSWData->write( (char*)im2->mpData, im2->mDataLength);
	SET_RX_PORT_STATE(2,1);
	SIGNAL_RX_PORT(2);
	//MessageQueue::Message* im3 = rx_ports[1]->Read();RRR
	//MessageQueue::Message* im3 = rx_ports[3]->Read();
	MessageQueue::Message* im3 = READ_PORT(3);
	inFDRThresh->write( (char*)im3->mpData, im3->mDataLength);
	SET_RX_PORT_STATE(3,1);
	SIGNAL_RX_PORT(3);

    //------------------------------------------------------------
    test_header(*inSWParameters, flag.get_in_name(1),flag.get_in_flag(1));
    test_header(*inSWData,flag.get_in_name(2),flag.get_in_flag(2));
    test_header(*inFDRThresh, flag.get_in_name(3),flag.get_in_flag(3));
        
    //open output file
    ofstream outFile(fileName.value.c_str());
    if (!outFile.is_open()){
    	LOG_PTAG(Fatal)<<"Error opening output file "<<fileName.value
	    <<endl<<flush;
	exit(1);
    }   
    LOG_PTAG(Release) <<"Writing significant window data to file: "
    	    	      << fileName.value <<flush;
       
    
    int direction=(invert?-1:1);

    double (*print_function)(ostream&, const vector<InType>&, const InType, 
    	const long&, const int);
    if ((outFormat.value=="2d")||(outFormat.value=="2D"))
    	print_function= &print_SigWin2d;
    else
    	print_function= &print_SigWinGnuplot3d;
	
    LOG_PTAG(Info) << "Selecting significant windows ... " << flush;
    vector<InType> FDRThresh;
    SlideWindowParameters swpar;
    
    timeRead.start("ReadData");
    timeRead.pause("ReadData");
    timeComp.start("WriteData");
    timeComp.pause("WriteData");
    
    long cSW(0), cThresh(0);
    
    while(swpar.read(*inSWParameters)){//for each SW structure
    	++cSW;
     	LOG_PTAG(Info) << "Processing SW structure "<< cSW <<" ..." << flush;
    	for (long i=0; i<swpar.nWSizes(); i++){//for each window size
	    string descr="";
    	    vector<InType> data;
      	    LOG_PTAG(Debug) << "Reading data "<< i << flush;
    	    timeRead.cont("ReadData");
    	    if (!read_vector(*inSWData, data, descr)){
    	    	LOG_PTAG(Fatal)<<"Problem reading SW structure " << cSW 
		    << "window size "<< swpar.window_size(i) << "!"<<flush; 
	    	exit(1);
	    }
    	    timeRead.pause("ReadData");
	    if(long(FDRThresh.size())<=i){
      	    	LOG_PTAG(Debug) << "Reading thresh "<< cThresh << flush;
   	    	InType aux;
	    	if(!read_struct(*inFDRThresh, aux)){
	    	    LOG_PTAG(Fatal)<<"Problem reading FDR threshold "
		    	<< i <<"!" <<flush;
	    	    exit(1);
		}
    	    	FDRThresh.push_back(aux);
	    	++cThresh;
	    }
	    
	    //Select and write the selected windows
    	    timeComp.cont("WriteData");
   	    if (i==0) 
	    	print_header(outFile, swpar, FDRThresh.at(0));
	    print_function(outFile, data, FDRThresh.at(i), swpar.window_size(i),
	    	direction);	
    	    timeComp.pause("WriteData");
	}
        outFile << endl<<endl;
    }
    
    if (cSW==0) 
	LOG_PTAG(Warning)<< "No input SW data!"<<flush;
    else
    	Log::print(Release) << "Number of SW structures: " << cSW <<flush;
    
    Log::print(Release) << "Threshold vector size= " << cThresh <<flush;
       
    outFile.close();
    
    timeRead.finish("ReadData");
    timeComp.finish("WriteData");

    LOG_PTAG(Release) << "Data file ready. "<<flush;
    write_test_header(*outFileName, flag.get_out_flag(1));
    *outFileName << fileName.value << flush;

	 MessageQueue::Message *om1 = new MessageQueue::Message();
     string tstr = outFileName->str();
     char* pstr = (char*)tstr.c_str();
     om1->mDataLength = tstr.size();
     om1->mpData = (void*)malloc(om1->mDataLength);
     memcpy(om1->mpData,pstr,om1->mDataLength);
     //tx_ports[1]->Write(om1);
     WRITE_PORT(1,om1);
    
    Log::print(Info) << " OK." << flush;
	 WRITE_PORT(1,NULL);
}
    
//****************************************************************************
template <class InType>    
void SigWinSelectVL::print_header(ostream& outStream, SlideWindowParameters& p,
    const InType& thresh){

    //printing data   
    outStream << "#Windows "<< (invert?"beneath":"above") 
    	<<" the FDR threshold for " <<p.description() << endl ;
    if ((outFormat.value=="2d")||(outFormat.value=="2D")){
    	outStream << 
	    "#printing a point at wMin to get correct number of sets\n";     		    	outStream << p.wMin()<< " " << p.wMin()/2+1 <<" "<< p.wMin()/2+1<<endl;
	outStream <<"#windowSize first last" << endl;
    }
    else{
    	outStream<<"#printing threshold at wMin 3 times to prevent isolines\n";     	for (int i=0; i<3; ++i){
	    outStream <<p.wMin()/2+1<< " " << p.wMin() << " " << thresh <<endl;
    	}
	outStream << endl<<"#windowNumber windowSize value" << endl;
    }

}
    	
    
//****************************************************************************
REGISTER_MODULE(SigWinSelectVL);
//end_of_file
