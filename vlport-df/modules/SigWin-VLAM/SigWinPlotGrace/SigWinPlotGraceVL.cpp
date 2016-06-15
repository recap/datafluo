//****************************************************************************
/** \class SigWinPlotGrace SigWinPlotGrace
 *  \ingroup SigWin Graphics
 *  \brief  Creates a xmgrace commands file  with instructions to plot
 *  	    a collection of SigWin-maps.
 
 *  \details 
 *  \parameters
 *  <dl>
 *  	\param{separation,float,0.2}
 *  	    A factor determining the separation between two SigWin-maps.  
 *  	\param{ratio,float,1}
 *  	    The approximate aspect ratio for arranging the SigWin-map in rows 
 *  	    and columns.
  *  	\param{full_border,boolean,no}
 *  	    If \b \c yes, plots the whole triangular border of the 
 *  	    	SigWin-maps.\n
 *  	    Otherwise, plots only the area limited by the SWParameters
 *  	    	 structures
 *  	\param{log_level,log_label,Info} 
 *	    Logs messages with level higher or equal to \em log_level
 *  	\param{test_ports,TestFlag,all} 
 *  	    Selects which ports will 
 *	    send/receive a test header to test number conversion. 
 *  \inputPorts
 *  <dl>
 *  	\port{sw_parameters,swparameters,/SWParameters} 
 *  	    A succession of SWParameters structures defining the input
 *  	    SlideWindow structures.
 *  	\port{data_file_names,string,/string} 
 *  	    A succession of names of the local files containing the selected 
 *  	    significant windows. (See SigWinSelect)
 *  </dl>
 *  \outputPorts
 *  <dl>
 *  	\port{grace_file, file,/file} 
 *  	    The created xmgrace commands file.
 *  </dl>
 * 
 *  \description
 *  Creates an xmgrace commands file  with instructions to plot
 *  	    a collection of SigWin-maps.
 *  \par
 *  There will be as many SigWin-maps as SWParameters structures received by 
 *  the \em sw_parameters input port. The data corresponding to each 
 *  SigWin-map is stored in the input files. Each input file containing 
 *  the data for a complete set of SigWin-maps. Significant windows 
 *  corresponding to the same file will have the same color. 
 *  \par Note:
 *  	All input files must contain data for the complete set of SlideWindow 
 *  	structures defined by the SWParameters structures received by the
 *  	\c sw_parameters input port.
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
 *  Created: January 15, 2007
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
#include <string>
//#include <grace_np.h>
#include <cmath>

#include "LogManager.H"
#include "Parameter.H"
#include "VecIO.H"
#include "SlideWindowParameters.H"
#include "serializer.h"
#include "Shape.H"

#include "TimeLag.H"


typedef vector<SlideWindowParameters>::iterator swpiter;

using namespace std;


class SigWinPlotGraceVL : public IModule{
  private:
    stringstream  *inSWParameters; 
    stringstream  *inDataFiles; 
    stringstream  *outPlotFile;
    
    //VL parameters
    Parameter<double> factor; //>separation between graphs factor
    Parameter<double> ratiolc; //>~aspect ratio between nlines and ncols
    Parameter<bool> triangBorder; //>triangular border? yes / no
    Parameter<WarnType> logLevel; //>Logging level.
    Parameter<string> testPorts; //> Sets test_header function.
    TestFlags<2,1> flag; 
    string defaultName;
    
  public:
    SigWinPlotGraceVL():
	factor("separation",0.2),
	ratiolc("ratio", 1.0),
	triangBorder("full_border",false),
	//logLevel("log_level",DEFAULTLOGLEVEL),
	logLevel("log_level",Extreme),
	testPorts("test_ports","all"),
	flag("both"),
	defaultName("SigWin"){
	//Log::open(__func__,logLevel.value);
    inSWParameters = new stringstream();
    inDataFiles = new stringstream();
    outPlotFile = new stringstream();
	flag.set_in_name(1,"sw_parameters");
	flag.set_in_name(2,"data_file_names");
	flag.set_out_name(1,"grace_file");
	INIT_PORTS();

    //rx_ports[1] = new MessageQueue("sw_parameters");
    //rx_ports[2] = new MessageQueue("data_file_names");
    //tx_ports[1] = new MessageQueue("grace_file");
	MAP_RX_PORT(1,sw_parameters);
	MAP_RX_PORT(2,data_file_names);
	MAP_TX_PORT(1,grace_file);

    };
    
    virtual ~SigWinPlotGraceVL() throw() { 
    	delete outPlotFile;
    	delete inDataFiles;
		delete inSWParameters;
    	//Log::close();
    };
	void start();
	void stop();
	void init(vector<string>* rParam);
 
  private:
    string create_plot_file();
    double multInOnePlot_config(void);

    //auxiliary variables	
    vector<SlideWindowParameters> p;
    vector<string> dataFileName;
    vector<Shape<4,2,double> > border;
    vector<Point<2,double> > lowCorner;
    vector<Point<2,double> > upCorner;

    int max_size(const SlideWindowParameters& P) const{
    	return (triangBorder.value? P.size() : P.max_nWindows());
    }

    int max_heig(const SlideWindowParameters& P) const{
    	return(triangBorder.value? P.size() : P.range_nWindows());
    }

};

void SigWinPlotGraceVL::stop() {}
void SigWinPlotGraceVL::init(vector<string>* rParam) {
		//get parameters
    /*LOG_PTAG(Release)<< "Parameters:"<<flush;
    GET_PARAMETER(factor);
    GET_PARAMETER(ratiolc);
    GET_PARAMETER(triangBorder);
    GET_PARAMETER(logLevel);
    Log::set_warn_level(logLevel.value);
    GET_PARAMETER(testPorts);
    flag.set(testPorts.value);
    LOG_PTAG(Info)<<"Ports to test: "<<flag<<flush;*/


}

