//****************************************************************************
/** \file SlideWindowRank.H 
 *  \brief A class to compute and store sliding window rank properties
 *         of a sequence of numbers 
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  Jan 31, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Jan 2008: Changed storage type from array to vector.
 *  Jan 2008: Modified to conform with new GNU C++.
 *  Created:  August 10, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SLIDEWINDOWRANK_
#define SLIDEWINDOWRANK_ 

#include "LogManager.H"
#include "Rank.H"
#include "Marker.H"
#include "SlideWindow.H"

//****************************************************************************
/** 	A class to compute and store sliding window rank properties of 
 *  a sequence of numbers.
*/ 
template<class T> class SlideWindowRank : public SlideWindow<T>{
  private:
    Marker item; //>Marks the elements that are not in a subsequence.
    	    	 //>The elements are stored in ranked order. 
    
  public:
    //constructors
    SlideWindowRank<T>(const long n, const long step=2, const string d="") :
     	SlideWindow<T>(n,1,n,step,Freq,d), item(n,wMax()/2){}

    SlideWindowRank<T>(const long n, const long min, const long max, 
    	const long step=2, const string d=""):
	SlideWindow<T>(n,min,max,step,Freq,d),
    	item(n,wMax()/2){}

    template<class U> void compute_medians(const Rank<U>& sequence);
    
    SlideWindowParameters definition(void) const {return this->def;}
    long N(void) const {return definition().size();}
    Serie<long> winDef(void) const {return definition().scope();}
    long wMin(void) const {return definition().wMin();}
    long wMax(void) const {return definition().wMax();}
    long wStep(void) const {return definition().wStep();}
    long nWSizes(void) const {return definition().nWSizes();}
    long nWindows(long wSize) const{return definition().nWindows(wSize);}
    long min_nWindows(void) const {return definition().min_nWindows();}
    long max_nWindows(void) const {return definition().max_nWindows();}
    DataType type(void) const {return definition().datatype();}
    string description(void) const {return definition().get_description();}

  protected:
    long storage_size(long windowValue) const {return N()-windowValue+1;}

  private:      /// Replaces value[windowSize][*] by its cumulative tail.
    long window_size(const long index) const {return winDef().value(index);}
    long window_index(const long wSize)const{return winDef().index(wSize);}
    long window_index_test(const long wSize) const{
    	return winDef().index_test(wSize);
    }

    //functions	
    template<class U> 
    void compute_medians_odd(const long WindowSize, const Rank<U>& seq);
    template<class U> 
    void compute_medians_even(const long WindowSize, const Rank<U>& seq);
    template<class U> 
    void compute_medians(const long WindowSize,  const Rank<U>& sequence);
};

    
  
//****************************************************************************
/// Computes sliding window medians of a input sequence
template<class T> template<class U>
void SlideWindowRank<T>::compute_medians(const Rank<U>& sequence){
    
    LOG_PTAG(High) << "wSize="<< wMax() <<flush;
    if (wMax()!=1){
    	item.set_position(wMax()/2);
    	for (long i=0; i<wMax(); i++){
    	    item.unmark(sequence.rank_of(i));
    	}
    	for (long i=wMax(); i<N(); i++){
    	    item.mark(sequence.rank_of(i));
    	}

    	compute_medians(wMax(), sequence);
    }
    else{
    	const long wIndex = window_index(wMax());    
    	for (long i=0; i<storage_size(wMax()); ++i){
	    this->set_raw(wIndex,i,sequence.get(i));
	}
    	LOG_PTAG(Debug)<<this->get_raw(wIndex,0);
	return;
    } 
    
    for (long wSize=wMax()-wStep(); wSize>=wMin(); wSize-=wStep()){
    	LOG_PTAG(High) << "wSize="<< wSize <<flush;
    	if (wSize!=1){
    	    long diff(N()-(wSize+wStep()));
	    //sets position before decreasing window size
    	    item.set_position(wSize/2);	
	    if (diff<=wSize){
    	    	for (long i=0; i<diff; i++){
    	    	    item.unmark(sequence.rank_of(i));
	    	}
	    	for (long i=wSize; i<N(); i++){
    	    	    item.mark(sequence.rank_of(i));
	    	}		
    	    }
	    else{
    	    	for (long i=0; i<wSize; i++){
    	    	    item.unmark(sequence.rank_of(i));
	    	}
	    	for (long i=diff; i<N(); i++){
    	    	    item.mark(sequence.rank_of(i));
	    	}
    	    }
	    compute_medians(wSize, sequence);
	}
	else{
    	    const long wIndex = window_index(wSize);    
    	    for (long i=0; i<storage_size(wSize); ++i){
	    	this->set_raw(wIndex,i,sequence.get(i));
	    }
    	    LOG_PTAG(Debug)<<this->get_raw(wIndex,0);
	}	
    }	
}

///Compute sliding windows median for window size <WindowSize>
template<class T> template<class U>
inline void SlideWindowRank<T>::compute_medians(const long WindowSize,
    const Rank<U>& sequence){
    if (WindowSize%2==0)
    	compute_medians_even(WindowSize, sequence);
    else
       	compute_medians_odd(WindowSize, sequence);
}

template<class T> template<class U>
void SlideWindowRank<T>::compute_medians_odd(const long WindowSize,
    const Rank<U>& sequence){
    //Assumes item is marked for first subsequence. 
    const long wIndex = window_index(WindowSize);    
    this->set_raw(wIndex,0, sequence.get_ranked(item.position_index()));
    long first=0;
    long last=WindowSize;
    for (long i=1; i<storage_size(WindowSize); ++i){
    	item.replace(sequence.rank_of(last),sequence.rank_of(first));
    	//item.mark(sequence.rank_of(first));
	//item.unmark(sequence.rank_of(last));
	this->set_raw(wIndex,i, sequence.get_ranked(item.position_index()));
	++first;
	++last;
    }
}

template<class T> template<class U>
void SlideWindowRank<T>::compute_medians_even(const long WindowSize,
    const Rank<U>& sequence){
    //Assumes item is marked for first subsequence. 
    const long wIndex = window_index(WindowSize);
    this->set_raw(wIndex, 0, T((sequence.get_ranked(item.position_index())
       	               +sequence.get_ranked(item.prev_position_index()))/2.0));

    long first=0;
    long last=WindowSize;
    for (long i=1; i<storage_size(WindowSize); i++){
    	item.replace(sequence.rank_of(last),sequence.rank_of(first));
    	//item.mark(sequence.rank_of(first));
	//item.unmark(sequence.rank_of(last));
    	this->set_raw(wIndex,i, T((sequence.get_ranked(item.position_index())
                        +sequence.get_ranked(item.prev_position_index()))/2.0));
	++first;
	++last;
    }
}
 
#endif //SLIDEWINDOWRANK_
  
