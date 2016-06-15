//****************************************************************************
/** \file SlideWindow.H 
 *  \brief A class to compute and store sliding window properties of a  
 *         sequence of numbers 
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
 *  Created:  July 25, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/

#ifndef SLIDEWINDOW_
#define SLIDEWINDOW_

#include <iostream>
#include "SlideWindowBase.H"
#include "VecIO.H"


template<class T> class SlideWindow;

template<class T> bool diff(const SlideWindow<T>& a, const SlideWindow<T>& b);

/** \brief A class store sliding window properties of a sequence of numbers 
 *  Stores sliding windows values of a given property in a triangular structure.
 */ 
template<class T>
class SlideWindow : public SlideWindowBase<T> {
  public:
    SlideWindow<T>(const long n=0, const long step=1, const DataType t=Freq,
    	const string d="") : SlideWindowBase<T>(n, step, t, d) { alloc();}
    SlideWindow<T>(const long n, const long min, const long max,
    	const long step=1, const DataType t=Freq, const string d="") :
    	SlideWindowBase<T>(n, min, max, step, t, d) { alloc();}
    SlideWindow<T>(const long n, const Serie<long> w, const DataType t=Freq, 	
    	const string d="") : SlideWindowBase<T>(n, w, t, d) { alloc();}
    SlideWindow<T>(const SlideWindowParameters p):SlideWindowBase<T>(p)
     	{alloc();}
    
    //create a sub slide window
    SlideWindow<T>(const SlideWindow& sw, const long& size, const long& begin=0,
    	const string& d="", const bool keepPname=true): 
	SlideWindowBase<T>(sw, size, d, keepPname) {init_sub(sw, size, begin);}
    
    SlideWindow<T>(const SlideWindow& sw, const Interval<long>& i, 
    	const bool keepPname=true): SlideWindowBase<T>(sw, i, keepPname) { 
	init_sub(sw, i.size(), i.begin());
	LOG_PTAG(Detail)<< "Copy OK"<<flush;
    }
	
    virtual ~SlideWindow<T>(){}
    
    //functions for getting information about parameters
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

    //functions for setting parameters   
    void set_type(const DataType t) {definition().set_datatype(t);}
    void set_description(const string &d) {definition().set_description(d);}

    ///Gets value[wSize][index]
    T get(const long wSize, const long index) const {
    	return v.at(window_index_test(wSize)).at(index);
    }
    T get_raw(const long wIndex, const long index) const {
    	return v.at(wIndex).at(index);
    }
    
    ///Returns window content
    vector<T> window_content(const long WindowSize) const;
    
    /// Replaces value[*][*] by its cumulative tail.
    void sum_tail(void);
    
    template<class U> void compute_medians(const vector<U>& vec);

    void print_window(const long wSize, ostream& outS, const string &s="")const;
    void write_window(const long wSize, ostream& outF, const string &s="")const;
    bool read_window(const long windowSize, istream& inStream);
    bool read_data(istream& inStream);

    friend bool diff<T>(const SlideWindow<T>& a, const SlideWindow<T>& b);

  protected:
    long storage_size(long windowValue) const {return N()-windowValue+1;}
    
    void alloc(void);
    void realloc(const SlideWindowParameters &newDef);

    void set(const long windowSize, const long index, const T& val){
    	v.at(window_index_test(windowSize)).at(index) = val;
    }
    void set_raw(const long windowIndex, const long index, const T& val){
    	v.at(windowIndex).at(index) = val;
    }

  private:
    vector<vector<T> > v;  //> structure where property values are stored.

    /// Replaces value[windowSize][*] by its cumulative tail.
    void sum_tail(const long windowSize);

    long window_size(const long index) const {return winDef().value(index);}
    long window_index(const long wSize)const{return winDef().index(wSize);}
    long window_index_test(const long wSize) const{
    	return winDef().index_test(wSize);
    }
    

    void init_sub(const SlideWindow& sw, const long& size, const long& begin){ 
	if ((begin<0)||(size<0)||(begin+size>sw.N())){
	    LOG_PTAG(Fatal) << "Wrong limits creating sub SlideWindow (b=" 
	    	<< begin << ", s=" << size << ")\n";
	    exit(1);
	} 
	alloc();
    	for (long i=0; i<nWSizes(); i++){
	    for (unsigned j=0, j0=begin; j<v.at(i).size(); ++j, ++j0){
	    	v.at(i).at(j) = sw.v.at(i).at(j0);
	    }
	}
    }

};




