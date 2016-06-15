//****************************************************************************
/** \file SigWinAux.H 
 *  \brief Routines to select and print signifincat windows.  
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  January 25, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: January 08, 2007.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#include <fstream>
#include <string>

#include "DataIO.H"
#include "LogManager.H"
#include "TimeLag.H"


///Prints first and last point for which data[first]=...=data[last]>=threshold
//with a given format. Output: maximun (or minimum) data value
template <class T>
double print_SigWin(ostream& outStream, const vector<T>& data, 
    const T threshold, const long& wSize, const int direction,
    void (*format)(ostream&, const unsigned, const unsigned, T& data));


///printing format for 2d plots
template <class T> 
void format_SigWin2d(ostream& outStream, const unsigned wSize, 
    const unsigned first, const unsigned last, const T& data){
    outStream << wSize << " " << first <<" "<< last <<" "<< endl;
}
    
///print_SigWin with format_SigWin2d
/*template <class T>
double print_SigWin2d(ostream& outStream, const vector<T>& data, 
    const T threshold, const long& wSize, const int direction=1){
    return print_SigWin(outStream, data, threshold, wSize, direction, 
    	&format_SigWin2d<T>);
}    
*/
///printing format for gnuplot 3d-colour map plots
template <class T> 
void format_SigWinGnuplot3d(ostream& outStream, const unsigned wSize, 
    const unsigned first, const unsigned last, const T& data){
    outStream <<first<<" "<<wSize<<" "<<data<<endl;
    if (first < last)   
        outStream << last << " " << wSize << " " << data << endl;
    outStream << endl;
}

///print_SigWin with format_SigWinGunuplor3d
template <class T>
double print_SigWinGnuplot3d(ostream& outStream, const vector<T>& data, 
    const T threshold, const long& wSize, const int direction=1){
    return print_SigWin(outStream, data, threshold, wSize, direction, 
    	&format_SigWinGnuplot3d<T>);
}

///Prints first and last point for which data[first]=...=data[last]>=threshold
//with a given format. Output: maximun (or minimum) data value
template <class T>
double print_SigWin(ostream& outStream, const vector<T>& data, 
    const T threshold, const long& wSize, const int direction,
    void (*format)(ostream&, const unsigned, const unsigned, const unsigned,
     	const T&)){
    double extreme= threshold;
    int begin = wSize/2+1;
        
    unsigned j(1), first(0);
    const T signedThresh(threshold*direction);
    T signedData(direction*data.at(0));
    if (signedData>signedThresh){//saves first point
	first = 0;
    	if (signedData > extreme) 
	    extreme = data.at(j);
    }
	   
    while (j < data.size()){
	if (data.at(j) == data.at(j-1)){
	    ++j;
	    continue;
	}
	if (direction*data.at(j-1) > signedThresh){ //prints
	    format(outStream, wSize, first+begin, j-1+begin, data.at(j-1));
	}
	signedData=direction*data.at(j);
	if (signedData >= signedThresh){//saves first point 
	    first = j;
	    if (signedData > extreme) 
	    	extreme = data.at(j);
	}
	j++;
    }
    if (direction*data.at(j-1) > signedThresh)//prints
    	format(outStream, wSize, first+begin, j-1+begin, data.at(j-1));    
    return extreme;
}    


inline void check_count(const long count, const long count2){ 
    if (count!=count2)
    	Log::print(Debug)<< "problem("<<count << "!=" << count2<<")" <<flush;
}

///Prints first and last point for which data[first]=...=data[last]>=threshold
//with a given format. Output: maximun (or minimum) data value
template <class T>
double print_SigWin2d(ostream& outStream, const vector<T>& data, 
    const T threshold, const long& wSize, const int direction){
    int begin = wSize/2+1;
    const T signedThresh(threshold*direction);
    unsigned j(0),first(0);
    bool sigwin(false);	   
    LOG_PTAG(Debug)<<"#t="<<threshold<<flush;
    
    int count=0;	
    while (j < data.size()){
	if (direction*data.at(j) > signedThresh){//window is significant 
	    ++count;
	    if (!sigwin){
	    	first = j;
		sigwin=true;
	    }
	}
    	else if (sigwin){//last window was significant
	    format_SigWin2d(outStream, wSize, first+begin, j-1+begin,
	     	    	    data.at(j-1));
	    sigwin=false;
	    check_count(j-1-first+1, count);
	    count=0;
	}
	++j;
    }    
    if (direction*data.back() > signedThresh){ 
	format_SigWin2d(outStream, wSize, first+begin, j-1+begin,data.back());
	check_count(j-1-first+1, count);
    }

    return 0;
}    