void SigWinPlotGraceVL::start() {

	//MessageQueue::Message* im1 = rx_ports[1]->Read();
	MessageQueue::Message* im1 = READ_PORT(1);
    inSWParameters->write( (char*)im1->mpData, im1->mDataLength);
	SET_RX_PORT_STATE(1,1);
	SIGNAL_RX_PORT(1);

    //------------------------------------------------------------
    test_header(*inSWParameters, flag.get_in_name(1), flag.get_in_flag(1));
    SlideWindowParameters par_aux;
    LOG_PTAG(Detail) << "SWParameters: " << flush;


    while (par_aux.read(*inSWParameters)){
	p.push_back(par_aux);
	Log::print(Detail)<< par_aux <<" "<<flush;
    }//while
    LOG_PTAG(Info)<< "Total number of slide window sequences= " << p.size(); 

    //------------------------------------------------------------
	//MessageQueue::Message* im2 = rx_ports[2]->Read();
	MessageQueue::Message* im2 = READ_PORT(2);
    inDataFiles->write( (char*)im2->mpData, im2->mDataLength);
	SET_RX_PORT_STATE(2,1);
	SIGNAL_RX_PORT(2);

    test_header(*inDataFiles, flag.get_in_name(2), flag.get_in_flag(2));
    string description="";
    scan_vector(*inDataFiles, dataFileName, description);
    LOG_PTAG(Info)<< "Total number of input files= " << dataFileName.size(); 

    
    //-----------------------------------------------------------
    TimeLag TimeProc;
    TimeProc.start("TimeProc");
    LOG_PTAG(Release) << "Creating grace config file... "<<flush;
    string plotFileName = create_plot_file();
    Log::print(Info) << " OK." << flush;
   
   
    
    //-----------------------------------------------------------
    LOG_PTAG(Info) << "Streaming plot file."<<flush;   
    Serializer serializer;
    ifstream inFile(plotFileName.c_str());
    if (inFile.is_open()){
    	LOG_PTAG(Release) << "Writing grace config file to outPort. "<<flush;
    	*outPlotFile << serializer << inFile;

		MessageQueue::Message *om1 = new MessageQueue::Message();
        string tstr = outPlotFile->str();
        char* pstr = (char*)tstr.c_str();

        om1->mDataLength = tstr.size();
        om1->mpData = (void*)malloc(om1->mDataLength);
        memcpy(om1->mpData,pstr,om1->mDataLength);
        //tx_ports[1]->Write(om1);
		WRITE_PORT(1,om1);

	inFile.close();
    }
    else{
    	LOG_PTAG(Fatal) << "Problem opening file " << plotFileName 
	    << " for reading. " <<flush;
    }
	WRITE_PORT(1,NULL);
    Log::print(Info) << " OK." << flush;
    TimeProc.finish("TimeProc");    
};    
    