//*****************************************************************************
//SlideWindow functions
template<class T> 
void SlideWindow<T>::alloc(void){
    v.resize(nWSizes());
    for (long i=0; i<nWSizes(); i++){
    	v.at(i).resize(storage_size(window_size(i)));
    }
}

template<class T> 
void SlideWindow<T>::realloc(const SlideWindowParameters& newDef){
    this->def = newDef;
    LOG_PTAG(High) << "New parameters=" << definition() << flush;
    alloc();	
}


/// Replaces value[windowSize][*] by its cumulative tail.
template<class T> void SlideWindow<T>::sum_tail(const long windowSize){
    long wIndex=window_index_test(windowSize);
    for (long i=v.at(wIndex).size()-2; i>=0; --i)
    	v.at(wIndex).at(i) += v.at(wIndex).at(i+1);
} 

/// Replaces value[*][*] by its cumulative tail.
template<class T> void SlideWindow<T>::sum_tail(void){
    SlideWindowBase<T>::sum_tail();
    set_type(type()==Prob? NormTail : Tail);
}


/// This computes the median in a ineficient way. Used for testing.
template<class T> template<class U> 
void SlideWindow<T>::compute_medians(const vector<U>& vec){
    for (long wSize=wMax(); wSize>=wMin(); wSize-=wStep()){
    	LOG_PTAG(Debug) << "Window size= " << wSize << endl;
	long wIndex = window_index(wSize);
	for (long window=0; window<storage_size(wSize); window++){ 
 	    vector<U> aux(vec.begin()+window, vec.begin()+window+wSize);
    	    sort(aux.begin(), aux.end());
	    
	    if (wSize%2==1)
	    	v.at(wIndex).at(window) = aux.at(wSize/2);
	    else
	    	v.at(wIndex).at(window)= (aux.at(wSize/2)
		                       +aux.at(wSize/2-1))/2.0;
	}
    }
}

template<class T> 
vector<T> SlideWindow<T>::window_content(const long WindowSize) const {
    return v.at(window_index_test(WindowSize));
}



//*****************************************************************************
template<class T> 
void SlideWindow<T>::print_window(const long windowSize, ostream& outFile, 
    const string& s) const {
    std::ostringstream saux;
    saux << description() << ":" <<s << windowSize;
    print_vector(outFile, window_content(windowSize), saux.str());
}

template<class T> 
void SlideWindow<T>::write_window(const long windowSize, ostream& outStream, 
    const string& s) const {
    std::ostringstream saux;
    saux << description() << ":" <<s << windowSize;
    write_vector(outStream, window_content(windowSize), saux.str());
}

template<class T> 
bool SlideWindow<T>::read_window(const long windowSize, istream& inStream){
    long wIndex=window_index_test(windowSize);
    v.at(wIndex).clear();
    string saux="";
    if (!read_vector(inStream, v.at(wIndex), saux)) return false;
    if (!saux.find(description())){
    	LOG_PTAG(Warning)<<"Descriptions '"<<saux<<"' and '"<< description() 
	    << ") do not match!";
    }
    if (long(v.at(wIndex).size()) != storage_size(windowSize)){
    	LOG_PTAG(Fatal) << "Input vector size ("<<v.at(wIndex).size()
	    << ") differs from expected ("<<storage_size(windowSize)<<")";
	exit(1);
    }
    return true;
}


template<class T> 
bool SlideWindow<T>::read_data(istream& inStream){
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
	if (!read_window(windowSize, inStream)) return false;
    }
    return true;
}
   
//*****************************************************************************
template<class T> 
bool diff(const SlideWindow<T>& a, const SlideWindow<T>& b){
    if (a.definition() != b.definition()){
     	return true;
    } 
    for (long i=a.wMin(); i<=a.wMax(); i+=a.wStep()){
    	long wIndex=a.window_index_test(i);
	for (long j=0; j<a.storage_size(i); j++){
	     if (a.v.at(wIndex).at(j) != b.v.at(wIndex)(j)){
    	    	cout << "value[" << wIndex << "][" << j << "] " 
	     	    << a.v.at(wIndex)(j) << " " << b.v.at(wIndex)(j) << endl;
    	    	return true;
	    }
    	}
    } 
    return false;
}   


#endif /*SLIDEWINDOW_*/