//-----------------------------------------------------------------------------
///Identifies significant windows in the input vector. 
//Returns a vector of pairs (first, last) indicating the intervals for which
//the indices first<=j<=last satisfies data[j]*direction > threshold*direction. 
//##### Important, indices start at 0 (not at window size / 2 +1)
template <class T>
vector<pair<long, long> > id_SigWins(const vector<T>& data, 
    const T threshold, const int direction){

    const T signedThresh(threshold*direction);
    vector<pair<long, long> > pairs;
    pair<long, long> interval;
    interval.first=0;
    
    bool sigwin(false);	   
    LOG_PTAG(Debug)<<"#t="<<threshold<<flush;
    
    int count=0;	
    for (unsigned j=0; j < data.size(); ++j){
	if (direction*data.at(j) > signedThresh){//window is significant 
	    ++count;
	    if (!sigwin){
	    	interval.first = j;
		sigwin=true;
	    }
	    if (j==data.size()-1){//print final window 
	    	interval.second=j;
	    	pairs.push_back(interval);
	    	check_count(j-interval.first+1, count);
	    }
	}
    	else if (sigwin){//last window was significant
 	    interval.second=j-1;
	    pairs.push_back(interval);
	    sigwin=false;
	    check_count(j-1-interval.first+1, count);
	    count=0;
     	    LOG_PTAG(Detail) << "SigWin: "<< interval.first <<"("
	    	<<data.at(interval.first) << ")-" 
	    	<<interval.second << "("<< data.at(interval.second)
		<<") t=" << threshold << flush;
	}
    }    
    return pairs;
}    

///Prints the numbers of the windows selected using id_SigWins using the
///SigWin2D format. 
double print_SigWin2d(ostream& outStream, 
    const vector<pair<long, long> >& sigwins, const long& wSize){
    
    int begin = wSize/2+1;
    
    if (sigwins.size()==0){
    	LOG_PTAG(Detail)<< "No significant windows at window size " << wSize 
	    	    	<< "." << flush;
        return 0;
    }
    for (unsigned j=0; j < sigwins.size(); ++j){
    	format_SigWin2d(outStream, wSize, sigwins.at(j).first+begin,
	    sigwins.at(j).second+begin, 1);
    }    
    return 1;
}    

///Computes the partial SigWin score for a given window size. 
//###important: scores start at 0  
vector<double> partial_SigWin_score(const vector<pair<long, long> >& sigwins,
    	const long& N, const long& wSize){
     
    LOG_PTAG(Debug)<< "Computing partial scores at window size " << wSize 
	    	    	<< "." << flush;
   
    vector<double> score(N,0.0);
       
    if (sigwins.size()==0){
    	LOG_PTAG(Detail)<< "No significant windows at window size " << wSize 
	    	    	<< "." << flush;
        return score;
    }
    int wsize_2=wSize/2;

    LOG_PTAG(Debug)<< "Interval:"<<sigwins.at(0).first << "-"<<
	    sigwins.at(0).second << "("<<wsize_2 <<")" << flush;
    for (unsigned j=0; j < sigwins.size(); ++j){
    	LOG_PTAG(Detail)<< "Interval:"<<sigwins.at(j).first << "-"<<
	    sigwins.at(j).second << "("<<wSize <<")"<< flush;
    	for(int ind=sigwins.at(j).first; ind <= sigwins.at(j).second; ++ind)
	    for(int k=ind; k<ind+wSize; ++k)
	    	score.at(k)+=1;
    }    
    LOG_PTAG(Debug)<< "Frequencies accumulated.";
    for (unsigned j=0; j < score.size(); ++j)
    	score.at(j)/=wSize;
    
    return score;
}    

//-----------------------------------------------------------------------------
///Selects significant windows. 
//Retuns a vector with the indices of the elements in the vector data that 
//are beyond the threshold
template <class T>
vector<long> select_SigWin(const vector<T>& data, const T threshold, 
    const int direction){
    const T signedThresh(threshold*direction);
    
    vector<long> sigwins;
    for (unsigned j=0; j<data.size(); ++j)
	if (direction*data.at(j) > signedThresh)
	    sigwins.push_back(j);

    return sigwins;
}    

///Prints the numbers of the windows selected using select_SigWin using the
///SigWin2D format. 
double print_SigWin2d(ostream& outStream, const vector<long>& sigwins, 
    const long& wSize){
    
    int begin = wSize/2+1;
    
    if (sigwins.size()==0){
    	LOG_PTAG(Detail)<< "No significant windows at window size " << wSize 
	    	    	<< "." << flush;
        return 1;
    }
    long first=sigwins.at(0);
    int count=0;	
    for (unsigned j=1; j < sigwins.size(); ++j){
	if (sigwins.at(j) == sigwins.at(j-1)+1){//consecutive sig windows 
	    ++count;
	}
    	else{
	    format_SigWin2d(outStream, wSize, first+begin, 
	    	sigwins.at(j-1)+begin, 1);
	    check_count(sigwins.at(j-1)-first, count);
	    count=0;
	    first=sigwins.at(j);
	}
    }    
    format_SigWin2d(outStream, wSize, first+begin, sigwins.back()+begin,1);
    check_count(sigwins.back()-first, count);
    return 0;
}    

///Computes the partial SigWin score for a given window size. 
vector<double> partial_SigWin_score(const vector<long>& sigwins, const long& N, 
    	const long& wSize){
    
    vector<double> score(N,0.0);
    
    if (sigwins.size()==0){
    	LOG_PTAG(Detail)<< "No significant windows at window size " << wSize 
	    	    	<< "." << flush;
        return score;
    }
    int wsize_2=wSize/2;
    for (unsigned j=0, index=wsize_2+1; j < sigwins.size(); ++j, ++index){
    	for(unsigned k=index-wsize_2; k<=index+wsize_2; ++k)
	    score.at(k)+=1;
    }    
    for (unsigned j=0; j < score.size(); ++j)
    	score.at(j)/=wSize;
    
    return score;
}    
