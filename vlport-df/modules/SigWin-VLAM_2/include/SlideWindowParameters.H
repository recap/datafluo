//****************************************************************************
/** \file SlideWindowParameters.H 
 *  \brief Sliding windows parameters 
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
 *  Feb 13, 2008: Function stream_sw2ascii updated.
 *  Jan 2008: Changed storage type from array to vector.
 *  Jan 2008: Modified to conform with new GNU C++.
 *  Created:  January 04, 2006.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SLIDEWINDOWPARAMETERS_
#define SLIDEWINDOWPARAMETERS_

#include <iostream>
#include "LogManager.H" 
#include "Serie.H"
#include "DataIO.H"
#include "VecIO.H"
#include <string>
#include <sstream>

//*****************************************************************************
template<class T> 
void stream_sw2ascii(istream& inStream, ostream& outFile, const int lineSize);   
//*****************************************************************************

enum DataType_{Freq, Prob, ProbSym, Tail, NormTail, NormTailSym};
typedef DataType_ DataType;


class SlideWindowParamBase {
  protected:
    long N; //> Original vector size.
    Serie<long> winDef;//> Windows definition.
    DataType type;//> \see enum DataType.
    
  public:  
    SlideWindowParamBase(const long n=0, const long step=1, 
    	const DataType t=Freq) : 
	N(n), winDef(1, step, (n!=0?(n-1)/step+1:0)), type(t) { 
	winDef.check_bounds_cresc(1,n);
    }
    
    SlideWindowParamBase(const long n, const long min, const long max,
    	const long step=1, const DataType t=Freq) : 	
	N(n), winDef(min, step, (max<min?0:(max-min)/step+1)), type(t) { 
	winDef.check_bounds_cresc(min,n);
    }
    
    SlideWindowParamBase(long n, Serie<long> s, DataType t) :
    	N(n), winDef(s), type(t) { winDef.check_bounds_cresc(1,n);
    }	 
    
    virtual ~SlideWindowParamBase(){}
    
    long size(void) const {return N;}
    Serie<long> scope(void) const {return winDef;}
    long wMin(void) const {return winDef.min();}
    long wMax(void) const {return winDef.max();}
    long wStep(void) const {return winDef.step();}
    long nWSizes(void) const {return winDef.number();}
    long nWindows(const long ws) const {return N-ws+1;}//ws=windowSize
    long window_size(const long i) const {return winDef.value(i);}
    long nWindows_from_index(const long i) const {return N-window_size(i)+1;}
    
    long window_index(const long ws) const {return winDef.index(ws);}
    long window_index_test(const long ws) const {return winDef.index_test(ws);}
    
    long first(const long ws) const {return ws/2+1;}//starts at 1
    long last(const long ws) const {return nWindows(ws)+first(ws)-1;}
    long first_wMin(void) const {return first(wMin());}//starts at 1
    long last_wMin(void) const {return last(wMin());}
    long first_wMax(void) const {return first(wMax());}//starts at 1
    long last_wMax(void) const {return last(wMax());}
    long min_nWindows(void) const {return nWindows(wMax());}
    long max_nWindows(void) const {return nWindows(wMin());}
    long range_nWindows(void) const {return max_nWindows()-min_nWindows()+1;}
    DataType datatype(void) const {return type;}

    void set_datatype(DataType t) {type=t;}
    
    virtual void write(ostream& out) const{
    	LOG_PPTAG(High)<< *this <<flush;
    	write_struct(out,N); 
	write_struct(out,winDef);
	write_struct(out,type);
    }
    
    virtual bool read(istream& in){
    	LOG_PPTAG(High)<< "Before" << *this << flush;
    	if (!read_struct(in,N))
	    return false; 
	if (!read_struct(in,winDef))
	    return false;
	if (!read_struct(in,type))
	    return false;
    	LOG_PPTAG(High)<< "After" << *this << flush;
	return true;
    }

       
    friend ostream &operator<<(ostream &s, const SlideWindowParamBase &a);
    friend bool operator!=(const SlideWindowParamBase &x, 
    	const SlideWindowParamBase &y);
    friend bool operator==(const SlideWindowParamBase &x, 
    	const SlideWindowParamBase &y);
};

ostream &operator<<(ostream &s, const SlideWindowParamBase &a){
    s << "(N=" << a.N << ", winDef=" << a.winDef << ", type= " << a.type<< " )";
    return s;
}
    
bool operator!=(const SlideWindowParamBase &x, const SlideWindowParamBase &y){
    if (x.N != y.N) return true;
    if (x.winDef != y.winDef) return true;
    if (x.type != y.type) return true;
    return false;
}

bool operator==(const SlideWindowParamBase &x, const SlideWindowParamBase &y){
    if (!(x!=y)) 
    	return true;
    else
    	return false;
}

class SlideWindowParameters : public SlideWindowParamBase{
  protected:
    string descr;//> Description.
    
  public:  
    SlideWindowParameters(const string d="", const long n=0, const long step=1,
    	const DataType t=Freq) : 
	SlideWindowParamBase(n, step, t), descr(d){}

    SlideWindowParameters(const string& d, const long n, const long min, 
    	const long max, const long step=1, const DataType t=Freq) : 	
	SlideWindowParamBase(n, min, max, step, t), descr(d) {
	    LOG_PTAG(Detail) << "descr= '" <<  d<< "'";
    	}
    
    SlideWindowParameters(const string& d, long n, Serie<long> s, DataType t) :
	SlideWindowParamBase(n, s, t), descr(d) {}

    SlideWindowParameters& operator=(const SlideWindowParameters& c){
    	N=c.N;
	winDef=c.winDef;
	type=c.type;
	descr=c.descr;
	return *this;
    }


    void set_description(const string& a) {descr=a;}
    string get_description(void) const {return descr;}
    string description(void) const {return descr;}

    void write(ostream& out) const {
    	LOG_PPTAG(High)<< *this <<flush;
    	SlideWindowParamBase::write(out);
    	write_struct(out,descr);	
    }
    
    bool read(istream& in){
    	LOG_PPTAG(Extreme)<< "Before" << *this <<flush;
     	if (!SlideWindowParamBase::read(in)) return false;
    	if (!read_struct(in,descr)) return false;
    	LOG_PPTAG(High)<< "After" << *this <<flush;
	return true;
    }
    friend ostream &operator<<(ostream &s, const SlideWindowParameters &a);
    friend bool operator!=(const SlideWindowParameters &x, 
    	const SlideWindowParameters &y);
    friend bool operator==(const SlideWindowParameters &x, 
    	const SlideWindowParameters &y);
    
};


ostream &operator<<(ostream &s, const SlideWindowParameters &a){
    s << "(N=" << a.N << ", winDef=" << a.winDef << ", type= " << a.type
    	<< ", descr= " << a.descr << " )";
    return s;
}
    
bool operator!=(const SlideWindowParameters &x, const SlideWindowParameters &y){
    if (x.N != y.N) return true;
    if (x.winDef != y.winDef) return true;
    if (x.type != y.type) return true;
    if (x.descr != y.descr) return true;
    return false;
}

bool operator==(const SlideWindowParameters &x, const SlideWindowParameters &y){
    if (!(x!=y)) 
    	return true;
    else
    	return false;
}


//*****************************************************************************
template<class T> 
void stream_sw2ascii(istream& inStream, ostream& outFile, const int lineSize){   
    SlideWindowParameters p;
    
    LOG_PTAG(Debug) << "Reading parameters... " << flush; 
    read_struct(inStream, p);

    LOG_PTAG(Debug) << "Writing parameters... " << flush; 
    outFile << p << endl << flush;
    
    LOG_PTAG(Debug) << "Streaming data. " << flush; 
    for (long i=p.wMin(); i<p.wMax(); i+=p.wStep()){
    	std::ostringstream saux;
    	saux << "window size " << i;
    	stream_vector2ascii<T>(inStream, outFile, saux, lineSize);
    }
}

#endif //SLIDEWINDOWPARAMETERS
