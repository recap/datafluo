//****************************************************************************
/** \file Interval.H 
 *  \brief A class to store partition boundaries.
 *  \warning  Only works for integer partitions.
 *
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since September 21, 2007.
 */
/*---------------------------------------------------------------------------
 *  History:
 *  Sep 21, 2007: changed internal structure to store the end of the interval.
 *  	    	  constructors, write and read had also to be chabged.
 *  Created: May 15, 2006
 *---------------------------------------------------------------------------
 * Author: Marcia A. Inda
 *  	   Integrtive Bioinformatics Unit, UvA
 *         http://staff.science.uva.nl/~indahttp://staff.science.uva.nl/~inda
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef INTERVALH_
#define INTERVALH_

#include <iostream>
#include "LogManager.H"
#include "DataIO.H"


//*****************************************************************************
// * Struct interval	                                                     
//***************************************************************************** 
template <class T> 
class Interval;

template<class T>
T get_intervals(const vector<string>& names, vector<Interval<T> >& interval);

template<class T> inline ostream &operator<<(ostream &s, const Interval<T> &a);
template<class T> 
inline bool operator==(const Interval<T> &x,const Interval<T> &y);

template <class T> 
class Interval {
  public:
    //contructors
    Interval(){}
    Interval(string& name_, T begin_, T end_): v(name_), b(begin_), e(end_){}
    
    T begin(void) const {return b;}
    T end(void) const {return e;}
    T size(void) const {return e-b+1;}
    string name(void) const {return v;}
    bool write(ostream& outStream);
    bool read(istream& inStream);
        
    friend ostream &operator<< <T> (ostream &s, const Interval<T> &a);
    friend bool operator== <T> (const Interval<T> &x, const Interval<T> &y);
    friend T get_intervals<T>(const vector<string>& names, 
    	vector<Interval<T> >& i);
	
    void set_name(const string& s){v=s;}
      
  private:
    T b;//begin 
    T e;//end 
    string v;
};

template<class T>
bool Interval<T>::write(ostream& outStream){
    LOG_PTAG(Detail) << "Writing Interval " << *this <<flush; 
    write_struct(outStream, v);
    write_struct(outStream, b);
    write_struct(outStream, e);
    return true;
}

template <class T>
bool Interval<T>::read(istream& inStream){
    LOG_PTAG(Detail) << "Reading Interval "<<flush; 
    read_struct(inStream, v);
    read_struct(inStream, b);
    read_struct(inStream, e);
    LOG_PTAG(High)<< *this <<flush;
    return true;
}


template<class T> inline ostream &operator<<(ostream &s, const Interval<T> &a){
    s << a.v << ":[" << a.b << ", " << a.e << "]";
    return s;
}

template<class T> 
inline bool operator==(const Interval<T> &x,const Interval<T> &y){
    if (x.v != y.v) return false;
    if (x.b != y.b) return false;
    if (x.n != y.n) return false;
    return true;
}


///gets the labeled intervals from \c names and stores them in \c interval.
template<class T>
T get_intervals(const vector<string>& names, vector<Interval<T> >& interval){
    string oldValue(names.at(0));
    T begin(0);
    T largest(0);
    long count(0);
    interval.clear();
    long size = names.size();
    LOG_PTAG(Detail) << "Names size=" << size <<flush;
    for(long i=1; i<size; ++i){
	LOG_PTAG(High)<< "value(" <<i << ")= "<< names.at(i) <<flush ;
    	string newValue=names.at(i);

	if ((newValue != oldValue)||(i==size-1)){
	    T size = i-begin;
	    Interval<T> aux(oldValue,begin, i-1);
	    interval.push_back(aux);
	    Log::print(Detail) << "{interval.at(" << count << ")=" <<
	     	interval.at(count) <<"}" <<flush;
	    oldValue = newValue;
	    if (largest < size)
	    	largest = size;
	    begin = i;
	    ++count;
	}
    } 
    LOG_PTAG(Debug) << count<<flush;
    return largest;
}


bool write_struct(ostream& outStream, Interval<long>& data, const int i=1){
    return data.write(outStream);
}

bool read_struct(istream& inStream, Interval<long>& data, const int i=1){
    return data.read(inStream);
}

#endif //IINTERVALH_
//end-of-file
