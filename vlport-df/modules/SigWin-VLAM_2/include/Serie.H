//****************************************************************************
/** \file Serie.H 
 *  \brief class to store regular sequence parameters
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 04, 2006.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: November 16, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SERIEH_
#define SERIEH_

#include <iostream>
#include "LogManager.H"


//*****************************************************************************
// * Struct Serie	                                                     
//*****************************************************************************
template <class T> class Serie;

template<class T> ostream &operator<<(ostream &s, const Serie<T> &a);
template<class T> bool operator==(const Serie<T> &x,const Serie<T> &y);

template <class T> class Serie {
  public:
    //contructors
    Serie(){}
    Serie(T begin_, T delta_, long number_): 
    	b(begin_), e(begin_+delta_*(number_-1)), d(delta_), n(number_) { 	     }
    
    T begin(void) const {return b;}
    T end(void) const {return e;}
    T delta(void) const {return d;}
    long number(void) const {return n;}
    
    T min(void) const {return b;}
    T max(void) const {return e;}
    T step(void) const {return d;}
      
    T value(const long i) const {return i*d+b;}
    long index(const long v) const {return long((v-b)/d);}
    long index_test(const long v) const {
    	if((v<b)||(v>e)||((v-b)%d!=0)){
	    LOG_PTAG(Error) << "Element " << v << " not in series " 
	    	<< *this <<"\n";
	    exit(1);
    	}
    	return index(v);
    }
    
    friend ostream &operator<< <T> (ostream &s, const Serie<T> &a);
    friend bool operator== <T> (const Serie<T> &x, const Serie<T> &y);
    
    bool check_bounds_cresc(const T begin_, const T end_);
  
  private:
    T b;//begin or min 
    T e;//end or max 
    T d;//delta or step
    long n;//number   
};

template<class T> inline ostream &operator<<(ostream &s, const Serie<T> &a){
    s << "(b=" << a.b << ", e=" << a.e << ", d= " << a.d <<", n=" << a.n <<")";
    return s;
};

template<class T> inline bool operator==(const Serie<T> &x,const Serie<T> &y){
    if (x.b != y.b) return false;
    if (x.e != y.e) return false;
    if (x.d != y.d) return false;
    if (x.n != y.n) return false;
    return true;
}

template<class T> inline bool operator!=(const Serie<T> &x,const Serie<T> &y){
    if (!(x==y)) 
    	return true;
    else
    	return false;
}
 
///Makes sure the series bounds is between begin_ and end_.
template<class T> 
inline bool Serie<T>::check_bounds_cresc(const T begin_, const T end_){
    LOG_PTAG(Extreme)<< "Serie= " << *this << "(b=" << begin_ << ", e=" <<end_ 
    	    	   << ")\n";
    bool error(false);
    if (begin_ > end_){
	if (n != 0){
	    LOG_PTAG(Error) << "Serie should be empty!\n";
	    error = true;
	}
    	b=0; e=0; d=0; n=0;
    	error = false;
	return error;
    } 
	
    if (n == 0){
	LOG_PTAG(Info) << "Serie is empty!\n";
    	b=0; e=0; d=0;
	return false;
    }
    	
    if (b < begin_){
    	LOG_PTAG(Error) << "begin < " << begin_ << "! Redefining begin from " 
	    	    	<< b << " to " << begin_ << ".\n";
	b = begin_;
	error = true;
    }
    else if (b > end_){
    	LOG_PTAG(Error) << "begin > " << end_ << "! Redefining begin from " 
	    	    	<< b << " to " << end_ << ".\n";
	b = end_;
	error = true;
    }			 
				 
    if (e < b){
    	LOG_PTAG(Error)<< "end > begin. Redefining end from "
	               << e << " to begin (" << b << ").\n";
	e = b;
	n = 1;
	error = true;
    }
    else if (e > end_){
    	T old(e);
    	n = (end_-b)/d + 1;
    	e = b + d*(n-1);
    	LOG_PTAG(Error) << "end < " << end_ << "! Redefining end from " 
	    	    	<< old << " to " << e << ".\n";
	error = true;
    }
    else{
    	T eAux(b+(n-1)*d);
    	if (e != eAux){
	    if (eAux>end_){
	    	T nAux=(e-b)/d + 1;
    	    	LOG_PTAG(Error) << "Redefining number from " 
	    	    	        << n << " to " << nAux << ".\n";
	    	n=nAux;
		eAux=b+(n-1)*d;
	    }
    	    LOG_PTAG(Error) << "Redefining end from " 
	    	    	    << e << " to " << eAux << ".\n";
    	    e=eAux;
	}
    }
    return error;
}


#endif //REGSEQH_
//end-of-file
