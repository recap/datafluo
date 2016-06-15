//****************************************************************************
/** \file SlideWindowNoStore.H 
 *  \brief A class to compute sliding window properties of asequence of numbers 
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
 *  Created:  February 28, 2007.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SLIDEWINDOWNOSTORE_
#define SLIDEWINDOWNOSTORE_

#include <iostream>
#include "VecIO.H"
#include "SlideWindowBaseNoStore.H"


template<class T> class SlideWindowNoStore;

template<class T> bool diff(const SlideWindowNoStore<T>& a, 
    const SlideWindowNoStore<T>& b);

/** \brief A class store sliding window properties of a sequence of numbers 
 */ 
template<class T>
class SlideWindowNoStore : public SlideWindowBaseNoStore<T> {
  public:
    SlideWindowNoStore<T>(const long n=0, const long step=1, 
    	const DataType t=Freq, const string d="") : 
	SlideWindowBaseNoStore<T>(n, step, t, d){v.reserve(max_nWindows());}
    SlideWindowNoStore<T>(const long n, const long min, const long max,
    	const long step=1, const DataType t=Freq, const string d="") :
    	SlideWindowBaseNoStore<T>(n, min, max, step, t, d){
	v.reserve(max_nWindows());
    }
    SlideWindowNoStore<T>(const long n, const Serie<long> w, 
    	const DataType t=Freq, const string d="") : 
	SlideWindowBaseNoStore<T>(n, w, t, d){v.reserve(max_nWindows());}
    SlideWindowNoStore<T>(const SlideWindowParameters p) : 
    	SlideWindowBaseNoStore<T>(p){v.reserve(max_nWindows());}
    

    //virtual ~SlideWindowNoStore<T>(){this->clear();}
    

    //functions for getting information about parameters
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
    	this->window_index_test(WindowSize);
	this->currWsize=WindowSize;
	v.resize(current_storage_size());
    }


    ///Gets the ith value from current window size (value[wSize][index])
    T get(const long index) const {return v.at(index);}
    
    ///Returns window content
    vector<T> window_content(void) const {return v;}
    
    /// Replaces value[*][*] by its cumulative tail.
    void sum_tail(void);
    
    
    void print_window(ostream& outFile=cout, const string& s="") const ;
    void write_window(ostream& outStream, const string& s="") const ;
    
    friend bool diff<T>(const SlideWindowNoStore<T>& a, 
    	                const SlideWindowNoStore<T>& b);

  protected:    
    void set(const long index, const T val){v.at(index) = val;}
    
    long current_storage_size(void) const {return N()-current_window_size()+1;}

    long current_window_test(const long wSize) const;
 
  private:
    vector<T> v;//data vector   
           
};

//*****************************************************************************
//SlideWindowNoStore functions
template<class T> 
inline long SlideWindowNoStore<T>::current_window_test(const long wSize) const{
    if (current_window_size()!=wSize){
	LOG_PTAG(Fatal) << "Wrong window size\n";
	exit(1);
    }
    return wSize;
}


/// Replaces value[windowSize][*] by its cumulative tail.
template<class T> void SlideWindowNoStore<T>::sum_tail(void){
    for (long i=v.size()-2; i>=0; --i)
    	v.at(i) += v.at(i+1);
} 


//*****************************************************************************
template<class T> 
void SlideWindowNoStore<T>::print_window(ostream& outFile, const string& s)
const{
    std::ostringstream saux;
    saux << description() << ":" <<s << current_window_size();
    print_vector(outFile, v, saux.str());
}

template<class T> 
void SlideWindowNoStore<T>::write_window(ostream& outFile, const string& s)
const{
    std::ostringstream saux;
    saux << description() << ":" << s << current_window_size();
    write_vector(outFile, v, saux.str());
}

    
//*****************************************************************************
template<class T> 
bool diff(const SlideWindowNoStore<T>& a, const SlideWindowNoStore<T>& b){
    if (a.definition() != b.definition()) return true; 
    if (a.current_window_size() != b.current_window_size()) return true; 
    
    for (long j=0; j<a.current_storage_size(); j++){
	if (a.v.at(j) != b.v.at(j)){
    	    cout << "value(" << a.current_window_size() << ")[" << j << "] " 
	        << a.v.at(j) << " " << b.v.at(j) << endl;
    	    	return true;
    	}
    } 
    return false;
}   

#endif /*SLIDEWINDOWNOSTORE_*/