//****************************************************************************
string SigWinPlotGraceVL::create_plot_file(void){
    double xSize = multInOnePlot_config();

    string mainName=p.at(0).description();
    string::size_type size=mainName.find_last_of(":");
    if (mainName=="") 
    	mainName=defaultName;
    else if ((size!=0)&&(size!=string::npos)) 
    	mainName.resize(size);
    ++size;
    LOG_PTAG(Debug)<<"psize="<<int(p.size())<<flush;

    for (int i=0; i<int(p.size()); ++i){
    	LOG_PTAG(Debug)<<"Processing set " << i <<flush;
	    
    	Log::print(Detail)<<" borders"<<flush;
    	Shape<4,2,double> baux;//borders are limited by wMin and wMax
	long wMin = p.at(i).wMin();
	long wMax = p.at(i).wMax(); 
    	if (triangBorder.value){
	    baux.vertex[0].set(1, 1);
    	    baux.vertex[1].set(p.at(i).size(), 1);
    	    baux.vertex[2].set(p.at(i).size()/2.0,p.at(i).size());
    	    baux.vertex[3].set(p.at(i).size()/2.0,p.at(i).size());
    	}
	else{
	    baux.vertex[0].set(p.at(i).first(wMin), wMin);
    	    baux.vertex[1].set(p.at(i).last(wMin),  wMin);
    	    baux.vertex[2].set(p.at(i).last(wMax),  wMax);
    	    baux.vertex[3].set(p.at(i).first(wMax), wMax);
    	}
		 
	border.push_back(baux);		
	LOG_PTAG(Detail)<< i << " border=" << baux;
    	
    }
    

    //open plot file
    string plotFileName = mainName + ".agrcmd";
    LOG_PTAG(Release)<<"Writing "<< plotFileName <<flush;
    ofstream plotFile(plotFileName.c_str());
    if (!plotFile.is_open()){
    	LOG_PTAG(Fatal)<<"Error opening grace config file " << plotFileName
	    <<endl<<flush; 
	exit(1);
    } 
   
    //print plot file
    LOG_PTAG(Info) << "#ploting significant windows for sub regions";
    plotFile<<"@timestamp off\n";
    plotFile<<"@page size 600 600\n";
    double tick = (xSize<100?10:int(xSize/100)*10);
    
    for (int i=0; i<int(p.size()); ++i){
     	LOG_PTAG(Debug)<< "#Ploting subwindow: "<< i << flush;
	
    	plotFile<<"@G" <<i<<" on\n";
    	plotFile<<"@G" <<i<<" type XY\n";
    	plotFile<<"@G" <<i<<" bar hgap 0.000000\n";	
    	plotFile<<"@with G" <<i<<endl;
	Point<2,double>::set_separator(", ");
    	plotFile<<"@    world "<< (border.at(i).vertex[0]+(-1.0)) <<", "   
	    <<border.at(i).vertex[1].get_x() << ", " 	
	    <<border.at(i).vertex[2].get_y()<<endl;
    	plotFile<<"@    view "<< lowCorner.at(i) << ", "<<upCorner.at(i)<<endl;
	LOG_PTAG(Debug)<< "size=" <<size <<  flush;
	LOG_PTAG(Debug)<< "("<< p.at(i).get_description()<<")"<< flush;
    	plotFile<<"@    xaxis label \""<<p.at(i).get_description().substr(size)
	    << "\"\n";
   	plotFile<<"@    xaxis label char size 0.6\n";
    	plotFile<<"@    xaxis  bar off\n";
    	plotFile<<"@    yaxis  bar off\n";
    	plotFile<<"@    yaxis  tick off\n";
   	plotFile<<"@    xaxis  tick place normal\n";
   	plotFile<<"@    yaxis  tick place normal\n";
	plotFile<<"@    xaxis tick major "<<tick<< "\n";
	plotFile<<"@    xaxis tick minor "<<tick/4<<"\n";
	plotFile<<"@    yaxis tick major "<<tick<< "\n";
	plotFile<<"@    yaxis tick minor "<<tick/4<<"\n";
	plotFile<<"@    autoticks\n";
	plotFile<<"@    xaxis tick major size "<<0.5<< "\n";
	plotFile<<"@    xaxis tick minor size "<<0.25<<"\n";
    	plotFile<<"@    xaxis  ticklabel char size 0.5\n";
    	plotFile<<"@    xaxis  tick out\n";
    	plotFile<<"@    yaxis  ticklabel char size 0.5\n";
	plotFile<<"@    legend off\n";
	plotFile<<"@    frame type 0\n";
	plotFile<<"@    frame linestyle 0\n";
	plotFile<<"@    S0 line type 1\n";
	plotFile<<"@    S0 type xy\n";
	plotFile<<"@    S0 point " << border.at(i).vertex[0]<<endl;
	plotFile<<"@    S0 point " << border.at(i).vertex[1]<<endl;
	plotFile<<"@    S0 point " << border.at(i).vertex[2]<<endl;
	plotFile<<"@    S0 point " << border.at(i).vertex[3]<<endl;
	plotFile<<"@    S0 point " << border.at(i).vertex[0]<<endl;
    }
    LOG_PTAG(Info) << "Ploting files " <<flush;
    for (unsigned j=0; j<dataFileName.size(); ++j){
    	LOG_PTAG(Info) << dataFileName.at(j)<<flush;
    	plotFile<<"@WITH G"<<p.size()<<endl;
	plotFile<<"@G"<<p.size()<<" HIDDEN TRUE\n";
    	plotFile<<"@READ xydx \""<<dataFileName.at(j)<<"\"\n";
    	for (int i=int(p.size()-1); i>=0; --i){
    	    plotFile<<"@WITH G"<<p.size()<<endl;
	    plotFile<<"@    S"<<i<<".Y1=S"<<i<<".Y1-S"<<i<<".Y\n";
   	    plotFile<<"@copy G"<<p.size()<<".S"<<i<<" TO G"<<i<<".S" <<j+1<<endl;
	    plotFile<<"@G"<<i<<".S"<<j+1<<".X=S"<<i<<".Y\n";
	    plotFile<<"@G"<<i<<".S"<<j+1<<".Y=S"<<i<<".X\n";
	    plotFile<<"@kill G"<<p.size()<<".S"<<i<<endl;
   	    plotFile<<"@WITH G"<<i<<endl;
	    plotFile<<"@    S"<<j+1<<" type xydx\n";
    	    plotFile<<"@    S"<<j+1<<" symbol 0\n";
	    plotFile<<"@    S"<<j+1<<" line type 0\n";
    	    plotFile<<"@    S"<<j+1<<" errorbar on\n";
    	    plotFile<<"@    S"<<j+1<<" errorbar place normal\n";
    	    plotFile<<"@    S"<<j+1<<" errorbar color "<<j+2<<endl;
    	    plotFile<<"@    S"<<j+1<<" errorbar linewidth 1.0\n";
    	    plotFile<<"@    S"<<j+1<<" errorbar linestyle 1\n";
    	    plotFile<<"@    S"<<j+1<<" errorbar size 0\n";
   	    plotFile<<"@    S"<<j+1<<" legend  \""<<dataFileName.at(j)<<"\"\n";
    	}
    }
//    plotFile<< "saveall \""<<mainName <<".arg\"\n";
    plotFile.close();
    return plotFileName;
    
}

