//****************************************************************************
/** \file SlideWindowBase.H 
 *  \brief A base class to compute and store sliding window properties of a  
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
 *  Jan 2008: Modified to conform with new GNU C++.
 *  Created:  December 07, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/

#ifndef SLIDEWINDOWBASE_
#define SLIDEWINDOWBASE_

#include <iostream>
#include <string>
#include "DataIO.H"
//#include "ArrayIO.H"
#include "VecIO.H"
#include "LogManager.H" 
#include "SlideWindowParameters.H"
#include "Interval.H"


/** \brief A class store sliding window properties of a sequence of numbers 
 *  Stores sliding windows values of a given property in a triangular structure.
 */ 
template<class T>
class SlideWindowBase {
  public:
    SlideWindowBase<T>(const long n=0, const long step=1, 
    	const DataType t=Freq, const string d="") : def(d, n, step, t){}
     
    SlideWindowBase<T>(const long n, const long min, const long max,
    	const long step=1, const DataType t=Freq, const string d="") : 
	def(d, n, min, max, step, t){}

    SlideWindowBase<T>(const long n, const Serie<long> w, 
    	const DataType t=Freq, const string d="") : def(d, n, w, t){}

    SlideWindowBase<T>(const SlideWindowParameters& par) : def(par){}

    
    //create a sub slide window
    SlideWindowBase<T>(const SlideWindowBase& sw, const long& size, 
    	const string descr="", const bool keepPname=true) : 
	def((keepPname? sw.description()+":"+descr : descr), size, sw.wMin(), 
	    (sw.wMax()>size? size : sw.wMax()), sw.wStep(), sw.type()){}
    
    SlideWindowBase<T>(const SlideWindowBase& sw, const Interval<long>& i, 
    	const bool keepPname=true) : 
	def((keepPname?sw.description()+":"+i.name() : i.name()), i.size(),
	    sw.wMin(), 
	    (sw.wMax()>i.size()?i.size():sw.wMax()), sw.wStep(), sw.type()){}
		   
    virtual ~SlideWindowBase<T>(){}
    
    long N(void) const {return def.N;}
    Serie<long> winDef(void) const {return def.scope();}
    long wMin(void) const {return def.wMin();}
    long wMax(void) const {return def.wMax();}
    long wStep(void) const {return def.wStep();}
    long nWSizes(void) const {return def.nWSizes();}
    long nWindows(long wSize) const{return def.nWindows(wSize);}
    long min_nWindows(void) const {return def.min_nWindows();}
    long max_nWindows(void) const {return def.max_nWindows();}
    DataType type(void) const {return def.datatype();}

    string description(void) const {return def.get_description();}
    SlideWindowParameters definition(void) const {return def;}
    
    void set_type(const DataType t) {def.set_datatype(t);}
    void set_description(const string &d) {def.set_description(d);}

    ///Gets value[wSize][index]
    virtual T get(const long wSize, const long index) const = 0;
    
    ///Returns window content
    virtual vector<T> window_content(const long WindowSize) const = 0;
    
    
    void print_parameters(ostream& outFile=cout) const;
    virtual void print_window(const long WindowSize, ostream& outFile=cout, 
    	const string &s="") const=0 ;
    void print_data(ostream& outFile=cout, const string &s="") const;
    void print(ostream& out=cout, const string &s="") const {
    	print_parameters(out); 
	out<<endl;
	print_data(out, s);
    }    
    
    virtual void write_parameters(ostream& outStream) const;
    void write_data(ostream& outStream, const string &s="") const;
    virtual void write_window(const long WindowSize, ostream& outStream, 
    	const string &s="") const=0 ;
    void write(ostream& out, const string &s="") const {
    	write_parameters(out); 
	write_data(out, s);
    }
    
    virtual bool read_parameters(istream& inStream);
    /*
    bool read_data(istream& inStream);
    virtual bool read_window(const long WindowSize, istream& inStream) = 0 ;
    bool read(istream& in){ 
    	if (!read_parameters(in)) return false; 
	if (!read_data(in)) return false;
	return true;
    }
    */
    
  protected:
    virtual void alloc(void)=0;
    virtual void realloc(const SlideWindowParameters& newDef)=0;
    
    virtual long storage_size(long windowValue) const = 0;

    /// Replaces value[*][*] by its cumulative tail.
    void sum_tail(void);
    long window_size(const long i) const {return winDef().value(i);}
    long window_index(const long value) const {return winDef().index(value);}
    long window_index_test(const long v) const{return winDef().index_test(v);}
	
    long ceil_div(const long num, const long den) const{return (num+den-1)/den;}
	
  protected:
    SlideWindowParameters def;

    /// Replaces value[windowSize][*] by its cumulative tail.
    virtual void sum_tail(const long windowSize)=0;

};




//*****************************************************************************
//SlideWindowBase functions	
/// Replaces value[*][*] by its cumulative tail.
template<class T> void SlideWindowBase<T>::sum_tail(void){
    for (long window=wMin(); window<=wMax(); window+=wStep()) 
       sum_tail(window);
}

template<class T> 
void SlideWindowBase<T>::print_parameters(ostream& outStream) const {
    LOG_PTAG(Debug) << "Printing parameters... " << flush; 
    outStream << def;
    outStream.flush();
}

template<class T> 
void SlideWindowBase<T>::print_data(ostream& outFile, const string &s) const{
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep())
    	print_window(windowSize, outFile, s);
}

template<class T> 
void SlideWindowBase<T>::write_parameters(ostream& outStream) const {
    LOG_PTAG(Debug) << "Writing parameters... " << flush; 
    def.write(outStream);
    outStream.flush();
    LOG_PTAG(Debug) << def  << flush; 
}



template<class T> 
void SlideWindowBase<T>::write_data(ostream& outStream, const string &s) const {
    LOG_PTAG(High)<<"nWSizes="<< nWSizes() <<flush; 
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
     	Log::print(Info)<<"."<<flush; 
	write_window(windowSize, outStream, s);
    }
}


template<class T> 
bool SlideWindowBase<T>::read_parameters(istream& inStream){ 
    SlideWindowParameters newDef;
    if (!newDef.read(inStream)) return false;
    LOG_PTAG(High)<<"value read= " << newDef <<flush;
    if (newDef!=def)
    	realloc(newDef);
    LOG_PTAG(Detail)<<"new value= " << def <<flush;
    return true;
}


#endif /*SLIDEWINDOWBASE_*/


