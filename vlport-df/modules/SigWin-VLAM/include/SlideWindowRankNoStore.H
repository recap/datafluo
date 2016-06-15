//****************************************************************************
/** \file SlideWindowRankNoStore.H 
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
 *  Created:  March 12, 2007.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SLIDEWINDOWRANKNOSTORE_
#define SLIDEWINDOWRANKNOSTORE_ 

#include "LogManager.H"
#include "Rank.H"
#include "Marker.H"
#include "SlideWindowNoStore.H"
#include <sstream>

//****************************************************************************
/** 	A class to compute and store sliding window rank properties of 
 *  a sequence of numbers.
*/ 
template<class T> class SlideWindowRankNoStore : public SlideWindowNoStore<T>{
  public:
    //constructors
    SlideWindowRankNoStore<T>(const long n, const long step=2, 
    	const string d="") : SlideWindowNoStore<T>(n,1,n,step,Freq,d),
	item(n,wMax()/2), marked(false),prevWsize(0){}

    SlideWindowRankNoStore<T>(const long n, const long min, const long max, 
    	const long step=2, const string d=""):
	SlideWindowNoStore<T>(n,min,max,step,Freq,d),
    	item(n,max/2), marked(false),prevWsize(0){}

    SlideWindowParameters definition(void) const {return this->def;}
    long N(void) const {return definition().size();}
    long wMin(void) const {return definition().wMin();}
    long wMax(void) const {return definition().wMax();}
    long wStep(void) const {return definition().wStep();}
    long nWSizes(void) const {return definition().nWSizes();}
    long nWindows(long wSize) const{return definition().nWindows(wSize);}
    long min_nWindows(void) const {return definition().min_nWindows();}
    long max_nWindows(void) const {return definition().max_nWindows();}
    DataType type(void) const {return definition().datatype();}
    string description(void) const {return definition().get_description();}
    
    //functions for setting parameters   
    void set_type(const DataType t) {definition.set_datatype(t);}
    void set_description(const string d) {definition.set_description(d);}

    //set/get current window size
    long current_window_size(void) const {return this->currWsize;}
    void set_current_window_size(const long WindowSize){
	prevWsize = this->currWsize;
    	SlideWindowNoStore<T>::set_current_window_size(WindowSize);
    }
   
    template<class U> 
    void write_medians(ostream& outStream, const Rank<U>& sequence); 

    template<class U> 
    void write_current_medians(ostream& outStream, const Rank<U>& sequence); 

    template<class U> 
    void compute_current_medians(const Rank<U>& sequence);

    void print_window(ostream& outFile=cout, const string &s="") const {
    	SlideWindowNoStore<T>::print_window(outFile,s);}
    
    //void write_window(ostream& outStream, const string &s="") const {
    //	SlideWindowNoStore<T>::write_window(outStream,s);}
 
  protected:    
    
    long current_storage_size(void) const {return N()-current_window_size()+1;}
 
    /// Replaces value[windowSize][*] by its cumulative tail.
    //void sum_tail(void);          


  private:
    Marker item; //>Marks the elements that are not in a subsequence.
    	    	 //>The elements are stored in ranked order. 
    bool marked; //> flag to indicate item is inicialized
    
    long prevWsize; //> previous window size
    long prev_window_size()const{return prevWsize;}

    //functions	
    template<class U> 
    void mark_and_compute_medians(const Rank<U>& sequence);
    template<class U> 
    void compute_medians_odd(const Rank<U>& seq);
    template<class U> 
    void compute_medians_even(const Rank<U>& seq);
    template<class U> 
    void compute_medians_aux(const Rank<U>& sequence);

    template<class U> 
    void mark_first(const Rank<U>& seq);
    
    /* not used
    template<class U> 
    void mark_previous_larger(const long WindowSize,  const Rank<U>& sequence);
    template<class U> 
    void mark_previous_smaller(const long WindowSize,  const Rank<U>& sequence);
    */
    
};

    
  
//****************************************************************************
template<class T> template<class U>
void SlideWindowRankNoStore<T>::write_medians(ostream& outStream, 
    const Rank<U>& sequence){
    for (long i=0; i<nWSizes(); i++){	
    	Log::print(Info)<<"."<<flush; 
	set_current_window_size(definition().window_size(i));
    	write_current_medians(outStream, sequence);
    }
}

