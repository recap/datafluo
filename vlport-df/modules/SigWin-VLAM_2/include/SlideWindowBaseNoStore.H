//****************************************************************************
/** \file SlideWindowBase.H 
 *  \brief A base class to compute sliding window properties of a  
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
 *  Created:  December 07, 2005.(?)
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SLIDEWINDOWBASENOSTORE_
#define SLIDEWINDOWBASENOSTORE_

#include <iostream>
#include "DataIO.H"
#include "LogManager.H" 
#include "SlideWindowParameters.H"
#include "Interval.H"


/** \brief A class store sliding window properties of a sequence of numbers 
 *  Stores sliding windows values for one window size at a time.
 */ 
template<class T>
class SlideWindowBaseNoStore {
  public:
    //constructors
    SlideWindowBaseNoStore<T>(const long n=0, const long step=2, 
    	const DataType t=Freq, const string d="") : def(d, n, step, t),
	currWsize(0){}
     
    SlideWindowBaseNoStore<T>(const long n, const long min, const long max,
    	const long step=2, const DataType t=Freq, const string d="") : 
	def(d, n, min, max, step, t), currWsize(0){}

    SlideWindowBaseNoStore<T>(const long n, const Serie<long> w, 
    	const DataType t=Freq, const string d="") : def(d, n, w, t),
	currWsize(0){}

    SlideWindowBaseNoStore<T>(const SlideWindowParameters& par) : def(par),
	currWsize(0){}

    
    //constructors: create a sub slide window
    SlideWindowBaseNoStore<T>(const SlideWindowBaseNoStore& sw, 
    	const long& size, const string descr="", const bool keepPname=true) : 
	def((keepPname? sw.description()+":"+descr : descr), size, sw.wMin(), 
	    (sw.wMax()>size? size : sw.wMax()), sw.wStep(), sw.type()),
	currWsize(sw.currWsize){}
    
    SlideWindowBaseNoStore<T>(const SlideWindowBaseNoStore& sw, 
    	const Interval<long>& i, const bool keepPname=true) : 
	def((keepPname?sw.description()+":"+i.name() : i.name()), i.size(),
	sw.wMin(),(sw.wMax()>i.size()?i.size():sw.wMax()),sw.wStep(),sw.type()),
	currWsize(sw.currWsize){}
		   
    virtual ~SlideWindowBaseNoStore<T>(){}
    
    //functions for getting information about parameters
    SlideWindowParameters definition(void) const {return def;}
    long N(void) const {return def.size();}
    long wMin(void) const {return def.wMin();}
    long wMax(void) const {return def.wMax();}
    long wStep(void) const {return def.wStep();}
    long nWSizes(void) const {return def.nWSizes();}
    long nWindows(long wSize) const{return def.nWindows(wSize);}
    long min_nWindows(void) const {return def.min_nWindows();}
    long max_nWindows(void) const {return def.max_nWindows();}
    DataType type(void) const {return def.datatype();}
    string description(void) const {return def.get_description();}

    //functions for setting parameters   
    void set_type(const DataType t) {def.set_datatype(t);}
    void set_description(const string d) {def.set_description(d);}

    //set/get current window size
    virtual void set_current_window_size(const long WindowSize)=0;
    long current_window_size(void) const {return currWsize;}
    
    ///Gets the ith value from current window size (value[wSize][index])
    virtual T get(const long i) const = 0;
   
    ///Gets window content
    virtual vector<T> window_content(void) const = 0;

    /// Replaces value[currwindowSize][*] by its cumulative tail.
    virtual void sum_tail(void)=0;
    
    //IO functions
    //printing
    void print_parameters(ostream& outFile=cout) const;
    virtual void print_window(ostream& outFile=cout, const string &s="")const=0;
    void print(ostream& out=cout, const string &s="") const {
    	print_parameters(out);
	print_window(out,s);
    }    
    //writing
    void write_parameters(ostream& outStream) const;
    virtual void write_window(ostream& outstream, const string &s="") const=0 ;
    void write(ostream& out, const string &s="") const {
    	write_parameters(out); 
	write_window(out,s);
    }

    //reading
    virtual bool read_parameters(istream& inStream);
    //virtual bool read_window(istream& inStream) = 0;
    /*bool read(istream& in){ 
    	if (!read_parameters(in)) return false; 
	if (!read_window(in)) return false;
	return true;
    }
    */

  protected:
    virtual long current_storage_size() const = 0;
    
    long window_index(const long wSize) const {return def.window_index(wSize);}
    long window_index_test(const long w) const{return def.window_index_test(w);}
    
	
    long ceil_div(const long num, const long den) const{return (num+den-1)/den;}
	
  protected:
    SlideWindowParameters def;//definition of the SlideWindow structure.
    long currWsize; 


};




//*****************************************************************************
//SlideWindowBaseNoStore functions	
//*****************************************************************************
template<class T> 
void SlideWindowBaseNoStore<T>::print_parameters(ostream& outStream) const {
    LOG_PTAG(Debug) << "Printing parameters... " << flush; 
    outStream << def << endl;
    outStream.flush();
}


template<class T> 
void SlideWindowBaseNoStore<T>::write_parameters(ostream& outStream) const {
    LOG_PTAG(Debug) << "Writing parameters... " << flush; 
    def.write(outStream);
    outStream.flush();
    LOG_PTAG(Debug) << def  << flush; 
}


template<class T> 
bool SlideWindowBaseNoStore<T>::read_parameters(istream& inStream){ 
    if (!def.read(inStream)) return false;
    LOG_PTAG(Detail)<<"new value= " << def <<flush;
    return true;
}


#endif /*SLIDEWINDOWBASENOSTORE_*/