//****************************************************************************
///Computes the position of each subgraph according with the data in p
double SigWinPlotGraceVL::multInOnePlot_config(){ 
    upCorner.resize(p.size(),Point<2,double>(0,0));
    lowCorner.resize(p.size(),Point<2,double>(0,0));
    
    double faux = sqrt(ratiolc.value*p.size());//solve: size=nLines*nCols; 
    	    	    	    	    	      //       nLines/nCols = ratiolc;
    int nLines(int(faux+0.5));//round nLines
    LOG_PTAG(Debug) << " faux =" << faux;
    if (nLines == 0)
    	nLines =1;
    LOG_PTAG(Debug)<< "nLines =" << nLines << ", nGraphs " << p.size() <<" ";
    
    double nCols(p.size()/double(nLines));
    
    int totalXsize(0);//total size of x dimension 
    for (unsigned i=0; i<p.size(); ++i)
    	totalXsize += max_size(p.at(i));
    	
    double idealXsize(double(totalXsize)/nLines);//ideal x size per line
    LOG_PTAG(Debug)<< "total nElem=" << totalXsize 
                   << ", ideal nElem/line=" << idealXsize;
		   
    double dist=factor.value*idealXsize;//distance between two graphs
    if (nCols>1)
    	dist /= (nCols-1);
    
    Log::print(Detail)<< ", dist=" << dist;
    
    idealXsize+=dist*(nCols-1);//add space between graphs
    Log::print(Detail)<< " corercted idealXsize" << idealXsize;
    
    vector<int> firstGraph(nLines+2);//first graph of each line (starts at 1)
    vector<int> nGraphs(nLines+1);   //number oo graphs per line (starts at 1)
    vector<int> height(nLines+1);  //height of each line (starts at 1)
    vector<double> lineSize(nLines+1); //size of each line (starts at 1)
    double totalHeight=0;
   
    int line(1); //line counter
    unsigned end = 0; //last graph in line
    unsigned start = 0; //first gragh in line
    double sumXsize = 0; //current cumulative x size of line 
    double sumLastLine = 0; // x size of last line
    double maxLineSize(0);
    while ((line <= nLines)&&(end<p.size())){ //proccess all graphs
    	double lastSum = sumXsize;//needed in case we decrease end
    	sumXsize += max_size(p.at(end));
	if (start!=end)//add distance between graphs
	    sumXsize += dist;

	double cummIdealXsize = line*idealXsize;	    
	LOG_PTAG(Detail) << "x size of graph " << end << "=" 
	    	    	 << p.at(end).max_nWindows() << " sum+d=" << sumXsize 
			 << " max+d=" << cummIdealXsize<<" ";
	Log::print(Detail)<< " diff=" << sumXsize-cummIdealXsize; 
	if ((sumXsize >= cummIdealXsize)||(end==p.size()-1)){
	    //ideal nr of elems in line is reached
	    LOG_PTAG(Debug)<<"Proccessing line " << line <<": " ;
	    
	    //number of graphs in line is chosen by minimizing the difference
	    //between ideal line size and actual line size 
	    if ( (cummIdealXsize-lastSum) < (sumXsize-cummIdealXsize) ){
	    	if(end!=0){
		    sumXsize = lastSum; //line ends at last graph 
		    --end;
		}
	    }
	    firstGraph.at(line)=start;
	    nGraphs.at(line)=end-start+1;
	    lineSize.at(line) = sumXsize-sumLastLine;
	    if (maxLineSize<lineSize.at(line))
	    	maxLineSize = lineSize.at(line);
	    LOG_PTAG(Debug)<< " lisnesize=" << lineSize.at(line)<< " graphs=" 
	    	    	    <<start <<" to " << end << " ("<< end-start+1<<") ";
	    LOG_PTAG(Debug)<<"Computing x origin of graphs in line " << line 
	    	    	    << flush; 
	    double xSize = 0;
	    for (unsigned i=start; i<=end; ++i){
		lowCorner.at(i).coord[x] = xSize;
		upCorner.at(i).coord[x] = xSize+max_size(p.at(i));
		xSize += max_size(p.at(i))+dist;
		int heig=max_heig(p.at(i));
		if (height.at(line)<heig)
		    height.at(line) = heig;
    	    }
	    totalHeight += height.at(line);
    	    Log::print(Detail)<<" height="<< height.at(line);
    	    Log::print(Detail)<<" total height="<< totalHeight;
    	    Log::print(Detail)<<" firstGraph="<< firstGraph.at(line);
	    ++line;
	    start=end+1;
	    sumLastLine = sumXsize;
	    Log::print(Debug)<< "... OK"<<flush;
	}
	++end;    
    }
    
    
    
    
    //compute y origin and adds space between graphs 
    firstGraph.at(nLines+1)=p.size();//ending marker
    double ydist= dist * (nLines);
    totalHeight+=(nLines-1)*ydist;
    
    double upper(totalHeight);
    double lower(totalHeight+ydist);
    for (int i=1; i<=nLines; ++i){
    	LOG_PTAG(Debug) << "line="<< i << " lower=" <<lower; 
    	Log::print(Debug)<<" dist="<<ydist<<" height=" <<height.at(i);
	lower -= (height.at(i)+dist*nLines);
	LOG_PTAG(Debug)<<"first=" <<firstGraph.at(i)<<" last=" 
	    	    	<<firstGraph.at(i+1)-1<<flush;
	double dx=(maxLineSize-lineSize.at(i))/double(nGraphs.at(i)+1);
	for (int j=firstGraph.at(i), j0=1; j<firstGraph.at(i+1); ++j, ++j0){
    	    double dy= ( (upper-lower) - max_heig(p.at(j)) );
	    lowCorner.at(j).coord[y]=(lower)/totalHeight; 
	    upCorner.at(j).coord[y]=(upper-dy)/totalHeight;
	    
	    lowCorner.at(j).coord[x]+=j0*dx;
	    lowCorner.at(j).coord[x]/=maxLineSize;
	    upCorner.at(j).coord[x]+=j0*dx;
	    upCorner.at(j).coord[x]/=maxLineSize;
		
	    LOG_PTAG(Debug) <<"lower("<< j << ")=" <<lowCorner.at(j);
    	}
	upper-= (height.at(i)+ydist);
    }    

    for (unsigned i=0; i<p.size(); ++i){
    	lowCorner.at(i).coord[x]*=0.9;
	lowCorner.at(i).coord[x]+=0.05;
    	lowCorner.at(i).coord[y]*=0.9;
	lowCorner.at(i).coord[y]+=0.05;
    	upCorner.at(i).coord[x]*=0.9;
	upCorner.at(i).coord[x]+=0.06;
    	upCorner.at(i).coord[y]*=0.9;
	upCorner.at(i).coord[y]+=0.06;
    }    

    LOG_PTAG(Debug)<<"line=" << line << "(" << nLines << ") end=" << end 
    	<< "(" << p.size() << ")"<<flush; 
    return maxLineSize;
}    		    

    
//****************************************************************************
REGISTER_MODULE(SigWinPlotGraceVL);
//end_of_file