template<class T> template<class U>
void SlideWindowRankNoStore<T>::write_current_medians(ostream& outStream, 
    const Rank<U>& sequence){

    compute_current_medians(sequence);
    LOG_PTAG(Debug)<<"windowSize="<< current_window_size() << " v[0]= " 
	               << this->get(0)  << " storageSize=" 
		       << current_storage_size() <<flush;

    this->write_window(outStream, "mm");
    LOG_PTAG(Detail)<<"success! v[" 
	               << current_storage_size()-1 << "]= " 
		       << get(current_storage_size()-1) <<flush;
}



/// Computes sliding window medians of a input sequence
template<class T> template<class U>
void SlideWindowRankNoStore<T>::compute_current_medians(
    const Rank<U>& sequence){
    
    if (current_window_size()==1){
    	for (long i=0; i<current_storage_size(); i++)
	    this->set(i, T(sequence.get(i)));
    	return;
    }
    if (!marked){
    	mark_and_compute_medians(sequence);
	return;
    }
    
    if (prev_window_size()>current_window_size()){
    	item.set_position(current_window_size()/2);	
    	mark_first(sequence);
    }
    else{
    	mark_first(sequence);
    	item.set_position(current_window_size()/2);	
    }
    compute_medians_aux(sequence);
}

template<class T> template<class U>
void SlideWindowRankNoStore<T>::mark_and_compute_medians(const Rank<U>& seq){
    item.set_position(current_window_size()/2);
    for (long i=0; i<current_window_size(); i++){
    	item.unmark(seq.rank_of(i));
    }
    for (long i=current_window_size(); i<N(); i++){
    	item.mark(seq.rank_of(i));
    }
    compute_medians_aux(seq);
    marked=true;
}

template<class T> template<class U>
void SlideWindowRankNoStore<T>::mark_first(const Rank<U>& sequence){
    const long diff = N()-prev_window_size();
    if (diff<=current_window_size()){
    	for (long i=0; i<diff; i++){
    	    item.unmark(sequence.rank_of(i));
	}
	for (long i=current_window_size(); i<N(); i++){
    	    item.mark(sequence.rank_of(i));
	}
    }
    else{
        for (long i=0; i<current_window_size(); i++){
       	    item.unmark(sequence.rank_of(i));
	}
	for (long i=diff; i<N(); i++){
    	    item.mark(sequence.rank_of(i));
	}
    }
}

//not used
/*
template<class T> template<class U>
void SlideWindowRankNoStore<T>::mark_previous_larger(const long wSize,
    const Rank<U>& sequence){
    long diff(N()-(wSize+wStep()));
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
}

template<class T> template<class U>
void SlideWindowRankNoStore<T>::mark_previous_smaller(const long wSize,
    const Rank<U>& sequence){
    long diff(N()-(wSize-wStep()));
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
}
*/

///Compute sliding windows median for window size <WindowSize>
//Assumes item is marked for first subsequence. 
template<class T> template<class U>
inline void SlideWindowRankNoStore<T>::compute_medians_aux(const Rank<U>& seq){
    if (current_window_size()%2==0)
    	compute_medians_even(seq);
    else
       	compute_medians_odd(seq);
}

//Assumes item is marked for first subsequence. 
template<class T> template<class U>
void SlideWindowRankNoStore<T>::compute_medians_odd(const Rank<U>& sequence){
    	
    this->set(0, T(sequence.get_ranked(item.position_index())));
    long first=0;
    long last=current_window_size();
    for (long i=1; i<current_storage_size(); ++i){
    	item.replace(sequence.rank_of(last),sequence.rank_of(first));
	this->set(i, T(sequence.get_ranked(item.position_index())));
	++first;
	++last;
    }
}

//Assumes item is marked for first subsequence. 
template<class T> template<class U>
void SlideWindowRankNoStore<T>::compute_medians_even(const Rank<U>& sequence){
    this->set(0, T(( sequence.get_ranked(item.position_index())
       	        +sequence.get_ranked(item.prev_position_index()))/2.0));

    long first=0;
    long last=current_window_size();
    for (long i=1; i<current_storage_size(); i++){
    	item.replace(sequence.rank_of(last),sequence.rank_of(first));
    	this->set(i, T((sequence.get_ranked(item.position_index())
                   +sequence.get_ranked(item.prev_position_index()))/2.0));
	++first;
	++last;
    }
}
 
#endif //SLIDEWINDOWRANKNOSTORE_
  
